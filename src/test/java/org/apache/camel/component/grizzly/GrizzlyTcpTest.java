package org.apache.camel.component.grizzly;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Copyright Mar 30, 2011 Trustwave Holdings Inc. All Rights Reserved.
 * <p/>
 * $Id$
 */
public class GrizzlyTcpTest extends ContextTestSupport {
    protected String uri = "grizzly:tcp://localhost:6123?sync=false";

    public void testGrizzlyRoute() throws Exception {
        MockEndpoint endpoint = getMockEndpoint("mock:result");
        Object body = "Hello there!";
        endpoint.expectedBodiesReceived(body);

        template.sendBodyAndHeader(uri, body, "cheese", 123);

        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from(uri).to("log:before?showAll=true").to("mock:result").to("log:after?showAll=true");
            }
        };
    }
}
