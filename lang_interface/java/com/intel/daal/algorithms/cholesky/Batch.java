/* file: Batch.java */
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

/**
 * @defgroup cholesky Cholesky Decomposition
 * @brief Contains classes for computing Cholesky decomposition
 * @ingroup analysis
 * @{
 */
/**
 * @defgroup cholesky_batch Batch
 * @ingroup cholesky
 * @{
 */
package com.intel.daal.algorithms.cholesky;

import com.intel.daal.algorithms.AnalysisBatch;
import com.intel.daal.algorithms.Precision;
import com.intel.daal.services.DaalContext;

/**
 * <a name="DAAL-CLASS-ALGORITHMS__CHOLESKY__BATCH"></a>
 * \brief Computes Cholesky decomposition in the batch processing mode.
 * <!-- \n<a href="DAAL-REF-CHOLESKY-ALGORITHM">Cholesky decomposition algorithm description and usage models</a> -->
 *
 * \par References
 *      - @ref InputId class
 *      - @ref ResultId class
 */
public class Batch extends AnalysisBatch {
    public Input          input;     /*!< %Input data */
    public Method     method; /*!< Computation method for the algorithm */
    private Precision                 prec; /*!< Precision of intermediate computations */

    /** @private */
    static {
        System.loadLibrary("JavaAPI");
    }

    /**
     * Constructs the Cholesky decomposition algorithm by copying input objects
     * of another Cholesky decomposition algorithm
     * @param context    Context to manage the Cholesky decomposition algorithm
     * @param[in] other  An algorithm to be used as the source to initialize the input objects
     *                   and parameters of the algorithm
     */
    public Batch(DaalContext context, Batch other) {
        super(context);
        this.method = other.method;
        prec = other.prec;

        this.cObject = cClone(other.cObject, prec.getValue(), method.getValue());
        input = new Input(context, cObject, prec, method);
    }

    /**
     * <a name="DAAL-METHOD-ALGORITHM__CHOLESKY__BATCH__BATCH"></a>
     * Constructs the Cholesky decomposition algorithm
     *
     * @param context    Context to manage the Cholesky decomposition algorithm
     * @param cls        Data type to use in intermediate computations for Cholesky decomposition, Double.class or Float.class
     * @param method     Cholesky decomposition computation method, @ref Method
     */
    public Batch(DaalContext context, Class<? extends Number> cls, Method method) {
        super(context);

        this.method = method;

        if (method != Method.defaultDense) {
            throw new IllegalArgumentException("method unsupported");
        }
        if (cls != Double.class && cls != Float.class) {
            throw new IllegalArgumentException("type unsupported");
        }

        if (cls == Double.class) {
            prec = Precision.doublePrecision;
        } else {
            prec = Precision.singlePrecision;
        }

        this.cObject = cInit(prec.getValue(), method.getValue());
        input = new Input(context, cObject, prec, method);
    }

    /**
     * Computes Cholesky decomposition
     * @return  Cholesky decomposition results
    */
    @Override
    public Result compute() {
        super.compute();
        Result result = new Result(getContext(), cGetResult(cObject, prec.getValue(), method.getValue()));
        return result;
    }

    /**
     * Registers user-allocated memory to store results of Cholesky decomposition
     * @param result    Structure to store results of Cholesky decomposition
     */
    public void setResult(Result result) {
        cSetResult(cObject, prec.getValue(), method.getValue(), result.getCObject());
    }

    /**
     * Returns the newly allocated Cholesky decomposition algorithm with a copy of input objects
     * of this Cholesky decomposition algorithm
     * @param context    Context to manage the Cholesky decomposition algorithm
     *
     * @return The newly allocated algorithm
     */
    @Override
    public Batch clone(DaalContext context) {
        return new Batch(context, this);
    }

    private native long cInit(int prec, int method);

    private native long cGetResult(long cAlgorithm, int prec, int method);

    private native void cSetResult(long cAlgorithm, int prec, int method, long cObject);

    private native long cClone(long algAddr, int prec, int method);
}
/** @} */
/** @} */
