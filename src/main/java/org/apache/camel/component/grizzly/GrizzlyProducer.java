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

import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.ServicePoolAware;
import org.apache.camel.Exchange;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.converter.IOConverter;
import org.apache.camel.processor.CamelLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glassfish.grizzly.nio.transport.TCPNIOServerConnection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.Connection;

import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Component for Grizzly.
 *
 * @version 1.0
 */
public class GrizzlyProducer extends DefaultProducer implements ServicePoolAware {
    private static final Logger LOG = LoggerFactory.getLogger(GrizzlyProducer.class);
    private final boolean lazySessionCreation;
    private final long timeout;
    private final boolean sync;
    private final CamelLogger noReplyLogger;
    private Connection connection;
    private TCPNIOTransport connector;

    public GrizzlyProducer(final GrizzlyEndpoint grizzlyEndpoint) {
        super(grizzlyEndpoint);
        this.lazySessionCreation = grizzlyEndpoint.getConfiguration().isLazySessionCreation();
        this.timeout = grizzlyEndpoint.getConfiguration().getTimeout();
        this.sync = grizzlyEndpoint.getConfiguration().isSync();
        this.noReplyLogger = new CamelLogger(LOG, grizzlyEndpoint.getConfiguration().getNoReplyLogLevel());
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        if (connection == null && !lazySessionCreation) {
            throw new IllegalStateException("Not started yet!");
        }
        
        if (connection == null || !connection.isOpen()) {
            connector = getEndpoint().getConnector();
            connector.start();
            
            Future<Connection> future = connector.connect(getEndpoint().getAddress());
            connection = future.get(10, TimeUnit.SECONDS);
            assert connection != null;
        }

        if (getEndpoint().getConfiguration().getEncoding() != null) {
            exchange.setProperty(Exchange.CHARSET_NAME, IOConverter.normalizeCharset(
                    Charset.forName(getEndpoint().getConfiguration().getEncoding()).name()));
        }

        Object body = GrizzlyPayloadHelper.getIn(getEndpoint(), exchange);
        if (body == null) {
            noReplyLogger.log("No payload to send for exchange: " + exchange);
            return; // exit early since nothing to write
        }

        if (getEndpoint().getConfiguration().isTextline()) {
            body = getEndpoint().getCamelContext().getTypeConverter().mandatoryConvertTo(String.class, exchange, body);
        }

        if (sync) {
            // only initialize latch if we should get a response
            // latch = new CountDownLatch(1);
            // reset handler if we expect a response
            //ResponseHandler handler = (ResponseHandler)
            //handler.reset();
        }

        if (LOG.isDebugEnabled()) {
            Object out = body;
            if (body instanceof byte[]) {
                // byte arrays is not readable so convert to string
                out = exchange.getContext().getTypeConverter().convertTo(String.class, body);
            }
            LOG.debug("Writing body : " + out);
        }

        GrizzlyHelper.writeBody(connection, body, exchange);

        Boolean close;
        if (ExchangeHelper.isOutCapable(exchange)) {
            close = exchange.getOut().getHeader(GrizzlyConstants.GRIZZLY_CLOSE_SESSION_WHEN_COMPLETE, Boolean.class);
        } else {
            close = exchange.getIn().getHeader(GrizzlyConstants.GRIZZLY_CLOSE_SESSION_WHEN_COMPLETE, Boolean.class);
        }


        boolean disconnect = getEndpoint().getConfiguration().isDisconnect();
        if (close != null) {
            disconnect = close;
        }
        if (disconnect) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Closing session when complete at address: " + getEndpoint().getAddress());
            }
            connector.stop();
        }
        
    }

    @Override
    public GrizzlyEndpoint getEndpoint() {
        return (GrizzlyEndpoint) super.getEndpoint();
    }

    @Override
    public boolean isSingleton() {
        //Todo examine this falacy
        return false;
    }
}
