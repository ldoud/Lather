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

package org.apache.cxf.transport.xmpp.pep;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.xmpp.connection.XMPPConnectionFactory;
import org.apache.cxf.transport.xmpp.connection.XMPPTransportFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.PEPListener;
import org.jivesoftware.smackx.PEPManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.PEPEvent;
import org.jivesoftware.smackx.provider.PEPProvider;
import org.jivesoftware.smackx.pubsub.EventElement;

/**
 * Creates both XMPP destinations for servers and conduits for clients. Web service providers or web service
 * clients that use the XMPP transport namespace of "http://cxf.apache.org/transports/xmpp" as their transport
 * ID will trigger the use of this factory for the creation of XMPPDestination (provider) or XMPPClientConduit
 * (client).
 * 
 * @author Leon Doud
 */
public class PEPTransportFactory extends AbstractTransportFactory implements DestinationFactory,
    ConduitInitiator, XMPPTransportFactory {

    public static final List<String> DEFAULT_NAMESPACES = Arrays
        .asList("http://cxf.apache.org/transports/xmpp/pep");

    private XMPPConnectionFactory destinationConnectionFactory;
    private XMPPConnectionFactory conduitConnectionFactory;
    
    private PEPSoapProvider soapProvider = new PEPSoapProvider();
    private PEPProvider pepProvider = new PEPProvider();

    public PEPTransportFactory() throws XMPPException {
        super();
        setTransportIds(DEFAULT_NAMESPACES);
       
//        ProviderManager.getInstance().addExtensionProvider(
//            "event", "http://jabber.org/protocol/pubsub#event", pepProvider);
    }

    /**
     * Set the bus used via Spring configuration.
     */
    @Resource(name = "cxf")
    public void setBus(Bus bus) {
        super.setBus(bus);
    }

    /**
     * Creates a destination for a service that receives XMPP messages.
     */
    public Destination getDestination(EndpointInfo endpointInfo) throws IOException { 
        // The node name is the full name of the service.
        final String nodeName = endpointInfo.getService().getName().toString();
        pepProvider.registerPEPParserExtension(nodeName, soapProvider);
       
        PEPDestination dest = new PEPDestination(endpointInfo);       
        
        try {            
//            XMPPConnection conn = destinationConnectionFactory.login(endpointInfo);
            XMPPConnection conn = new XMPPConnection("localhost");
            conn.connect();
            
            // Advertise interest in receiving information.
            ServiceDiscoveryManager disco = ServiceDiscoveryManager.getInstanceFor(conn);
            disco.addFeature(nodeName+"+notify");            
            
            // Create destination.
            dest.setXmppConnection(conn);

            conn.addPacketListener(new PacketListener() {
                
                @Override
                public void processPacket(Packet p) {
                    
                    Message msg = (Message)p;
                    try {
                        EventElement event = (EventElement)msg.getExtension(
                            "event", "http://jabber.org/protocol/pubsub#event");
                        
                        String eventNode = event.getEvent().getNode();
                        if(nodeName.equals(event.getEvent().getNode())) {
                            System.out.println("event packet: "+event.toXML());
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            },
            new PacketExtensionFilter("event", "http://jabber.org/protocol/pubsub#event"));
            
            conn.login("user1", "user1", nodeName);

        } catch (XMPPException e) {
            throw new IOException(e);
        }

        return dest;
    }

    /**
     * Creates a conduit for a client that all share a single XMPP connection. The connection is shared via
     * the bus.
     */
    @Override
    public Conduit getConduit(EndpointInfo endpointInfo) throws IOException {
        return getConduit(endpointInfo, endpointInfo.getTarget());
    }

    /**
     * Creates a conduit for a client that all share a single XMPP connection. The connection is shared via
     * the bus.
     */
    @Override
    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType endpointType)
        throws IOException {
        
        String nodeName = endpointInfo.getInterface().getName().toString();
        pepProvider.registerPEPParserExtension(nodeName, soapProvider);

        try {
            XMPPConnection conn = conduitConnectionFactory.login(endpointInfo);
                        
            PEPManager mgr = new PEPManager(conn);
            PEPClientConduit conduit = new PEPClientConduit(endpointType, mgr, nodeName);
            conduit.setXmppConnection(conn);
            
            mgr.addPEPListener(new PEPListener() {
                
                @Override
                public void eventReceived(String arg0, PEPEvent event) {
                    System.out.println("Client received: "+event.toXML());
                }
            });
            
            return conduit;

        } catch (XMPPException e) {
            throw new IOException(e);
        }

        
    }

    @Override
    public void setDestinationConnectionFactory(XMPPConnectionFactory factory) {
        destinationConnectionFactory = factory;
    }

    @Override
    public void setConduitConnectionFactory(XMPPConnectionFactory factory) {
        conduitConnectionFactory = factory;
    }
}
