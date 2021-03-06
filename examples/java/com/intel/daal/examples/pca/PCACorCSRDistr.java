/* file: PCACorCSRDistr.java */
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
 //  Content:
 //     Java example of principal component analysis (PCA) using the correlation
 //     method in the distributed processing mode for sparse data
 ////////////////////////////////////////////////////////////////////////////////
 */

/**
 * <a name="DAAL-EXAMPLE-JAVA-PCACORRELATIONCSRDISTRIBUTED">
 * @example PCACorCSRDistr.java
 */

package com.intel.daal.examples.pca;

import com.intel.daal.algorithms.PartialResult;
import com.intel.daal.algorithms.pca.DistributedStep1Local;
import com.intel.daal.algorithms.pca.DistributedStep2Master;
import com.intel.daal.algorithms.pca.PartialCorrelationResult;
import com.intel.daal.algorithms.pca.PartialCorrelationResultID;
import com.intel.daal.algorithms.pca.InputId;
import com.intel.daal.algorithms.pca.MasterInputId;
import com.intel.daal.algorithms.pca.Method;
import com.intel.daal.algorithms.pca.Result;
import com.intel.daal.algorithms.pca.ResultId;
import com.intel.daal.data_management.data.NumericTable;
import com.intel.daal.data_management.data.CSRNumericTable;
import com.intel.daal.data_management.data_source.DataSource;
import com.intel.daal.data_management.data_source.FileDataSource;
import com.intel.daal.examples.utils.Service;
import com.intel.daal.services.DaalContext;


class PCACorCSRDistr {
    /* Input data set parameters */
    private static final String datasetFileNames[] = new String[] { "../data/distributed/covcormoments_csr_1.csv",
                                                                    "../data/distributed/covcormoments_csr_2.csv",
                                                                    "../data/distributed/covcormoments_csr_3.csv",
                                                                    "../data/distributed/covcormoments_csr_4.csv"
                                                                  };
    private static final int nNodes = 4;

    private static PartialResult[] pres = new PartialResult[nNodes];

    private static DaalContext context = new DaalContext();

    public static void main(String[] args) throws java.io.FileNotFoundException, java.io.IOException {

        for (int i = 0; i < nNodes; i++) {
            DaalContext localContext = new DaalContext();

            /* Read the input data from a file */
            CSRNumericTable data = Service.createSparseTable(localContext, datasetFileNames[i]);

            /* Create an algorithm to compute PCA decomposition using the correlation method on local nodes */
            DistributedStep1Local pcaLocal = new DistributedStep1Local(localContext, Float.class,
                                                                       Method.correlationDense);

            com.intel.daal.algorithms.covariance.DistributedStep1Local covarianceSparse
                = new com.intel.daal.algorithms.covariance.DistributedStep1Local(localContext, Float.class,
                                                                                 com.intel.daal.algorithms.covariance.Method.fastCSR);
            pcaLocal.parameter.setCovariance(covarianceSparse);

            /* Set the input data on local nodes */
            pcaLocal.input.set(InputId.data, data);

            /* Compute PCA decomposition on local nodes */
            pres[i] = pcaLocal.compute();
            pres[i].pack();

            localContext.dispose();
        }

        /* Create an algorithm to compute PCA decomposition using the correlation method on the master node */
        DistributedStep2Master pcaMaster = new DistributedStep2Master(context, Float.class, Method.correlationDense);

        com.intel.daal.algorithms.covariance.DistributedStep2Master covarianceSparse
            = new com.intel.daal.algorithms.covariance.DistributedStep2Master(context, Float.class,
                                                                              com.intel.daal.algorithms.covariance.Method.fastCSR);
        pcaMaster.parameter.setCovariance(covarianceSparse);

        /* Add partial results computed on local nodes to the algorithm on the master node */
        for (int i = 0; i < nNodes; i++) {
            pres[i].unpack(context);
            pcaMaster.input.add(MasterInputId.partialResults, pres[i]);
        }

        /* Compute PCA decomposition on the master node */
        pcaMaster.compute();

        /* Finalize computations and retrieve the results */
        Result res = pcaMaster.finalizeCompute();

        NumericTable eigenValues = res.get(ResultId.eigenValues);
        NumericTable eigenVectors = res.get(ResultId.eigenVectors);
        Service.printNumericTable("Eigenvalues:", eigenValues);
        Service.printNumericTable("Eigenvectors:", eigenVectors);

        context.dispose();
    }
}
