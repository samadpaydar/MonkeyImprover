package ir.ac.um.monkeyimprover.analysis.layouts;

import ir.ac.um.monkeyimprover.analysis.AnalysisUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LayoutInformationExtractor {

    public List<String> getContextClassNames(File xmlFile) {
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                List<String> contextClassNames = new ArrayList<>();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                return contextClassNames;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<String> getCallbackMethodNames(File xmlFile) {
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                final List<String> callbackMethodNames = new ArrayList<>();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(xmlFile, new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName,
                                             String qName, Attributes attributes) throws SAXException {
                        for (int i = 0; i < attributes.getLength(); i++) {
                            String attributeQualifiedName = attributes.getQName(i);
                            if (attributeQualifiedName != null && attributeQualifiedName.toLowerCase().equalsIgnoreCase("android:onclick")) {
                                callbackMethodNames.add(attributes.getValue(i));
                            }
                        }
                    }
                });
                return callbackMethodNames;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getNumberOfViews(File xmlFile) {
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                class ViewCounterHandler extends DefaultHandler {
                    private int count = 0;

                    @Override
                    public void startElement(String uri, String localName,
                                             String qName, Attributes attributes) throws SAXException {
                        if (AnalysisUtils.isAnAndroidView(qName)) {
                            count++;
                        }
                    }
                }

                ViewCounterHandler handler = new ViewCounterHandler();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(xmlFile, handler);
                return handler.count;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public String[] getRootLayoutInfo(File xmlFile) {
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                final String[] info = new String[2];
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(xmlFile, new DefaultHandler() {
                    private boolean rootElementVisited = false;

                    @Override
                    public void startElement(String uri, String localName,
                                             String qName, Attributes attributes) throws SAXException {
                        if (!rootElementVisited) {
                            rootElementVisited = true;
                            info[0] = attributes.getValue("android:id");
                            info[1] = attributes.getValue("tools:context");
                        }
                    }
                });
                return info;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isFragment(File xmlFile) {
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                class FragmentCheckHandler extends DefaultHandler {
                    private boolean isFragment = false;
                    private boolean rootElementVisited = false;

                    @Override
                    public void startElement(String uri, String localName,
                                             String qName, Attributes attributes) throws SAXException {
                        if (!rootElementVisited) {
                            rootElementVisited = true;
                            isFragment = AnalysisUtils.isAnAndroidView(qName);
                        }
                    }
                }
                FragmentCheckHandler handler = new FragmentCheckHandler();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(xmlFile, handler);
                return handler.isFragment;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}