/* file: DistributedPartialResultStep4Id.java */
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
 * @ingroup implicit_als_training_distributed
 * @{
 */
package com.intel.daal.algorithms.implicit_als.training;

/**
 * <a name="DAAL-CLASS-ALGORITHMS__IMPLICIT_ALS__TRAINING__DISTRIBUTEDPARTIALRESULTSTEP4ID"></a>
 * @brief Available identifiers of partial results of the implicit ALS training algorithm obtained
 * in the fourth step of the distributed processing mode
 */
public final class DistributedPartialResultStep4Id {
    private int _value;

    static {
        System.loadLibrary("JavaAPI");
    }

    /**
     * Constructs the partial result object identifier using the provided value
     * @param value     Value corresponding to the partial result object identifier
     */
    public DistributedPartialResultStep4Id(int value) {
        _value = value;
    }

    /**
     * Returns the value corresponding to the partial result object identifier
     * @return Value corresponding to the partial result object identifier
     */
    public int getValue() {
        return _value;
    }

    private static final int outputOfStep4ForStep1Id = 0;
    private static final int outputOfStep4ForStep3Id = 0;
    private static final int outputOfStep4Id = 0;

/** Partial results of the implicit ALS training algorithm obtained in the fourth step
*   and to be transferred to the first step of the distributed processing mode
*/
    public static final DistributedPartialResultStep4Id outputOfStep4ForStep1 = new DistributedPartialResultStep4Id(
            outputOfStep4ForStep1Id);

/** Partial results of the implicit ALS training algorithm obtained in the fourth step
*   and to be transferred to the third step of the distributed processing mode
*/
    public static final DistributedPartialResultStep4Id outputOfStep4ForStep3 = new DistributedPartialResultStep4Id(
            outputOfStep4ForStep3Id);
/** Partial results of the implicit ALS training algorithm obtained in the fourth step of the distributed processing mode */
    public static final DistributedPartialResultStep4Id outputOfStep4 = new DistributedPartialResultStep4Id(
            outputOfStep4Id);
}
/** @} */
