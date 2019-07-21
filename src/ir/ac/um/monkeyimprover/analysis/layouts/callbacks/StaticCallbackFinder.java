package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.analysis.layouts.callbacks.CallbackFinder;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexityAnalyzer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for finding Static Callbacks, i.e. those callbacks that are
 * directly declared in the XML layout files. For instance:
 * <Button .... onClick="exit" ....></Button>
 */
public class StaticCallbackFinder extends CallbackFinder {
    public StaticCallbackFinder(MonkeyImprover monkeyImprover) {
        super(monkeyImprover);
    }

    @Override
    public List<CallbackMethodInfo> getCallbackMethodInfos(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<CallbackMethodInfo> infoList = new ArrayList<>();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        List<String> callbackMethodNames = getCallbackMethodNames(xmlFile);
        MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
        if (callbackMethodNames != null && !callbackMethodNames.isEmpty()) {
            ClassFinder classFinder = new ClassFinder(monkeyImprover);
            List<VirtualFile> relatedJavaFiles = classFinder.findRelatedJavaFile(projectBaseDirectory, layoutFile, null);
            if (relatedJavaFiles != null && !relatedJavaFiles.isEmpty()) {
                for (String callbackMethodName : callbackMethodNames) {
                    CallbackMethodInfo info = methodComplexityAnalyzer.getCallbackMethodInfo(callbackMethodName, relatedJavaFiles);
                    infoList.add(info);
                }
            }
        }
        return infoList;
    }

    private List<String> getCallbackMethodNames(File xmlFile) {
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
                            if (attributeQualifiedName != null
                                    && attributeQualifiedName.toLowerCase()
                                    .equalsIgnoreCase("android:onclick")) {
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

}
