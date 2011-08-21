package org.apache.cxf.transport.xmpp;

import org.apache.cxf.Bus;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * Initializes and cleans up the XMPP transport feature for one bus.
 * This feature only works for a bus.
 * 
 * @author Leon Doud
 */
public class XMPPFeature extends AbstractFeature
{
    private static final String BUS_CONNECTION_PROPERTY_NAME = 
        "org.apache.cxf.transport.xmpp.XMPPFeature.BUS_CONNECTION";
    
    // TODO Make these configurable.
    private String serviceName = "localhost.localdomain";
    private String username = "service1";
    private String password = "service1";
    
    // The connection that is maintained by this feature.
    private XMPPConnection xmppConnection = new XMPPConnection(serviceName);
    
//    private XMPPDestinationFactory destFactory = new XMPPDestinationFactory();
    
    /**
     * Retrieve the XMPP connection from the bus.
     * It will not be able until the bus has been initialized.
     * 
     * @param bus The bus that contains the XMPP feature.
     * @return Non-null if the bus has XMPP feature and has been initialized.
     */
    public static XMPPConnection getConnectionFromBus(Bus bus)
    {
        return (XMPPConnection)bus.getProperty(BUS_CONNECTION_PROPERTY_NAME);
    }
    
    /**
     * Connects the XMPP server and puts the connection in the bus.
     * @param bus Where the XMPP connection will be stored.
     */
    @Override
    public void initialize(final Bus bus)
    {
        try
        {
            // Log into XMPP using the bus name as the XMPP resource name.
            xmppConnection.connect();
            xmppConnection.login(username, password, (String)bus.getProperty(bus.getId()));
            
            // Make the connection available via the bus.
            bus.setProperty(BUS_CONNECTION_PROPERTY_NAME, xmppConnection);
            
            // Hook the destination factory up with the bus.
            // This will register the Destination Factory.
//            destFactory.setBus(bus);
            
            // Register to be notified for the post shutdown event of the bus.
            // After the bus is shutdown the XMPP connection will be cleaned up.
            BusLifeCycleManager mngr = bus.getExtension(BusLifeCycleManager.class);
            mngr.registerLifeCycleListener(new BusLifeCycleListener() {
                
                @Override
                public void preShutdown()
                {
                    // Not an interesting event.                    
                }
                
                @Override
                public void initComplete()
                {
                    // Not an interesting event.
                }
                
                @Override
                public void postShutdown()
                {
                    // Remove the connection from the bus.
                    bus.setProperty(BUS_CONNECTION_PROPERTY_NAME, null);
                    
                    // Logout from XMPP.
                    if(xmppConnection.isConnected())
                    {
                        xmppConnection.disconnect();
                    }
                }
            });
            
        }
        catch (XMPPException e)
        {
            // TODO Use proper logging.
            e.printStackTrace();
        }                
    }
    
    /**
     * Not supported.
     * @param interceptorProvider Not used.
     * @param bus Not used.
     */
    @Override
    public void initialize(InterceptorProvider interceptorProvider, Bus bus)
    {
        throw new UnsupportedOperationException(
            "XMPP feature is only supported for the bus");
    }
    
    /**
     * Not supported.
     * @param client Not used.
     * @param bus Not used.
     */
    @Override
    public void initialize(Client client, Bus bus)
    {
        throw new UnsupportedOperationException(
            "XMPP feature is only supported for the bus");
    }
    
    /**
     * Not supported.
     * @param server Not used.
     * @param bus Not used.
     */
    @Override
    public void initialize(Server server, Bus bus)
    {
        throw new UnsupportedOperationException(
            "XMPP feature is only supported for the bus");
    }
 
}
