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
package org.camel.component.grizzly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.Exchange;
import org.apache.camel.CamelExchangeException;
import org.glassfish.grizzly.nio.transport.TCPNIOServerConnection;
import org.glassfish.grizzly.WriteResult;
import org.glassfish.grizzly.GrizzlyFuture;

import java.net.SocketAddress;
import java.io.IOException;

/**
 * Component for Grizzly.
 *
 * @version
 */
public class GrizzlyHelper {
    private static final transient Logger LOG = LoggerFactory.getLogger(GrizzlyHelper.class);

    private GrizzlyHelper() { /* Utility Class */ }

    /**
     * Writes the given body to Grizzly session. Will wait until the body has been written.
     *
     * @param connection  the Grizzly nio connection
     * @param body     the body to write (send)
     * @param exchange the exchange
     * @throws org.apache.camel.CamelExchangeException is thrown if the body could not be written for some reasons
     *                                (eg remote connection is closed etc.)
     */
    public static void writeBody(final TCPNIOServerConnection connection, Object body, Exchange exchange)
            throws CamelExchangeException, IOException {

        // the write operation is asynchronous. Use WriteFuture to wait until the session has been written
        GrizzlyFuture<WriteResult<Object, SocketAddress>> future = connection.write(body);
        
        // must use a timeout (we use 10s) as in some very high performance scenarios a write can cause 
        // thread hanging forever
        if (LOG.isTraceEnabled()) {
            LOG.trace("Waiting for write to complete");
        }
        future.join(10 * 1000L);
        
        if (!future.isDone()) {
            LOG.warn("Cannot write body: " + body + " using session: " + connection);
            throw new CamelExchangeException("Cannot write body", exchange);
        }
    }
}
