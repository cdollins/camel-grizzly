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
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.Endpoint;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.util.ObjectHelper;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.net.URI;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

/**
 * Component for Grizzly.
 *
 * @version 1.0
 */
public class GrizzlyComponent extends DefaultComponent {
    private static final Logger LOG = LoggerFactory.getLogger(GrizzlyComponent.class);
    private GrizzlyConfiguration configuration;

    public GrizzlyComponent() {
    }

    public GrizzlyComponent(final CamelContext context) {
        super(context);
    }

    

    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining,
                                      final Map<String,Object> parameters) throws Exception {
        final GrizzlyConfiguration config;
        if (configuration != null) {
            config = configuration;
        } else {
           config = new GrizzlyConfiguration(); 
        }
        
        final URI u = new URI(remaining);
        config.setHost(u.getHost());
        config.setPort(u.getPort());
        config.setProtocol(u.getScheme());
        config.setFilters(parameters);

        setProperties(config, parameters);

        return createEndpoint(uri, config);
    }

    private Endpoint createEndpoint(final String uri, final GrizzlyConfiguration config) {
        ObjectHelper.notNull(getCamelContext(), "camelContext");
        final String protocol = config.getProtocol();
        if (protocol != null) {
            if (protocol.equals("tcp")) {
                return createTCPSocketEndpoint(uri, config);
                //Todo Support UDP
                //} else if (config.isDatagramProtocol()) {
                //return createDatagramEndpoint(uri, config);
            }
        }
        throw new IllegalArgumentException("Unrecognised Grizzly protocol: " + protocol + " for uri: " + uri);
    }

    private Endpoint createTCPSocketEndpoint(final String uri, final GrizzlyConfiguration config) {
        final boolean sync = config.isSync();

        final ExecutorService acceptorService = getCamelContext().getExecutorServiceStrategy().newDefaultThreadPool(this, "GrizzlySocketAcceptor");
        final ExecutorService connectorService = getCamelContext().getExecutorServiceStrategy().newDefaultThreadPool(this, "GrizzlySocketConnector");
        final SocketAddress address = new InetSocketAddress(config.getHost(), config.getPort());

        final TCPNIOTransport acceptor = new TCPNIOTransport();
        acceptor.setWorkerThreadPool(acceptorService);
        
        final TCPNIOTransport connector = new TCPNIOTransport();
        connector.setWorkerThreadPool(connectorService);

        final GrizzlyEndpoint endpoint = new GrizzlyEndpoint(uri, this);
        endpoint.setAddress(address);
        endpoint.setAcceptor(acceptor);
        endpoint.setConnector(connector);
        endpoint.setConfiguration(config);

        if (sync) {
            endpoint.setExchangePattern(ExchangePattern.InOut);
        } else {
            endpoint.setExchangePattern(ExchangePattern.InOnly);
        }

        return endpoint;
    }
}
