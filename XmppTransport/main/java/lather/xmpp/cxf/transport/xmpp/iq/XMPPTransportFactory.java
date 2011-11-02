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
    public static final List<String> DEFAULT_NAMESPACES = Arrays.asList(
        "http://cxf.apache.org/transports/xmpp");
    
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
        
        // TODO Remove this hack and properly configure this.
        ProviderManager.getInstance().addIQProvider(
                "Envelope", 
                "http://www.w3.org/2003/05/soap-envelope", 
                new SoapProvider());
    }
    
    @Resource(name = "cxf")
    public void setBus(Bus bus) 
    {
        super.setBus(bus);
    }    
    
    /**
     * {@inheritDoc}
     */
    public Destination getDestination(EndpointInfo endpointInfo) 
        throws IOException 
    {
        // The resource portion of the JID is the QName of the service.
        XMPPConnection xmppConnection = connectToXmpp(endpointInfo.getName().toString());
        System.out.println("Destination logged in as:"+xmppConnection.getUser());
        return new XMPPDestination(xmppConnection, endpointInfo);
    }

    @Override
    public Conduit getConduit(EndpointInfo endpointInfo) 
        throws IOException
    {
        return getConduit(endpointInfo, endpointInfo.getTarget());
    }

    @Override
    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType endpointType)
            throws IOException
    {
        XMPPConnection xmppConnection = connectToXmpp(endpointInfo.getName().toString());
        System.out.println("Destination logged in as:"+xmppConnection.getUser());
        return new XMPPClientConduit(endpointInfo, endpointType, xmppConnection);
    }    
    
    public void setXmppServiceName(String xmppServiceName)
    {
        this.xmppServiceName = xmppServiceName;
    }
    
    public void setXmppUsername(String xmppUsername)
    {
        this.xmppUsername = xmppUsername;
    }
    
    public void setXmppPassword(String xmppPassword)
    {
        this.xmppPassword = xmppPassword;
    }    
    
    private XMPPConnection connectToXmpp(String resourceName)
        throws IOException
    {
        XMPPConnection xmppConnection = new XMPPConnection(xmppServiceName);   
        
        try
        {
            // Login to the XMMP server using the username and password from the configuration.
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
