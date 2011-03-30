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

import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.*;
import org.apache.camel.util.ObjectHelper;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.Connection;

import java.net.SocketAddress;

/**
 * Component for Grizzly.
 *
 * @version  1.0
 */
public class GrizzlyEndpoint extends DefaultEndpoint implements MultipleConsumersSupport {
    private SocketAddress address;
    private GrizzlyConfiguration configuration;
    private TCPNIOTransport acceptor;
    private TCPNIOTransport connector;

    public GrizzlyEndpoint() {}

    public GrizzlyEndpoint(String endpointUri, GrizzlyComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        ObjectHelper.notNull(configuration, "configuration");
        ObjectHelper.notNull(address, "address");
        ObjectHelper.notNull(connector, "connector");

        return new GrizzlyProducer(this);
    }

    @Override
    public Consumer createConsumer(final Processor processor) throws Exception {
        ObjectHelper.notNull(configuration, "configuration");
        ObjectHelper.notNull(address, "address");
        ObjectHelper.notNull(acceptor, "acceptor");

        return new GrizzlyConsumer(this, processor);
    }
    
    public Exchange createExchange(final Connection connection, final Object payload) {
        Exchange exchange = createExchange();
        exchange.getIn().setHeader(GrizzlyConstants.GRIZZLY_CONNECTION, connection);
        exchange.getIn().setHeader(GrizzlyConstants.GRIZZLY_LOCAL_ADDRESS, connection.getLocalAddress());
        exchange.getIn().setHeader(GrizzlyConstants.GRIZZLY_REMOTE_ADDRESS, connection.getPeerAddress());
        GrizzlyPayloadHelper.setIn(exchange, payload);
        return exchange;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public boolean isMultipleConsumersSupported() {
        //Only Datagram which is not supported
        return false;
    }

    public TCPNIOTransport getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(final TCPNIOTransport acceptor) {
        this.acceptor = acceptor;
    }

    public void setAddress(final SocketAddress address) {
        this.address = address;
    }

    public SocketAddress getAddress() {
        return this.address;
    }

    public void setConfiguration(final GrizzlyConfiguration configuration) {
        this.configuration = configuration;
    }

    public GrizzlyConfiguration getConfiguration() {
        return this.configuration;
    }

    public void setConnector(final TCPNIOTransport connector) {
        this.connector = connector;
    }

    public TCPNIOTransport getConnector() {
        return this.connector;
    }
}
