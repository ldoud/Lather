package lather.smackx.soap;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

public class SoapProvider implements IQProvider
{

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception
    {
        StringBuilder request = new StringBuilder();

        boolean endOfSoapMsg = false;
        while (!endOfSoapMsg)
        {
            // Append the current text.
            request.append(parser.getText());
            
            // If </Envelope> then stop parsing.
            if ("Envelope".equals(parser.getName()) && 
                "http://www.w3.org/2003/05/soap-envelope".equals(parser.getNamespace()) && 
                 parser.getEventType() == XmlPullParser.END_TAG)
            {
                endOfSoapMsg = true;
            }
            else if ("Envelope".equals(parser.getName()) && 
                    "http://schemas.xmlsoap.org/soap/envelope/".equals(parser.getNamespace()) && 
                     parser.getEventType() == XmlPullParser.END_TAG)
            {
                endOfSoapMsg = true;
            }            
            // Otherwise keep parsing.
            else
            {
                parser.next();
            }
        }
        
        SoapPacket packet = new SoapPacket();
        packet.setEnvelope(request.toString());
        return packet;
    }

}
