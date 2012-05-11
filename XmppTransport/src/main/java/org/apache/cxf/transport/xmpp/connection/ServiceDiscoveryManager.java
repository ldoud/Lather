/**
 * $RCSfile$
 * $Revision: 12580 $
 * $Date: 2011-08-17 23:44:41 -0400 (Wed, 17 Aug 2011) $
 *
 * Copyright 2003-2007 Jive Software.
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cxf.transport.xmpp.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.NodeInformationProvider;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.packet.DiscoverItems;

/*
 * This file is modified from the original.
 * The original is org.jivesoftware.smackx.ServiceDiscoveryManager 
 */

/**
 * Manages discovery of services in XMPP entities. This class provides:
 * <ol>
 * <li>A registry of supported features in this XMPP entity.
 * <li>Automatic response when this XMPP entity is queried for information.
 * <li>Ability to discover items and information of remote XMPP entities.
 * <li>Ability to publish publicly available items.
 * </ol>  
 * 
 * @author Gaston Dombiak
 */
public class ServiceDiscoveryManager {

    // Values used to construct the default identity.
    // Left default scope so it be accessed by unit tests.
    static String DEFAULT_IDENTITY_NAME = "Smack";
    static String DEFAULT_IDENTITY_TYPE = "pc";
    static String DEFAULT_IDENTITY_CATEGORY = "client";
    
    private static Map<Connection, ServiceDiscoveryManager> instances =
            new ConcurrentHashMap<Connection, ServiceDiscoveryManager>();

    // A set is used to avoid duplicate identities.
    // The member variable is marked volatile because the "setIdentity"
    // methods create a new CopyOnWriteArraySet.
    private volatile Collection<DiscoverInfo.Identity> identities = 
        new CopyOnWriteArraySet<DiscoverInfo.Identity>();
    
    private Connection connection;
    private final List<String> features = new ArrayList<String>();
    private DataForm extendedInfo = null;
    private Map<String, NodeInformationProvider> nodeInformationProviders =
            new ConcurrentHashMap<String, NodeInformationProvider>();

    // Create a new ServiceDiscoveryManager on every established connection
    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(final Connection connection) {
                getInstanceFor(connection);
                
                // Add a listener to the connection that removes the registered instance when
                // the connection is closed
                connection.addConnectionListener(new ConnectionListener() {
                    public void connectionClosed() {
                        // Unregister this instance since the connection has been closed
                        instances.remove(connection);
                    }

                    public void connectionClosedOnError(Exception e) {
                        // ignore
                    }

                    public void reconnectionFailed(Exception e) {
                        // ignore
                    }

                    public void reconnectingIn(int seconds) {
                        // ignore
                    }

                    public void reconnectionSuccessful() {
                        // ignore
                    }
                });
            }
        });
    }

    /**
     * Creates a new ServiceDiscoveryManager for a given Connection. This means that the 
     * service manager will respond to any service discovery request that the connection may
     * receive. 
     * 
     * @param connection the connection to which a ServiceDiscoveryManager is going to be created.
     */
    private ServiceDiscoveryManager(Connection connection) {
        this.connection = connection;
        
        // Using this as the default identity as previously it was static
        // data for all instances of this class.
        Identity defaultIdentity = 
            new Identity(DEFAULT_IDENTITY_CATEGORY, 
                                      DEFAULT_IDENTITY_NAME);
        defaultIdentity.setType(DEFAULT_IDENTITY_TYPE);
        
        identities.add(defaultIdentity);
    }

    /**
     * Returns the ServiceDiscoveryManager instance associated with a given Connection.
     * If the instance doesn't exist one is created.
     * 
     * @param connection the connection used to look for the proper ServiceDiscoveryManager.
     * @return the ServiceDiscoveryManager associated with a given Connection.
     */
    public synchronized static ServiceDiscoveryManager getInstanceFor(Connection connection) {
        ServiceDiscoveryManager mgr = instances.get(connection);
        
        if (mgr == null) {
            mgr = new ServiceDiscoveryManager(connection);
            
            // This avoids publishing references to 
            // "this" from within the constructor.
            mgr.init();
            
            // Register the new instance and associate it with the connection 
            instances.put(connection, mgr);
        }
        
        return mgr;
    }

    /**
     * Initializes the packet listeners of the connection that will answer to any
     * service discovery request. 
     */
    private void init() {
        // Listen for disco#items requests and answer with an empty result        
        PacketFilter packetFilter = new PacketTypeFilter(DiscoverItems.class);
        PacketListener packetListener = new PacketListener() {
            public void processPacket(Packet packet) {
                DiscoverItems discoverItems = (DiscoverItems) packet;
                // Send back the items defined in the client if the request is of type GET
                if (discoverItems != null && discoverItems.getType() == IQ.Type.GET) {
                    DiscoverItems response = new DiscoverItems();
                    response.setType(IQ.Type.RESULT);
                    response.setTo(discoverItems.getFrom());
                    response.setPacketID(discoverItems.getPacketID());
                    response.setNode(discoverItems.getNode());

                    // Add the defined items related to the requested node. Look for 
                    // the NodeInformationProvider associated with the requested node.  
                    NodeInformationProvider nodeInformationProvider =
                            getNodeInformationProvider(discoverItems.getNode());
                    if (nodeInformationProvider != null) {
                        // Specified node was found
                        List<DiscoverItems.Item> items = nodeInformationProvider.getNodeItems();
                        if (items != null) {
                            for (DiscoverItems.Item item : items) {
                                response.addItem(item);
                            }
                        }
                    } else if(discoverItems.getNode() != null) {
                        // Return <item-not-found/> error since client doesn't contain
                        // the specified node
                        response.setType(IQ.Type.ERROR);
                        response.setError(new XMPPError(XMPPError.Condition.item_not_found));
                    }
                    connection.sendPacket(response);
                }
            }
        };
        connection.addPacketListener(packetListener, packetFilter);

        // Listen for disco#info requests and answer the client's supported features 
        // To add a new feature as supported use the #addFeature message        
        packetFilter = new PacketTypeFilter(DiscoverInfo.class);
        packetListener = new PacketListener() {
            public void processPacket(Packet packet) {
                DiscoverInfo discoverInfo = (DiscoverInfo) packet;
                // Answer the client's supported features if the request is of the GET type
                if (discoverInfo != null && discoverInfo.getType() == IQ.Type.GET) {
                    DiscoverInfo response = new DiscoverInfo();
                    response.setType(IQ.Type.RESULT);
                    response.setTo(discoverInfo.getFrom());
                    response.setPacketID(discoverInfo.getPacketID());
                    response.setNode(discoverInfo.getNode());
                     // Add the client's identity and features only if "node" is null
                    if (discoverInfo.getNode() == null) {
                        // Set this client identity
                        // A thread safe collection is used to avoid 
                        // synchronization.
                        for(Iterator<DiscoverInfo.Identity> it = getIdentities(); it.hasNext();) {
                            response.addIdentity(it.next());
                        }
                        
                        // Add the registered features to the response
                        synchronized (features) {
                            for (Iterator<String> it = getFeatures(); it.hasNext();) {
                                response.addFeature(it.next());
                            }
                            if (extendedInfo != null) {
                                response.addExtension(extendedInfo);
                            }
                        }
                    }
                    else {
                        // Disco#info was sent to a node. Check if we have information of the
                        // specified node
                        NodeInformationProvider nodeInformationProvider =
                                getNodeInformationProvider(discoverInfo.getNode());
                        if (nodeInformationProvider != null) {
                            // Node was found. Add node features
                            List<String> features = nodeInformationProvider.getNodeFeatures();
                            if (features != null) {
                                for(String feature : features) {
                                    response.addFeature(feature);
                                }
                            }
                            // Add node identities
                            List<DiscoverInfo.Identity> identities =
                                    nodeInformationProvider.getNodeIdentities();
                            if (identities != null) {
                                for (DiscoverInfo.Identity identity : identities) {
                                    response.addIdentity(identity);
                                }
                            }
                        }
                        else {
                            // Return <item-not-found/> error since specified node was not found
                            response.setType(IQ.Type.ERROR);
                            response.setError(new XMPPError(XMPPError.Condition.item_not_found));
                        }
                    }
                    connection.sendPacket(response);
                }
            }
        };
        connection.addPacketListener(packetListener, packetFilter);
    }

    /**
     * Returns the NodeInformationProvider responsible for providing information 
     * (ie items) related to a given node or <tt>null</null> if none.<p>
     * 
     * In MUC, a node could be 'http://jabber.org/protocol/muc#rooms' which means that the
     * NodeInformationProvider will provide information about the rooms where the user has joined.
     * 
     * @param node the node that contains items associated with an entity not addressable as a JID.
     * @return the NodeInformationProvider responsible for providing information related 
     * to a given node.
     */
    private NodeInformationProvider getNodeInformationProvider(String node) {
        if (node == null) {
            return null;
        }
        return nodeInformationProviders.get(node);
    }

    /**
     * Sets the NodeInformationProvider responsible for providing information 
     * (ie items) related to a given node. Every time this client receives a disco request
     * regarding the items of a given node, the provider associated to that node will be the 
     * responsible for providing the requested information.<p>
     * 
     * In MUC, a node could be 'http://jabber.org/protocol/muc#rooms' which means that the
     * NodeInformationProvider will provide information about the rooms where the user has joined. 
     * 
     * @param node the node whose items will be provided by the NodeInformationProvider.
     * @param listener the NodeInformationProvider responsible for providing items related
     *      to the node.
     */
    public void setNodeInformationProvider(String node, NodeInformationProvider listener) {
        nodeInformationProviders.put(node, listener);
    }

    /**
     * Removes the NodeInformationProvider responsible for providing information 
     * (ie items) related to a given node. This means that no more information will be
     * available for the specified node.
     * 
     * In MUC, a node could be 'http://jabber.org/protocol/muc#rooms' which means that the
     * NodeInformationProvider will provide information about the rooms where the user has joined. 
     * 
     * @param node the node to remove the associated NodeInformationProvider.
     */
    public void removeNodeInformationProvider(String node) {
        nodeInformationProviders.remove(node);
    }

    /**
     * Returns the supported features by this XMPP entity.
     * 
     * @return an Iterator on the supported features by this XMPP entity.
     */
    public Iterator<String> getFeatures() {
        synchronized (features) {
            return Collections.unmodifiableList(new ArrayList<String>(features)).iterator();
        }
    }
    
    /**
     * The identities of this XMPP entity.
     * 
     * @return Unmodifiable iterator over the collection of identities.
     */
    public Iterator<DiscoverInfo.Identity> getIdentities() {
        return Collections.unmodifiableList(
                  new ArrayList<DiscoverInfo.Identity>(identities)).iterator();
    }
    
    /**
     * Adds a single identity to this XMPP entity.
     * 
     * @param additionalIdentity New identity for this entity.
     */
    public void addIdentity(DiscoverInfo.Identity additionalIdentity) {
        identities.add(additionalIdentity);
    }
    
    /**
     * Adds many identities to this XMPP entity.
     * 
     * @param moreIds All of these identities are added to this entity.
     */
    public void addIdentities(Collection<DiscoverInfo.Identity> moreIds) {
        identities.addAll(moreIds);
    }
    
    /**
     * Clears existing identities and uses this new identity.
     * 
     * @param newIdentity The new identity of this XMPP entity.
     */
    public void setIdentity(DiscoverInfo.Identity newIdentity) {
        CopyOnWriteArraySet<DiscoverInfo.Identity> newIdSet = 
            new CopyOnWriteArraySet<DiscoverInfo.Identity>();
        
        newIdSet.add(newIdentity);
        identities = newIdSet;
    }
    
    /**
     * Clears existing identities and uses these new identities.
     * 
     * @param moreIds The new identities of this XMPP entity.
     */
    public void setIdentities(Collection<DiscoverInfo.Identity> moreIds) {
        CopyOnWriteArraySet<DiscoverInfo.Identity> newIdSet = 
            new CopyOnWriteArraySet<DiscoverInfo.Identity>();
        
        newIdSet.addAll(moreIds);
        identities = newIdSet;
    }

    /**
     * Registers that a new feature is supported by this XMPP entity. When this client is 
     * queried for its information the registered features will be answered.<p>
     *
     * Since no packet is actually sent to the server it is safe to perform this operation
     * before logging to the server. In fact, you may want to configure the supported features
     * before logging to the server so that the information is already available if it is required
     * upon login.
     *
     * @param feature the feature to register as supported.
     */
    public void addFeature(String feature) {
        synchronized (features) {
            features.add(feature);
        }
    }

    /**
     * Removes the specified feature from the supported features by this XMPP entity.<p>
     *
     * Since no packet is actually sent to the server it is safe to perform this operation
     * before logging to the server.
     *
     * @param feature the feature to remove from the supported features.
     */
    public void removeFeature(String feature) {
        synchronized (features) {
            features.remove(feature);
        }
    }

    /**
     * Returns true if the specified feature is registered in the ServiceDiscoveryManager.
     *
     * @param feature the feature to look for.
     * @return a boolean indicating if the specified featured is registered or not.
     */
    public boolean includesFeature(String feature) {
        synchronized (features) {
            return features.contains(feature);
        }
    }

    /**
     * Registers extended discovery information of this XMPP entity. When this
     * client is queried for its information this data form will be returned as
     * specified by XEP-0128.
     * <p>
     *
     * Since no packet is actually sent to the server it is safe to perform this
     * operation before logging to the server. In fact, you may want to
     * configure the extended info before logging to the server so that the
     * information is already available if it is required upon login.
     *
     * @param info
     *            the data form that contains the extend service discovery
     *            information.
     */
    public void setExtendedInfo(DataForm info) {
      extendedInfo = info;
    }

    /**
     * Removes the dataform containing extended service discovery information
     * from the information returned by this XMPP entity.<p>
     *
     * Since no packet is actually sent to the server it is safe to perform this
     * operation before logging to the server.
     */
    public void removeExtendedInfo() {
       extendedInfo = null;
    }

    /**
     * Returns the discovered information of a given XMPP entity addressed by its JID.
     * 
     * @param entityID the address of the XMPP entity.
     * @return the discovered information.
     * @throws XMPPException if the operation failed for some reason.
     */
    public DiscoverInfo discoverInfo(String entityID) throws XMPPException {
        return discoverInfo(entityID, null);
    }

    /**
     * Returns the discovered information of a given XMPP entity addressed by its JID and
     * note attribute. Use this message only when trying to query information which is not 
     * directly addressable.
     * 
     * @param entityID the address of the XMPP entity.
     * @param node the attribute that supplements the 'jid' attribute.
     * @return the discovered information.
     * @throws XMPPException if the operation failed for some reason.
     */
    public DiscoverInfo discoverInfo(String entityID, String node) throws XMPPException {
        // Discover the entity's info
        DiscoverInfo disco = new DiscoverInfo();
        disco.setType(IQ.Type.GET);
        disco.setTo(entityID);
        disco.setNode(node);

        // Create a packet collector to listen for a response.
        PacketCollector collector =
            connection.createPacketCollector(new PacketIDFilter(disco.getPacketID()));

        connection.sendPacket(disco);

        // Wait up to 5 seconds for a result.
        IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        // Stop queuing results
        collector.cancel();
        if (result == null) {
            throw new XMPPException("No response from the server.");
        }
        if (result.getType() == IQ.Type.ERROR) {
            throw new XMPPException(result.getError());
        }
        return (DiscoverInfo) result;
    }

    /**
     * Returns the discovered items of a given XMPP entity addressed by its JID.
     * 
     * @param entityID the address of the XMPP entity.
     * @return the discovered information.
     * @throws XMPPException if the operation failed for some reason.
     */
    public DiscoverItems discoverItems(String entityID) throws XMPPException {
        return discoverItems(entityID, null);
    }

    /**
     * Returns the discovered items of a given XMPP entity addressed by its JID and
     * note attribute. Use this message only when trying to query information which is not 
     * directly addressable.
     * 
     * @param entityID the address of the XMPP entity.
     * @param node the attribute that supplements the 'jid' attribute.
     * @return the discovered items.
     * @throws XMPPException if the operation failed for some reason.
     */
    public DiscoverItems discoverItems(String entityID, String node) throws XMPPException {
        // Discover the entity's items
        DiscoverItems disco = new DiscoverItems();
        disco.setType(IQ.Type.GET);
        disco.setTo(entityID);
        disco.setNode(node);

        // Create a packet collector to listen for a response.
        PacketCollector collector =
            connection.createPacketCollector(new PacketIDFilter(disco.getPacketID()));

        connection.sendPacket(disco);

        // Wait up to 5 seconds for a result.
        IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        // Stop queuing results
        collector.cancel();
        if (result == null) {
            throw new XMPPException("No response from the server.");
        }
        if (result.getType() == IQ.Type.ERROR) {
            throw new XMPPException(result.getError());
        }
        return (DiscoverItems) result;
    }

    /**
     * Returns true if the server supports publishing of items. A client may wish to publish items
     * to the server so that the server can provide items associated to the client. These items will
     * be returned by the server whenever the server receives a disco request targeted to the bare
     * address of the client (i.e. user@host.com).
     * 
     * @param entityID the address of the XMPP entity.
     * @return true if the server supports publishing of items.
     * @throws XMPPException if the operation failed for some reason.
     */
    public boolean canPublishItems(String entityID) throws XMPPException {
        DiscoverInfo info = discoverInfo(entityID);
        return info.containsFeature("http://jabber.org/protocol/disco#publish");
    }

    /**
     * Publishes new items to a parent entity. The item elements to publish MUST have at least 
     * a 'jid' attribute specifying the Entity ID of the item, and an action attribute which 
     * specifies the action being taken for that item. Possible action values are: "update" and 
     * "remove".
     * 
     * @param entityID the address of the XMPP entity.
     * @param discoverItems the DiscoveryItems to publish.
     * @throws XMPPException if the operation failed for some reason.
     */
    public void publishItems(String entityID, DiscoverItems discoverItems)
            throws XMPPException {
        publishItems(entityID, null, discoverItems);
    }

    /**
     * Publishes new items to a parent entity and node. The item elements to publish MUST have at 
     * least a 'jid' attribute specifying the Entity ID of the item, and an action attribute which 
     * specifies the action being taken for that item. Possible action values are: "update" and 
     * "remove".
     * 
     * @param entityID the address of the XMPP entity.
     * @param node the attribute that supplements the 'jid' attribute.
     * @param discoverItems the DiscoveryItems to publish.
     * @throws XMPPException if the operation failed for some reason.
     */
    public void publishItems(String entityID, String node, DiscoverItems discoverItems)
            throws XMPPException {
        discoverItems.setType(IQ.Type.SET);
        discoverItems.setTo(entityID);
        discoverItems.setNode(node);

        // Create a packet collector to listen for a response.
        PacketCollector collector =
            connection.createPacketCollector(new PacketIDFilter(discoverItems.getPacketID()));

        connection.sendPacket(discoverItems);

        // Wait up to 5 seconds for a result.
        IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        // Stop queuing results
        collector.cancel();
        if (result == null) {
            throw new XMPPException("No response from the server.");
        }
        if (result.getType() == IQ.Type.ERROR) {
            throw new XMPPException(result.getError());
        }
    }
}