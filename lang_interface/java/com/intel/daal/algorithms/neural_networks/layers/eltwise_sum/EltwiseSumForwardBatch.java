/* file: EltwiseSumForwardBatch.java */
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
 * @defgroup eltwise_sum_forward_batch Batch
 * @ingroup eltwise_sum_forward
 * @{
 */
package com.intel.daal.algorithms.neural_networks.layers.eltwise_sum;

import com.intel.daal.algorithms.Precision;
import com.intel.daal.algorithms.ComputeMode;
import com.intel.daal.algorithms.AnalysisBatch;
import com.intel.daal.services.DaalContext;

/**
 * <a name="DAAL-CLASS-ALGORITHMS__NEURAL_NETWORKS__LAYERS__ELTWISE_SUM__ELTWISESUMFORWARDBATCH"></a>
 * \brief Class that computes the results of the forward element-wise sum layer in the batch processing mode
 * <!-- \n<a href="DAAL-REF-ELTWISESUMFORWARD">Forward element-wise sum layer description and usage models</a> -->
 *
 * \par References
 *      - @ref EltwiseSumLayerDataId class
 */
public class EltwiseSumForwardBatch extends com.intel.daal.algorithms.neural_networks.layers.ForwardLayer {
    public  EltwiseSumForwardInput input;     /*!< %Input data */
    public  EltwiseSumMethod       method;    /*!< Computation method for the layer */
    public  EltwiseSumParameter    parameter; /*!< EltwiseSumParameters of the layer */
    private Precision              prec;      /*!< Data type to use in intermediate computations for the layer */

    /** @private */
    static {
        System.loadLibrary("JavaAPI");
    }

    /**
     * Constructs the forward element-wise sum layer by copying input objects of another forward element-wise sum layer
     * @param context    Context to manage the forward element-wise sum layer
     * @param other      A forward element-wise sum layer to be used as the source to
     *                   initialize the input objects of the forward element-wise sum layer
     */
    public EltwiseSumForwardBatch(DaalContext context, EltwiseSumForwardBatch other) {
        super(context);
        this.method = other.method;
        prec = other.prec;

        this.cObject = cClone(other.cObject, prec.getValue(), method.getValue());
        input = new EltwiseSumForwardInput(context, cGetInput(cObject, prec.getValue(), method.getValue()));
        parameter = new EltwiseSumParameter(context, cInitParameter(cObject, prec.getValue(), method.getValue()));
    }

    /**
     * Constructs the forward element-wise sum layer
     * @param context    Context to manage the layer
     * @param cls        Data type to use in intermediate computations for the layer, Double.class or Float.class
     * @param method     The layer computation method, @ref EltwiseSumMethod
     */
    public EltwiseSumForwardBatch(DaalContext context, Class<? extends Number> cls, EltwiseSumMethod method) {
        super(context);

        this.method = method;

        if (method != EltwiseSumMethod.defaultDense) {
            throw new IllegalArgumentException("method unsupported");
        }
        if (cls != Double.class && cls != Float.class) {
            throw new IllegalArgumentException("type unsupported");
        }

        if (cls == Double.class) {
            prec = Precision.doublePrecision;
        }
        else {
            prec = Precision.singlePrecision;
        }

        this.cObject = cInit(prec.getValue(), method.getValue());
        input = new EltwiseSumForwardInput(context, cGetInput(cObject, prec.getValue(), method.getValue()));
        parameter = new EltwiseSumParameter(context, cInitParameter(cObject, prec.getValue(), method.getValue()));
    }

    EltwiseSumForwardBatch(DaalContext context, Class<? extends Number> cls, EltwiseSumMethod method, long cObject) {
        super(context);

        this.method = method;

        if (method != EltwiseSumMethod.defaultDense) {
            throw new IllegalArgumentException("method unsupported");
        }
        if (cls != Double.class && cls != Float.class) {
            throw new IllegalArgumentException("type unsupported");
        }

        if (cls == Double.class) {
            prec = Precision.doublePrecision;
        }
        else {
            prec = Precision.singlePrecision;
        }

        this.cObject = cObject;
        input = new EltwiseSumForwardInput(context, cGetInput(cObject, prec.getValue(), method.getValue()));
        parameter = new EltwiseSumParameter(context, cInitParameter(cObject, prec.getValue(), method.getValue()));
    }

    /**
     * Computes the result of the forward element-wise sum layer
     * @return  Forward element-wise sum layer result
     */
    @Override
    public EltwiseSumForwardResult compute() {
        super.compute();
        EltwiseSumForwardResult result = new EltwiseSumForwardResult(getContext(), cGetResult(cObject, prec.getValue(), method.getValue()));
        return result;
    }

    /**
     * Registers user-allocated memory to store the result of the forward element-wise sum layer
     * @param result    Structure to store the result of the forward element-wise sum layer
     */
    public void setResult(EltwiseSumForwardResult result) {
        cSetResult(cObject, prec.getValue(), method.getValue(), result.getCObject());
    }

    /**
     * Returns the structure that contains result of the forward layer
     * @return Structure that contains result of the forward layer
     */
    @Override
    public EltwiseSumForwardResult getLayerResult() {
        return new EltwiseSumForwardResult(getContext(), cGetResult(cObject, prec.getValue(), method.getValue()));
    }

    /**
     * Returns the structure that contains input object of the forward layer
     * @return Structure that contains input object of the forward layer
     */
    @Override
    public EltwiseSumForwardInput getLayerInput() {
        return input;
    }

    /**
     * Returns the structure that contains parameters of the forward layer
     * @return Structure that contains parameters of the forward layer
     */
    @Override
    public EltwiseSumParameter getLayerParameter() {
        return parameter;
    }

    /**
     * Returns the newly allocated forward element-wise sum layer
     * with a copy of input objects of this forward element-wise sum layer
     * @param context    Context to manage the layer
     *
     * @return The newly allocated forward element-wise sum layer
     */
    @Override
    public EltwiseSumForwardBatch clone(DaalContext context) {
        return new EltwiseSumForwardBatch(context, this);
    }

    private native long cInit(int prec, int method);
    private native long cInitParameter(long cAlgorithm, int prec, int method);
    private native long cGetInput(long cAlgorithm, int prec, int method);
    private native long cGetResult(long cAlgorithm, int prec, int method);
    private native void cSetResult(long cAlgorithm, int prec, int method, long cObject);
    private native long cClone(long algAddr, int prec, int method);
}
/** @} */
