package org.apache.cxf.transport.xmpp.iq;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class BasicConnectionFeature extends AbstractFeature
{    
    private static final Logger LOGGER = LogUtils.getLogger(BasicConnectionFeature.class);
    
    // Configuration options used to connect to XMPP server.
    private String xmppServiceName;
    private String xmppUsername;
    private String xmppPassword;    
    
    @Override
    public void initialize(Bus bus)
    {
        XMPPConnection connection = connectToXmpp(bus.getId()+"-bus");
        
        // All connection features must store the XMPP connection
        // using this method so the connection can be found 
        // by the transport factory.
        XMPPTransportFactory.storeConnection(bus, connection);
    }
    
    @Override
    public void initialize(Server server, Bus bus)
    {
        Destination destination = server.getDestination();
        if (destination instanceof XMPPDestination)
        {
            XMPPDestination xmppDestination = (XMPPDestination)destination;
            
            // The XMPP requires a username and a resource name.
            // Use the QName of the service as the XMPP resource name.
            XMPPConnection connection = connectToXmpp(
                server.getEndpoint().getEndpointInfo().getName().toString());
            xmppDestination.setConnection(connection);
        }
        else
        {
            LOGGER.log(
                Level.WARNING, 
                "XMPP connection configured for non-XMPP destination");
        }
    }
    
    @Override
    public void initialize(Client client, Bus bus)
    {
        Conduit conduit = client.getConduit();
        if (conduit instanceof XMPPClientConduit)
        {
            XMPPClientConduit xmppConduit = (XMPPClientConduit)conduit;
            XMPPConnection connection = connectToXmpp(null);
            xmppConduit.setConnection(connection);
        }
        else
        {
            LOGGER.log(
                Level.WARNING, 
                "XMPP connection configured for non-XMPP conduit");
        }
    }
    
    /**
     * Required configuration option for connecting to the XMPP server.
     * @param xmppServiceName The full name of the XMPP server.
     */
    public void setXmppServiceName(String xmppServiceName)
    {
        this.xmppServiceName = xmppServiceName;
    }
    
    /**
     * Required configuration option for connecting to the XMPP server.
     * @param xmppUsername The username for the XMPP connection.
     */
    public void setXmppUsername(String xmppUsername)
    {
        this.xmppUsername = xmppUsername;
    }
    
    /**
     * Required configuration option for connecting to the XMPP server.
     * @param xmppPassword The password for the XMPP connection.
     */
    public void setXmppPassword(String xmppPassword)
    {
        this.xmppPassword = xmppPassword;
    }    
    
    /**
     * Creates an XMPP connection to be use by one destination or conduit.
     * 
     * @param resourceName This is the last portion of a full JID.
     * @return The XMPP connection to be used by a destination or conduit.
     * @throws IOException If the XMPP error occurs during login.
     */
    private XMPPConnection connectToXmpp(String resourceName)
    {
        XMPPConnection xmppConnection = new XMPPConnection(xmppServiceName);   
        
        try
        {
            // Login to the XMMP server using the username 
            // and password from the configuration.
            xmppConnection.connect();
            xmppConnection.login(
                    xmppUsername, 
                    xmppPassword, 
                    resourceName);
        }
        catch (XMPPException xmppError)
        {
            LOGGER.log(Level.SEVERE, "Failed to login", xmppError);
        }
        
        return xmppConnection;
    }          
}
