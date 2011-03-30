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

import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.Processor;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.processor.CamelLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOServerConnection;

import java.net.SocketAddress;

/**
 * Component for Grizzly.
 */
public class GrizzlyConsumer extends DefaultConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(GrizzlyConsumer.class);
    private boolean sync;
    private CamelLogger noReplyLogger;
    private SocketAddress address;
    private TCPNIOTransport acceptor;
    private TCPNIOServerConnection connection;

    public GrizzlyConsumer(final GrizzlyEndpoint grizzlyEndpoint, final Processor processor) {
        super(grizzlyEndpoint, processor);
        this.address = grizzlyEndpoint.getAddress();
        this.acceptor = grizzlyEndpoint.getAcceptor();
        this.sync = grizzlyEndpoint.getConfiguration().isSync();
        this.noReplyLogger = new CamelLogger(LOG, grizzlyEndpoint.getConfiguration().getNoReplyLogLevel());
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (LOG.isInfoEnabled()) {
            LOG.info("Binding to server address: " + address + " using acceptor: " + acceptor);
        }
        connection = acceptor.bind(address);
        acceptor.start();
    }

    @Override
    protected void doStop() throws Exception {
        ObjectHelper.notNull(connection, "connection");
        
        if (LOG.isInfoEnabled()) {
            LOG.info("Unbinding from server connection: " + connection + " using acceptor: " + acceptor);
        }
        acceptor.stop();
        super.doStop();
    }

    @Override
    public GrizzlyEndpoint getEndpoint() {
        return (GrizzlyEndpoint) super.getEndpoint();
    }
}
