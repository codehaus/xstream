package com.thoughtworks.xstream.io.xml.xppdom;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Xpp3DomBuilder {
    public static Xpp3Dom build(Reader reader)
            throws Exception {
        List elements = new ArrayList();

        List values = new ArrayList();

        Xpp3Dom configuration = null;

        XmlPullParser parser = new MXParser();

        parser.setInput(reader);

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String rawName = parser.getName();

                Xpp3Dom childConfiguration = createConfiguration(rawName);

                int depth = elements.size();

                if (depth > 0) {
                    Xpp3Dom parent = (Xpp3Dom) elements.get(depth - 1);

                    parent.addChild(childConfiguration);
                }

                elements.add(childConfiguration);

                values.add(new StringBuffer());

                int attributesSize = parser.getAttributeCount();

                for (int i = 0; i < attributesSize; i++) {
                    String name = parser.getAttributeName(i);

                    String value = parser.getAttributeValue(i);

                    childConfiguration.setAttribute(name, value);
                }
            } else if (eventType == XmlPullParser.TEXT) {
                int depth = values.size() - 1;

                StringBuffer valueBuffer = (StringBuffer) values.get(depth);

                valueBuffer.append(parser.getText());
            } else if (eventType == XmlPullParser.END_TAG) {
                int depth = elements.size() - 1;

                Xpp3Dom finishedConfiguration = (Xpp3Dom) elements.remove(depth);

                String accumulatedValue = (values.remove(depth)).toString();

                if (finishedConfiguration.getChildCount() == 0) {
                    String finishedValue;

                    if (0 == accumulatedValue.length()) {
                        finishedValue = null;
                    } else {
                        finishedValue = accumulatedValue;
                    }

                    finishedConfiguration.setValue(finishedValue);
                }

                if (0 == depth) {
                    configuration = finishedConfiguration;
                }
            }

            eventType = parser.next();
        }

        reader.close();

        return configuration;
    }

    private static Xpp3Dom createConfiguration(String localName) {
        return new Xpp3Dom(localName);
    }
}