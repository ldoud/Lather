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

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientLifeCycleListener;
import org.apache.cxf.endpoint.ClientLifeCycleManager;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerLifeCycleListener;
import org.apache.cxf.endpoint.ServerLifeCycleManager;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;

public abstract class AbstractConnectionFeature extends AbstractFeature {
    
    private static final Logger LOGGER = LogUtils.getLogger(AbstractConnectionFeature.class);

    @Override
    public void initialize(Bus bus) {
        LOGGER.log(Level.WARNING, "This feature isn't for a bus.");
    }
    
    @Override
    public void initialize(final Server server, Bus bus) {
        Destination destination = server.getDestination();
        
        if (destination instanceof XMPPConnectionUser) {
            final XMPPConnectionUser needsConnection = (XMPPConnectionUser)destination;
            ServerLifeCycleManager mgr = bus.getExtension(ServerLifeCycleManager.class);
            
            // Initialize the XMPP connection when the destination's server starts.
            mgr.registerListener(new ServerLifeCycleListener() {
                @Override
                public void stopServer(Server stoppingServer) {
                    // Nothing
                }
                
                @Override
                public void startServer(Server startingServer) {
                    if (server == startingServer) {
                        // Create a factory that will log this destination into XMPP
                        needsConnection.initialize(createXmppFactory(server));                        
                    } 
                }
            });
        } else {
            LOGGER.log(Level.WARNING, "XMPP connection configured for non-XMPP destination");
        }
    }
    
    @Override
    public void initialize(final Client client, Bus bus) {
        Conduit conduit = client.getConduit();
        
        if (conduit instanceof XMPPConnectionUser) {
            final XMPPConnectionUser needsConnection = (XMPPConnectionUser)conduit;
            ClientLifeCycleManager mgr = bus.getExtension(ClientLifeCycleManager.class);
            
            mgr.registerListener(new ClientLifeCycleListener() {
                
                @Override
                public void clientDestroyed(Client stoppingClient) {
                    // NOthing
                }
                
                @Override
                public void clientCreated(Client startingClient) {
                    if (client == startingClient) {
                        needsConnection.initialize(createXmppFactory(client));
                    }
                }
            });
            
        } else {
            LOGGER.log(Level.WARNING, "XMPP connection configured for non-XMPP conduit");
        }
    }
    
    public abstract XMPPConnectionFactory createXmppFactory(Server server);
    
    public abstract XMPPConnectionFactory createXmppFactory(Client client);
}
