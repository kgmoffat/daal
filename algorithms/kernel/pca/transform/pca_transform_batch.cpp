/* file: pca_transform_batch.cpp */
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
//  Implementation of the regression algorithm classes.
//--
*/

#include "algorithms/pca/transform/pca_transform_types.h"
#include "serialization_utils.h"
#include "daal_strings.h"

using namespace daal::data_management;
using namespace daal::services;

namespace daal
{
namespace algorithms
{
namespace pca
{
namespace transform
{
namespace interface1
{
__DAAL_REGISTER_SERIALIZATION_CLASS(Result, SERIALIZATION_PCA_TRANSFORM_RESULT_ID);

Input::Input() : daal::algorithms::Input(lastdataForTransformInputId + 1)
{}

Input::Input(const Input &other) : daal::algorithms::Input(other)
{}

NumericTablePtr Input::get(InputId id) const
{
    return NumericTable::cast(Argument::get(id));
}

KeyValueDataCollectionPtr Input::get(TransformDataInputId id) const
{
    return KeyValueDataCollection::cast(Argument::get(id));
}

NumericTablePtr Input::get(TransformDataInputId wid, TransformComponentId id) const
{
    KeyValueDataCollectionPtr dataCollectionPtr = get(wid);
    if (get(wid).get() == NULL)
        return services::SharedPtr<NumericTable>();
    return NumericTable::cast((*dataCollectionPtr)[id]);
}

void Input::set(TransformDataInputId wid, TransformComponentId id, const NumericTablePtr &value)
{
    if (get(wid).get() == NULL)
    {
        set(wid, KeyValueDataCollectionPtr(new KeyValueDataCollection()));
    }
    KeyValueDataCollectionPtr dataCollectionPtr = get(wid);
    (*dataCollectionPtr)[id] = value;
}


void Input::set(InputId id, const NumericTablePtr &value)
{
    Argument::set(id, value);
}

void Input::set(TransformDataInputId id, const KeyValueDataCollectionPtr &value)
{
    Argument::set(id, value);
}


Status Input::check(const daal::algorithms::Parameter *par, int method) const
{
    Status s;
    const Parameter* parameter = static_cast<const Parameter *>(par);

    NumericTablePtr dataPtr = get(data);
    NumericTablePtr eigenvectorsPtr = get(eigenvectors);
    DAAL_CHECK_EX(dataPtr.get(), ErrorNullInputNumericTable, ArgumentName, dataStr());
    DAAL_CHECK_EX(eigenvectorsPtr.get(), ErrorNullInputNumericTable, ArgumentName, eigenvectorsStr());

    size_t nFeatures = dataPtr->getNumberOfColumns();
    size_t nFeaturesInEigen = eigenvectorsPtr->getNumberOfColumns();
    size_t nInputs = dataPtr->getNumberOfRows();
    size_t nEigenvectors = eigenvectorsPtr->getNumberOfRows();

    DAAL_CHECK_STATUS(s, checkNumericTable(dataPtr.get(), dataStr(), 0, 0, nFeatures, nInputs));
    DAAL_CHECK_STATUS(s, checkNumericTable(eigenvectorsPtr.get(), eigenvectorsStr(), packed_mask, 0, nFeaturesInEigen, nEigenvectors));
    DAAL_CHECK(nFeatures == nFeaturesInEigen, ErrorInconsistentNumberOfColumns);
    DAAL_CHECK(nEigenvectors <= nFeaturesInEigen, ErrorIncorrectNumberOfRowsInInputNumericTable);
    DAAL_CHECK_EX(nEigenvectors >= parameter->nComponents, ErrorIncorrectParameter, ParameterName, nComponentsStr());

    bool hasTransform = get(dataForTransform).get() != NULL;
    if(hasTransform)
    {
        NumericTablePtr pMeans = get(dataForTransform, mean);
        if(pMeans)
        {
            DAAL_CHECK_STATUS(s, checkNumericTable( pMeans.get(), meanStr(), packed_mask, 0,nFeatures, 1));
        }
        NumericTablePtr pVariances = get(dataForTransform, variance);
        if(pVariances)
        {
            DAAL_CHECK_STATUS(s, checkNumericTable( pVariances.get(), varianceStr(), packed_mask, 0, nFeatures, 1));
        }
        NumericTablePtr pEigenvalue = get(dataForTransform, eigenvalue);
        if(pEigenvalue)
        {
            DAAL_CHECK_STATUS(s, checkNumericTable( pEigenvalue.get(), eigenvalueStr(), packed_mask, 0, nFeatures, 1));
        }
    }

    return s;
}


Result::Result() : daal::algorithms::Result(lastResultId + 1) {}

NumericTablePtr Result::get(ResultId id) const
{
    return NumericTable::cast(Argument::get(id));
}

void Result::set(ResultId id, const NumericTablePtr &value)
{
    Argument::set(id, value);
}

Status Result::check(const daal::algorithms::Input * input, const daal::algorithms::Parameter * par, int method) const
{
    Status s;
    const Input* in = static_cast<const Input *>(input);
    const Parameter* parameter = static_cast<const Parameter *>(par);

    NumericTablePtr dataPtr = in->get(data);
    NumericTablePtr eigenvectorsPtr = in->get(eigenvectors);
    DAAL_CHECK_EX(dataPtr.get(), ErrorNullInputNumericTable, ArgumentName, dataStr());
    DAAL_CHECK_EX(eigenvectorsPtr.get(), ErrorNullInputNumericTable, ArgumentName, eigenvectorsStr());

    size_t nInputs = dataPtr->getNumberOfRows();
    size_t nComponents = parameter->nComponents == 0 ? eigenvectorsPtr->getNumberOfRows() : parameter->nComponents;
    DAAL_CHECK_STATUS(s, checkNumericTable(get(transformedData).get(), transformedDataStr(), packed_mask, 0, nComponents, nInputs));

    return s;
}

Parameter::Parameter(size_t nComponents) : nComponents(nComponents) {}

} // interface1
} // transform
} // pca
} // algorithm
} // daal
