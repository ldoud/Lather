package org.apache.cxf.transport.xmpp.pubsub;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.xmpp.common.XMPPConnectionUser;
import org.apache.cxf.transport.xmpp.iq.IQClientConduit;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

public class PubSubServiceNameNode extends AbstractFeature {
    
    private static final Logger LOGGER = LogUtils.getLogger(IQClientConduit.class);
    
    private boolean createIfMissing;
    
    public void setCreateIfMissing(boolean create) {
        createIfMissing = create;
    }
   
    @Override
    public void initialize(Bus bus) {
        // Doesn't work on a bus
        LOGGER.log(Level.WARNING, "This feature isn't for a bus");
    }
    
    @Override
    public void initialize(Server server, Bus bus) {
        Destination dest = server.getDestination();
        
        if (dest instanceof XMPPConnectionUser && dest instanceof ItemEventListener<?>) {
            String serviceName = server.getEndpoint().getEndpointInfo().getName().toString();
            XMPPConnectionUser connUser = (XMPPConnectionUser)dest;
            XMPPConnection connection = connUser.getXmppConnection();
            
            Node pubSubNode = findOrCreateNode(serviceName, connection);
            
            if (pubSubNode != null) {
                findOrCreateSubscription((ItemEventListener<?>)dest, serviceName, connection, pubSubNode);
            }
        }
        else {
            LOGGER.log(Level.WARNING, "This feature is only for PubSubDestinations");
        }
    }
    
    
    @Override
    public void initialize(Client client, Bus bus) {
        // TODO Auto-generated method stub
        super.initialize(client, bus);
    }

    private void findOrCreateSubscription(ItemEventListener<?> listener, String serviceName, XMPPConnection connection,
                                          Node pubSubNode) {
        try {
            List<Subscription> subscriptions = pubSubNode.getSubscriptions();
            boolean alreadySubscribed = false;
            for(Iterator<Subscription> i = subscriptions.iterator(); i.hasNext() && !alreadySubscribed;) {
                Subscription sub = i.next();
                alreadySubscribed = sub.getJid().equals(connection.getUser());
            }
            
            if (!alreadySubscribed) {
                pubSubNode.addItemEventListener(listener);
                try {
                    pubSubNode.subscribe(connection.getUser());
                } catch (XMPPException failedToSub) {
                    LOGGER.log(Level.SEVERE, "JID: "+connection.getUser()+ " to node: "+serviceName);
                }
            }
            
        } catch (XMPPException failedToGetSubscriptions) {
           LOGGER.log(Level.SEVERE, "Failed to find subscriptions for: "+serviceName);
        }
    }

    private Node findOrCreateNode(String serviceName, XMPPConnection connection) {
        PubSubManager mgr = new PubSubManager(connection);
        Node pubSubNode = null;
        try {
            pubSubNode = mgr.getNode(serviceName);
        } catch (XMPPException failedToFindNode) {
            if (createIfMissing) {
                try {
                    pubSubNode = mgr.createNode(serviceName);
                } catch (XMPPException failedToCreateNode) {
                    LOGGER.log(Level.SEVERE, "Failed to create node: "+ serviceName, failedToCreateNode);
                }
            }
            else {
                LOGGER.log(Level.WARNING, "Not creating node that wasn't found: "+serviceName);
            }
        }
        return pubSubNode;
    }

}
