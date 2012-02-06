package org.apache.cxf.transport.xmpp.smackx.soap;

import org.jivesoftware.smack.packet.IQ;

public class SoapPacket extends IQ
{
    private String soapEnvelope;
    
    @Override
    public String getXmlns()
    {
        return "http://www.w3.org/2003/05/soap-envelope";
    }
    
    public void setEnvelope(String soapEnvelope)
    {
        this.soapEnvelope = soapEnvelope;
    }
    
    @Override
    public String getChildElementXML()
    {
        return soapEnvelope;
    }

}
