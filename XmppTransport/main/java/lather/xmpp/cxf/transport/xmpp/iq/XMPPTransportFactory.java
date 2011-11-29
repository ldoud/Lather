package lather.xmpp.cxf.transport.xmpp.iq;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
 * Web service providers or web service clients that use the 
 * XMPP transport namespace of "http://cxf.apache.org/transports/xmpp"
 * as their transport ID will trigger the use of this factory for 
 * the creation of XMPPDestination (provider) or XMPPClientConduit (client).
 * 
 * @author Leon Doud
 */
public class XMPPTransportFactory extends AbstractTransportFactory 
    implements DestinationFactory, ConduitInitiator
{
    private static final String BUS_CONDUIT_XMPP_CONNECTION = "xmpp.transport.bus_conduit_connection";

    public static final List<String> DEFAULT_NAMESPACES = Arrays.asList(
        "http://cxf.apache.org/transports/xmpp");
    
    public static void storeConnection(Bus bus, XMPPConnection connection)
    {
        bus.setProperty(BUS_CONDUIT_XMPP_CONNECTION, connection);
    }
    
    public static XMPPConnection getConnection(Bus bus)
    {
        return (XMPPConnection)bus.getProperty(BUS_CONDUIT_XMPP_CONNECTION);
    }
    
    public XMPPTransportFactory() throws XMPPException
    {
        super(DEFAULT_NAMESPACES);
        
        SoapProvider xmppSoapFeature = new SoapProvider();
        
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
        // Connection feature will configure the XMPP connection later.
        return new XMPPDestination(endpointInfo);
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
        XMPPClientConduit conduit = new XMPPClientConduit(endpointType);
        
        // If there is common share connection in the bus 
        // then setup the conduit to use it.
        Bus bus = getBus();
        XMPPConnection connection = XMPPTransportFactory.getConnection(bus);
        
        // A null connection indicates a connection feature will
        // later configure the conduit with a connection.
        if (connection != null)
        {
            conduit.setConnection(connection);
        }
        
        return conduit;
    }    
 
}
