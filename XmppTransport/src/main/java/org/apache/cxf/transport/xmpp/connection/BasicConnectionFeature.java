/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.transport.xmpp.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class BasicConnectionFeature extends AbstractConnectionFeature {
    private static final Logger LOGGER = LogUtils.getLogger(BasicConnectionFeature.class);

    private XMPPConnection clientConnection;
    
    // Configuration options used to connect to XMPP server.
    private String xmppServiceName;
    private String xmppUsername;
    private String xmppPassword;
    
    /**
     * Required configuration option for connecting to the XMPP server.
     * @param xmppServiceName The full name of the XMPP server.
     */
    public void setXmppServiceName(String xmppServiceName) {
        this.xmppServiceName = xmppServiceName;
    }

    /**
     * Required configuration option for connecting to the XMPP server.
     * @param xmppUsername The username for the XMPP connection.
     */
    public void setXmppUsername(String xmppUsername) {
        this.xmppUsername = xmppUsername;
    }

    /**
     * Required configuration option for connecting to the XMPP server.
     * @param xmppPassword The password for the XMPP connection.
     */
    public void setXmppPassword(String xmppPassword) {
        this.xmppPassword = xmppPassword;
    }

    public XMPPConnectionFactory createXmppFactory(final Server server) {
        return new XMPPConnectionFactory() {
            @Override
            public XMPPConnection login() throws XMPPException {
                String resourceName = server.getEndpoint().getEndpointInfo().getName().toString();
                LOGGER.log(Level.FINE, "Creating destination connection using resource: "+resourceName);
                
                XMPPConnection xmppConnection = new XMPPConnection(xmppServiceName);
                xmppConnection.connect();
                xmppConnection.login(xmppUsername, xmppPassword, resourceName);
                
                return xmppConnection;
            }
        };
    }
    
    public XMPPConnectionFactory createXmppFactory(Client client) {
        return new XMPPConnectionFactory() {
            @Override
            public XMPPConnection login() throws XMPPException {;
                if (clientConnection == null) {
                    LOGGER.log(Level.FINE, "Creating conduit connection");
                    
                    XMPPConnection xmppConnection = new XMPPConnection(xmppServiceName);
                    xmppConnection.connect();
                    xmppConnection.login(xmppUsername, xmppPassword);
                    
                    clientConnection = xmppConnection;
                }
                
                return clientConnection;
            }
        };
    }
}
