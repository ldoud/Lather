package lather.smackx.provider;

import org.jivesoftware.smack.packet.IQ;

public class SoapPacket extends IQ
{
    private String soapEnvelope;
    
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
