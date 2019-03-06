package ir.ac.um.monkeyimprover.analysis;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class LayoutXMLFileHandler extends DefaultHandler {
    private List<String> callbackMethodNames;
    private MonkeyImprover monkeyImprover;
    private String context;

    public LayoutXMLFileHandler(MonkeyImprover monkeyImprover) {
        callbackMethodNames = new ArrayList<>();
        this.monkeyImprover = monkeyImprover;
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName,
                             String qName, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            String attributeQualifiedName = attributes.getQName(i);
            if (attributeQualifiedName != null && attributeQualifiedName.toLowerCase().equalsIgnoreCase("android:onclick")) {
                callbackMethodNames.add(attributes.getValue(i));
            } else if (attributeQualifiedName != null && attributeQualifiedName.toLowerCase().equalsIgnoreCase("tools:context")) {
                context = attributes.getValue(i);
            }

        }
    }

    @Override
    public void characters(char[] chars, int offset, int length) {
    }

    public List<String> getCallbackMethodNames() {
        return callbackMethodNames;
    }

    public String getContext() {
        return this.context;
    }

}
