package lather.smackx.soap;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

public class SoapProvider implements IQProvider
{

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception
    {
        return null;
    }

}
