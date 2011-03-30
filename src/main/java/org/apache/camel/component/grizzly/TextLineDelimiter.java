package org.apache.camel.component.grizzly;

/**
 * Copyright Mar 26, 2011 Trustwave Holdings Inc. All Rights Reserved.
 * <p/>
 * $Id$
 */
public interface TextLineDelimiter {

    /**
     * The line delimiter constant of UNIX (<tt>"\n"</tt>)
     */
    String UNIX = "\n";

    /**
     * The line delimiter constant of MS Windows/DOS (<tt>"\r\n"</tt>)
     */
    String WINDOWS = "\r\n";

    /**
     * The line delimiter constant of Mac OS (<tt>"\r"</tt>)
     */
    String MAC = "\r";
}
