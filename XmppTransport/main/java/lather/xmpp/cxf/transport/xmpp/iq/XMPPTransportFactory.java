package lather.xmpp.cxf.transport.xmpp.iq;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import lather.smackx.soap.SoapProvider;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;

/**
 * Creates both XMPP destinations for servers and conduits for clients.
 * 
 * Web service providers that use one of the XMPP URI prefixes will  
 * trigger the use of this factory for creation of XMPPDestination.
 * 
 * @author Leon Doud
 */
public class XMPPTransportFactory extends AbstractTransportFactory 
    implements DestinationFactory, ConduitInitiator
{
    private static final String CLIENT_CONDUIT_XMPP_CONNECTION = "xmpp.transport.client_conduit_connection";

    public static final List<String> DEFAULT_NAMESPACES = Arrays.asList(
        "http://cxf.apache.org/transports/xmpp");
    
    // TODO Don't think this or the static initializer is necessary.
    private static final Set<String> URI_PREFIXES = new HashSet<String>();
    
    // Configuration options used to connect to XMPP server.
    private String xmppServiceName;
    private String xmppUsername;
    private String xmppPassword;
    
    static 
    {
        URI_PREFIXES.add("xmpp://");
        URI_PREFIXES.add("xmpp:");
    }  
    
    public XMPPTransportFactory() throws XMPPException
    {
        super(DEFAULT_NAMESPACES);
        
        SoapProvider xmppSoapFeature = new SoapProvider();
        
        // TODO Remove this hack and properly configure this.
        ProviderManager.getInstance().addIQProvider(
                "Envelope", 
                "http://www.w3.org/2003/05/soap-envelope", 
                xmppSoapFeature);
        ProviderManager.getInstance().addIQProvider(
                "Envelope", 
                "http://schemas.xmlsoap.org/soap/envelope/", 
                xmppSoapFeature);        
    }
    
    /**
     * Set the bus used via Spring configuration.
     */
    @Resource(name = "cxf")
    public void setBus(Bus bus) 
    {
        super.setBus(bus);
    }    
    
    /**
     * Creates a destination for a service that has its own XMPP connection.
     */
    public Destination getDestination(EndpointInfo endpointInfo) 
        throws IOException 
    {
        // The resource portion of the JID is the QName of the service.
        XMPPConnection xmppConnection = connectToXmpp(endpointInfo.getName().toString());
        return new XMPPDestination(xmppConnection, endpointInfo);
    }
    
    /**
     * Creates a conduit for a client that all share a single XMPP connection.
     * The connection is shared via the bus.
     */
    @Override
    public Conduit getConduit(EndpointInfo endpointInfo) 
        throws IOException
    {
        return getConduit(endpointInfo, endpointInfo.getTarget());
    }

    /**
     * Creates a conduit for a client that all share a single XMPP connection.
     * The connection is shared via the bus.
     */
    @Override
    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType endpointType)
            throws IOException
    {
        Bus bus = getBus();
        
        // Synchronize in case initialization is necessary.
        synchronized (bus)
        {
            // All clients share their XMPP connection via the bus.
            XMPPConnection connection = 
                (XMPPConnection)bus.getProperty(CLIENT_CONDUIT_XMPP_CONNECTION);
            
            // Initialize if necessary.
            if (connection == null || !connection.isConnected())
            {
                connection = connectToXmpp(bus.getId()+"-client");
                bus.setProperty(CLIENT_CONDUIT_XMPP_CONNECTION, connection);
            }
            
            return new XMPPClientConduit(endpointType, connection);
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
        throws IOException
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
            throw new IOException(xmppError);
        }
        
        return xmppConnection;
    }        
}
