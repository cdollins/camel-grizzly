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

import org.apache.camel.LoggingLevel;
import org.apache.camel.RuntimeCamelException;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.Codec;

import java.util.List;

/**
 * Component for Grizzly.
 *
 * @version 1.0
 */
public class GrizzlyConfiguration implements Cloneable {
    private String protocol;
    private String host;
    private int port;
    private boolean sync = true;
    private boolean textline;
    private String textlineDelimiter;
    private String encoding;
    private long timeout = 30000;
    private boolean lazySessionCreation = true;
    private boolean transferExchange;
    private boolean grizzlyLogger;
    private int encoderMaxLineLength = -1;
    private int decoderMaxLineLength = -1;
    private boolean allowDefaultCodec = true;
    private Codec codec;
    private boolean disconnect;
    private boolean disconnectOnNoReply = true;
    private LoggingLevel noReplyLogLevel = LoggingLevel.WARN;
    private List<Filter> filters;


    public GrizzlyConfiguration copy() {
        try {
            return (GrizzlyConfiguration) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeCamelException(e);
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(final boolean sync) {
        this.sync = sync;
    }

    public boolean isTextline() {
        return textline;
    }

    public void setTextline(final boolean textline) {
        this.textline = textline;
    }

    public String getTextlineDelimiter() {
        return textlineDelimiter;
    }

    public void setTextlineDelimiter(final String textlineDelimiter) {
        this.textlineDelimiter = textlineDelimiter;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    public boolean isLazySessionCreation() {
        return lazySessionCreation;
    }

    public void setLazySessionCreation(final boolean lazySessionCreation) {
        this.lazySessionCreation = lazySessionCreation;
    }

    public boolean isTransferExchange() {
        return transferExchange;
    }

    public void setTransferExchange(final boolean transferExchange) {
        this.transferExchange = transferExchange;
    }

    public boolean isGrizzlyLogger() {
        return grizzlyLogger;
    }

    public void setGrizzlyLogger(final boolean grizzlyLogger) {
        this.grizzlyLogger = grizzlyLogger;
    }

    public int getEncoderMaxLineLength() {
        return encoderMaxLineLength;
    }

    public void setEncoderMaxLineLength(final int encoderMaxLineLength) {
        this.encoderMaxLineLength = encoderMaxLineLength;
    }

    public int getDecoderMaxLineLength() {
        return decoderMaxLineLength;
    }

    public void setDecoderMaxLineLength(final int decoderMaxLineLength) {
        this.decoderMaxLineLength = decoderMaxLineLength;
    }

    public boolean isAllowDefaultCodec() {
        return allowDefaultCodec;
    }

    public void setAllowDefaultCodec(final boolean allowDefaultCodec) {
        this.allowDefaultCodec = allowDefaultCodec;
    }

    public boolean isDisconnect() {
        return disconnect;
    }

    public void setDisconnect(final boolean disconnect) {
        this.disconnect = disconnect;
    }

    public boolean isDisconnectOnNoReply() {
        return disconnectOnNoReply;
    }

    public void setDisconnectOnNoReply(final boolean disconnectOnNoReply) {
        this.disconnectOnNoReply = disconnectOnNoReply;
    }

    public LoggingLevel getNoReplyLogLevel() {
        return noReplyLogLevel;
    }

    public void setNoReplyLogLevel(final LoggingLevel noReplyLogLevel) {
        this.noReplyLogLevel = noReplyLogLevel;
    }

    public void setFilters(final List<Filter> filters) {
        this.filters = filters;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public Codec getCodec() {
        return codec;
    }

    public void setCodec(Codec codec) {
        this.codec = codec;
    }
}
