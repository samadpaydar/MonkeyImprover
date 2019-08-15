package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;
import ir.ac.um.monkeyimprover.model.InteractableViewComplexity;
import ir.ac.um.monkeyimprover.utils.Utils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This interface defines the behavior of a InteractableViewFinder, a component which is responsible for
 * finding Views and their associated callback method or accessor method (e.g. a method that uses findViewById to access a view)
 */
public abstract class InteractableViewFinder {
    protected MonkeyImprover monkeyImprover;

    public InteractableViewFinder(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public abstract List<InteractableViewComplexity> getInteractableViewInfo(VirtualFile projectBaseDirectory, VirtualFile layoutFile);

    protected List<String> getViewIds(File xmlFile) {
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                final List<String> viewIds = new ArrayList<>();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(xmlFile, new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName,
                                             String qName, Attributes attributes) throws SAXException {
                        for (int i = 0; i < attributes.getLength(); i++) {
                            String attributeQualifiedName = attributes.getQName(i);
                            if (attributeQualifiedName != null
                                    && attributeQualifiedName.toLowerCase()
                                    .equalsIgnoreCase("android:id")) {
                                String viewId = attributes.getValue(i);
                                //it is needed to remove prefixes android:id="@+id/btn_modulo"
                                int index = viewId.lastIndexOf('/');
                                if (index != -1) {
                                    viewId = viewId.substring(index + 1).trim();
                                }
                                viewIds.add(viewId);
                            }
                        }
                    }
                });
                return viewIds;
            } catch (Exception e) {
                Utils.showException(e);
                e.printStackTrace();
            }
        }
        return null;
    }

    protected List<String> getInputViewIds(File xmlFile) {
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                final List<String> viewIds = new ArrayList<>();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(xmlFile, new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName,
                                             String qName, Attributes attributes) throws SAXException {
                        if (AnalysisUtils.isAnAndroidInputView(qName)) {
                            for (int i = 0; i < attributes.getLength(); i++) {
                                String attributeQualifiedName = attributes.getQName(i);
                                if (attributeQualifiedName != null
                                        && attributeQualifiedName.toLowerCase()
                                        .equalsIgnoreCase("android:id")) {
                                    String viewId = attributes.getValue(i);
                                    //it is needed to remove prefixes android:id="@+id/btn_modulo"
                                    int index = viewId.lastIndexOf('/');
                                    if (index != -1) {
                                        viewId = viewId.substring(index + 1).trim();
                                    }
                                    viewIds.add(viewId);
                                }
                            }
                        }
                    }
                });
                return viewIds;
            } catch (Exception e) {
                Utils.showException(e);
                e.printStackTrace();
            }
        }
        return null;
    }
}
