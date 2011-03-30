/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.grizzly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.Exchange;
import org.apache.camel.CamelExchangeException;
import org.glassfish.grizzly.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Component for Grizzly.
 *
 * @version 1.0
 */
public class GrizzlyHelper {
    private static final Logger LOG = LoggerFactory.getLogger(GrizzlyHelper.class);

    private GrizzlyHelper() { /* Utility Class */ }

    /**
     * Writes the given body to Grizzly session. Will wait until the body has been written.
     * @param connection
     * @param body
     * @param exchange the exchange  @throws org.apache.camel.CamelExchangeException is
     *        thrown if the body could not be written for some reasons
     * @throws java.io.IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * @throws java.util.concurrent.TimeoutException
     * @throws org.apache.camel.CamelExchangeException
     */
    public static void writeBody(final Connection connection, final Object body, final Exchange exchange) throws IOException,
            ExecutionException, TimeoutException, InterruptedException, CamelExchangeException {
        
        final GrizzlyFuture<WriteResult> future = connection.write(body);

        if (LOG.isTraceEnabled()) {
            LOG.trace("Waiting for write to complete");
        }

        future.get(10, TimeUnit.SECONDS);

        assert future != null;

        if (!future.isDone()) {
            LOG.warn("Cannot write body: " + body + " using connection: " + connection);
            throw new CamelExchangeException("Cannot write body", exchange);
        }
    }
}
