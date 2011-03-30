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
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.Endpoint;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.util.ObjectHelper;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.AbstractCodecFilter;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.utils.LogFilter;
import org.glassfish.grizzly.utils.StringFilter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.net.URI;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

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
        if (configuration != null ) {
            config = configuration;
        } else {
           config = new GrizzlyConfiguration(); 
        }
        
        final URI u = new URI(remaining);
        config.setHost(u.getHost());
        config.setPort(u.getPort());
        config.setProtocol(u.getScheme());
        config.setFilters(resolveAndRemoveReferenceListParameter(parameters, "filters", Filter.class));
        setProperties(config, parameters);

        return createEndpoint(uri, config);
    }

    private Endpoint createEndpoint(final String uri, final GrizzlyConfiguration config)
            throws IllegalAccessException, InstantiationException {
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

    private Endpoint createTCPSocketEndpoint(final String uri, final GrizzlyConfiguration config)
            throws InstantiationException, IllegalAccessException {
        final boolean sync = config.isSync();
        final int timeout =(int)(config.getTimeout() / 1000);

        final ExecutorService acceptorService = getCamelContext().getExecutorServiceStrategy().newDefaultThreadPool(this, "GrizzlySocketAcceptor");
        final ExecutorService connectorService = getCamelContext().getExecutorServiceStrategy().newDefaultThreadPool(this, "GrizzlySocketConnector");
        final SocketAddress address = new InetSocketAddress(config.getHost(), config.getPort());

        // Todo create the selector thread form camel thread pool
        final FilterChainBuilder acceptorChainBuilder = FilterChainBuilder.stateless();
        acceptorChainBuilder.addLast(getCodecFilter(config));
        if (config.isGrizzlyLogger()) {
            acceptorChainBuilder.addLast(new LogFilter());
        }
        acceptorChainBuilder.addAll(config.getFilters());
        final TCPNIOTransport acceptor = TCPNIOTransportBuilder.newInstance()
                .setConnectionTimeout(timeout)
                .setProcessor(acceptorChainBuilder.build())
                .build();
        acceptor.setWorkerThreadPool(acceptorService);

        // Todo create the selector thread form camel thread pool
        final FilterChainBuilder connectorChainBuilder = FilterChainBuilder.stateless();
        connectorChainBuilder.addLast(getCodecFilter(config));
        if (config.isGrizzlyLogger()) {
            connectorChainBuilder.addLast(new LogFilter());
        }
        connectorChainBuilder.addAll(config.getFilters());
        final TCPNIOTransport connector = TCPNIOTransportBuilder.newInstance()
                .setConnectionTimeout(timeout)
                .setProcessor(connectorChainBuilder.build())
                .build();
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

    private Filter getCodecFilter(final GrizzlyConfiguration config) {
        if (config.getCodec() != null) {
            return new AbstractCodecFilter(config.getCodec().getEncoder(),
                    config.getCodec().getDecoder()) { };
        } else {
            return getDefaultCodecFilter(config);
        }
    }

    private Filter getDefaultCodecFilter(final GrizzlyConfiguration config) {
        if (config.isTextline()) {
            final Charset charset = getEncodingParameter(configuration);
            final String delimiter = configuration.getTextlineDelimiter();
            return new StringFilter(charset, delimiter);
        } else {
            //Todo We want object serialization here
            return new BaseFilter();
        }
    }

    private static Charset getEncodingParameter(final GrizzlyConfiguration config) {
        String encoding = config.getEncoding();
        if (encoding == null) {
            encoding = Charset.defaultCharset().name();
            // set in on config so its updated
            config.setEncoding(encoding);
            if (LOG.isDebugEnabled()) {
                LOG.debug("No encoding parameter using default charset: " + encoding);
            }
        }
        if (!Charset.isSupported(encoding)) {
            throw new IllegalArgumentException("The encoding: " + encoding + " is not supported");
        }

        return Charset.forName(encoding);
    }

    public void setConfiguration(final GrizzlyConfiguration configuration) {
        this.configuration = configuration;
    }

    public GrizzlyConfiguration getConfiguration() {
        return this.configuration;
    }
}
