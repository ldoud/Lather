package org.apache.cxf.transport.xmpp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Destination;
import org.jivesoftware.smack.XMPPConnection;

/**
 * Automatically registers itself as an XMPPDestination producer with the Bus.
 * 
 * Web service providers that use one of the XMPP URI prefixes will  
 * trigger the use of this factory for creation of XMPPDestination.
 * 
 * @author Leon Doud
 */
@NoJSR250Annotations(unlessNull = { "bus" })
public class XMPPDestinationFactory extends AbstractTransportFactory
{
    public static final List<String> DEFAULT_NAMESPACES = Arrays.asList(
        "http://cxf.apache.org/transports/xmpp");
    
    private static final Set<String> URI_PREFIXES = new HashSet<String>();
    
    static 
    {
        URI_PREFIXES.add("xmpp://");
        URI_PREFIXES.add("xmpp:");
    }  
    
    public XMPPDestinationFactory()
    {
        super(DEFAULT_NAMESPACES);
    }
    
    @Resource(name = "cxf")
    public void setBus(Bus bus) 
    {
        super.setBus(bus);
    }    
    
    /**
     * {@inheritDoc}
     */
    public Destination getDestination(EndpointInfo endpointInfo) throws IOException 
    {
        XMPPConnection xmpp = XMPPFeature.getConnectionFromBus(getBus());
        return new XMPPDestination(xmpp, endpointInfo);
    }    
}
