/* file: dtrees_feature_type_helper.i */
/*******************************************************************************
* Copyright 2014-2018 Intel Corporation
* All Rights Reserved.
*
* If this  software was obtained  under the  Intel Simplified  Software License,
* the following terms apply:
*
* The source code,  information  and material  ("Material") contained  herein is
* owned by Intel Corporation or its  suppliers or licensors,  and  title to such
* Material remains with Intel  Corporation or its  suppliers or  licensors.  The
* Material  contains  proprietary  information  of  Intel or  its suppliers  and
* licensors.  The Material is protected by  worldwide copyright  laws and treaty
* provisions.  No part  of  the  Material   may  be  used,  copied,  reproduced,
* modified, published,  uploaded, posted, transmitted,  distributed or disclosed
* in any way without Intel's prior express written permission.  No license under
* any patent,  copyright or other  intellectual property rights  in the Material
* is granted to  or  conferred  upon  you,  either   expressly,  by implication,
* inducement,  estoppel  or  otherwise.  Any  license   under such  intellectual
* property rights must be express and approved by Intel in writing.
*
* Unless otherwise agreed by Intel in writing,  you may not remove or alter this
* notice or  any  other  notice   embedded  in  Materials  by  Intel  or Intel's
* suppliers or licensors in any way.
*
*
* If this  software  was obtained  under the  Apache License,  Version  2.0 (the
* "License"), the following terms apply:
*
* You may  not use this  file except  in compliance  with  the License.  You may
* obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*
*
* Unless  required  by   applicable  law  or  agreed  to  in  writing,  software
* distributed under the License  is distributed  on an  "AS IS"  BASIS,  WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*
* See the   License  for the   specific  language   governing   permissions  and
* limitations under the License.
*******************************************************************************/

/*
//++
//  Cpu-dependent initialization of service data structure
//--
*/
#include "dtrees_feature_type_helper.h"
#include "threading.h"
#include "service_error_handling.h"
#include "service_sort.h"
#include "service_array.h"

namespace daal
{
namespace algorithms
{
namespace dtrees
{
namespace internal
{

template <typename IndexType, typename algorithmFPType, CpuType cpu>
struct ColIndexTask
{
    DAAL_NEW_DELETE();
    ColIndexTask(size_t nRows) : _index(nRows), maxNumDiffValues(0){}
    bool isValid() const { return _index.get(); }

    struct FeatureIdx
    {
        algorithmFPType key;
        IndexType val;
        static int compare(const void *a, const void *b)
        {
            if(static_cast<const FeatureIdx*>(a)->key < static_cast<const FeatureIdx*>(b)->key)
                return -1;
            return static_cast<const FeatureIdx*>(a)->key > static_cast<const FeatureIdx*>(b)->key;
        }
        bool operator < (const FeatureIdx& o) const
        {
            return key < o.key;
        }
    };

    virtual services::Status makeIndex(NumericTable& nt, IndexedFeatures::FeatureEntry& entry,
        IndexType* aRes, size_t iCol, size_t nRows, bool bUnorderedFeature)
    {
        return this->makeIndexDefault(nt, entry, aRes, iCol, nRows, bUnorderedFeature);
    }

    services::Status makeIndexDefault(NumericTable& nt, IndexedFeatures::FeatureEntry& entry,
        IndexType* aRes, size_t iCol, size_t nRows, bool bUnorderedFeature)
    {
        Status s = this->getSorted(nt, iCol, nRows);
        if(!s)
            return s;
        const FeatureIdx* index = _index.get();
        if(index[0].key == index[nRows - 1].key)
        {
            entry.numIndices = 1;
            return s;
        }
        size_t iUnique = 0;
        aRes[index[0].val] = iUnique;
        algorithmFPType prev = index[0].key;
        for(size_t i = 1; i < nRows; ++i)
        {
            const IndexType idx = index[i].val;
            if(index[i].key == prev)
                aRes[idx] = iUnique;
            else
            {
                aRes[idx] = ++iUnique;
                prev = index[i].key;
            }
        }
        ++iUnique;
        entry.numIndices = iUnique;
        if(maxNumDiffValues < iUnique)
            maxNumDiffValues = iUnique;
        return services::Status();
    }

public:
    size_t maxNumDiffValues;

protected:
    Status getSorted(NumericTable& nt, size_t iCol, size_t nRows)
    {
        const algorithmFPType* pBlock = _block.set(&nt, iCol, 0, nRows);
        DAAL_CHECK_BLOCK_STATUS(_block);
        FeatureIdx* index = _index.get();
        for(size_t i = 0; i < nRows; ++i)
        {
            index[i].key = pBlock[i];
            index[i].val = i;
        }
        daal::algorithms::internal::qSort<FeatureIdx, cpu>(nRows, index, FeatureIdx::compare);
        return Status();
    }

protected:
    daal::internal::ReadColumns<algorithmFPType, cpu> _block;
    TVector<FeatureIdx, cpu, DefaultAllocator<cpu>> _index;
};


template <typename IndexType, typename algorithmFPType, CpuType cpu>
struct ColIndexTaskBins : public ColIndexTask<IndexType, algorithmFPType, cpu>
{
    typedef ColIndexTask<IndexType, algorithmFPType, cpu> super;
    ColIndexTaskBins(size_t nRows, const BinParams& prm) : super(nRows), _prm(prm), _bins(_prm.maxBins + 1){}
    virtual services::Status makeIndex(NumericTable& nt, IndexedFeatures::FeatureEntry& entry,
        IndexType* aRes, size_t iCol, size_t nRows, bool bUnorderedFeature) DAAL_C11_OVERRIDE;

private:
    services::Status assignIndexAccordingToBins(IndexedFeatures::FeatureEntry& entry, IndexType* aRes, size_t nBins);

private:
    const BinParams _prm;
    TVector<size_t, cpu, DefaultAllocator<cpu>> _bins;
};

template <typename TContainer>
static void append(TContainer& cont, size_t& contSize, size_t size)
{
    cont[contSize++] = size;
}

//Returns an index of the first element in the range[ar, ar + n) that is not less than(i.e.greater or equal to) value.
template<typename T>
const T* upper_bound(const T* first, const T* last, const T& value)
{
    size_t n = last - first;
    while(n > 0)
    {
        auto it = first;
        auto step = (n >> 1);
        it += step;
        if(!(value < *it))
        {
            first = ++it;
            n -= step + 1;
        }
        else
            n = step;
    }
    return first;
}

template <typename IndexType, typename algorithmFPType, CpuType cpu>
services::Status ColIndexTaskBins<IndexType, algorithmFPType, cpu>::assignIndexAccordingToBins(
    IndexedFeatures::FeatureEntry& entry, IndexType* aRes, size_t nBins)
{
    if(nBins == 1)
    {
        entry.numIndices = 0;
        return Status();
    }
    entry.numIndices = nBins;
    services::Status s = entry.allocBorders();
    if(!s)
        return s;
    const typename super::FeatureIdx* index = this->_index.get();
    size_t i = 0;
    for(size_t iBin = 0; iBin < nBins; ++iBin)
    {
        for(size_t n = i + _bins[iBin]; i < n; ++i)
            aRes[index[i].val] = iBin;
        entry.binBorders[iBin] = index[i - 1].key;
    }
    if(this->maxNumDiffValues < entry.numIndices)
        this->maxNumDiffValues = entry.numIndices;
    return s;
}

template <typename IndexType, typename algorithmFPType, CpuType cpu>
services::Status ColIndexTaskBins<IndexType, algorithmFPType, cpu>::makeIndex(NumericTable& nt,
    IndexedFeatures::FeatureEntry& entry, IndexType* aRes, size_t iCol, size_t nRows, bool bUnorderedFeature)
{
    if(bUnorderedFeature || nRows <= _prm.maxBins*_prm.minBinSize)
        return this->makeIndexDefault(nt, entry, aRes, iCol, nRows, bUnorderedFeature);

    Status s = this->getSorted(nt, iCol, nRows);
    if(!s)
        return s;

    const typename super::FeatureIdx* index = this->_index.get();
    if(index[0].key == index[nRows - 1].key)
    {
        entry.numIndices = 1;
        return s;
    }

    size_t nBins = 0;
    const size_t binSize = nRows / _prm.maxBins;
    size_t i = 0;
    for(; i + binSize < nRows;)
    {
        //trying to make a bin of size binSize
        size_t iRight = i + binSize - 1;
        size_t newBinSize = binSize;
        const typename super::FeatureIdx& ri = index[iRight];
        if(ri.key == index[iRight + 1].key)
        {
            //right border can't be placed at iRight because it has to be between different feature values
            //try moving the border to the right, find the first value bigger than the value at iRight
            ++iRight;
            size_t r = iRight + binSize;
            //at first, roughly locate the value bigger than iRight, jumping by binSize to the right
            for(; (r < nRows) && (index[r].key == ri.key); r += binSize){}
            if(r > nRows)
                r = nRows;
            //then locate a new border as the upper_bound between this rough value and iRight
            iRight = upper_bound<typename super::FeatureIdx>(index + iRight + 1, index + r, ri) - index;
            //this is the size of the bin
            newBinSize = iRight - i;
            //if the value it is too big (number of feature values equal to ri.key is bigger than binSize)
            //then perhaps left border of the bin can be moved to the right
            if(newBinSize >= 2 * binSize)
            {
                size_t iClosestSmallerValue = i + binSize - 1;
                for(; (iClosestSmallerValue > i) && (index[iClosestSmallerValue].key == ri.key); --iClosestSmallerValue);
                size_t dist = iClosestSmallerValue - i;
                if(dist > _prm.minBinSize)
                {
                    //add an extra bin at the left
                    const size_t newLeftBinSize = dist + 1;
                    append(_bins, nBins, newLeftBinSize);
                    i += newLeftBinSize;
                    newBinSize -= newLeftBinSize;
                }
                else if((nBins > 0) && dist)
                {
                    //if it is small and not the first bin, then extend previous bin by the value
                    const size_t nAddToPrevBin = dist + 1;
                    _bins[nBins - 1] += nAddToPrevBin;
                    i += nAddToPrevBin;
                    newBinSize -= nAddToPrevBin;
                }
            }
        }
        append(_bins, nBins, newBinSize);
        i += newBinSize;
    }
    if(i < nRows)
    {
        size_t newBinSize = nRows - i;
        if((newBinSize < _prm.minBinSize) && nBins)
            _bins[nBins - 1] += newBinSize;
        else
            append(_bins, nBins, nRows - i);
    }

#if _DEBUG
#if 0
    //run-time check for bins correctness
    size_t nTotal = 0;
    for(size_t i = 0; i < nBins; nTotal += _bins[i], ++i);
    DAAL_ASSERT(nTotal == nRows);
    size_t iBorder = 0;
    for(size_t i = 1; i < nBins; ++i)
    {
        iBorder += _bins[i - 1];
        DAAL_ASSERT(index[iBorder - 1].key != index[iBorder].key);
    }
#endif
#endif
    return assignIndexAccordingToBins(entry, aRes, nBins);
}

template <typename algorithmFPType, CpuType cpu>
services::Status IndexedFeatures::init(const NumericTable& nt, const FeatureTypes* featureTypes,
    const BinParams* pBimPrm)
{
    dtrees::internal::FeatureTypes featTypes;
    if(!featureTypes)
    {
        DAAL_CHECK_MALLOC(featTypes.init(nt));
        featureTypes = &featTypes;
    }

    _maxNumIndices = 0;
    services::Status s = alloc(nt.getNumberOfColumns(), nt.getNumberOfRows());
    if(!s)
        return s;

    const size_t nC = nt.getNumberOfColumns();
    const size_t nR = nt.getNumberOfRows();
    typedef ColIndexTask<IndexType, algorithmFPType, cpu> TlsTask;
    typedef ColIndexTask<IndexType, algorithmFPType, cpu> DefaultTask;
    typedef ColIndexTaskBins<IndexType, algorithmFPType, cpu> BinningTask;

    daal::tls<TlsTask*> tlsData([=, &nt]()->TlsTask*
    {
        const size_t nRows = nt.getNumberOfRows();
        TlsTask* res = (pBimPrm ? new BinningTask(nRows, *pBimPrm) : new DefaultTask(nRows));
        if(res && !res->isValid())
        {
            delete res;
            res = nullptr;
        }
        return res;
    });

    SafeStatus safeStat;
    daal::threader_for(nC, nC, [&](size_t iCol)
    {
        //in case of single thread no need to allocate
        TlsTask* task = tlsData.local();
        DAAL_CHECK_THR(task, services::ErrorMemoryAllocationFailed);
        safeStat |= task->makeIndex(const_cast<NumericTable&>(nt), _entries[iCol], _data + iCol*nRows(), iCol, nRows(),
            featTypes.isUnordered(iCol));
    });
    tlsData.reduce([&](TlsTask* task)-> void
    {
        if(_maxNumIndices < task->maxNumDiffValues)
            _maxNumIndices = task->maxNumDiffValues;
        delete task;
    });
    return safeStat.detach();
}

} /* namespace internal */
} /* namespace dtrees */
} /* namespace algorithms */
} /* namespace daal */
