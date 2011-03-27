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

import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Producer;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.util.ObjectHelper;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;

import java.net.SocketAddress;

/**
 * Component for Grizzly.
 *
 * @version  1.0
 */
public class GrizzlyEndpoint extends DefaultEndpoint implements MultipleConsumersSupport {
    private final String uri;
    private final GrizzlyComponent grizzlyComponent;
    private SocketAddress address;
    private GrizzlyConfiguration configuration;
    private TCPNIOTransport acceptor;
    private TCPNIOTransport connector;

    public GrizzlyEndpoint(final String uri, final GrizzlyComponent grizzlyComponent) {
        this.uri = uri;
        this.grizzlyComponent = grizzlyComponent;
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
