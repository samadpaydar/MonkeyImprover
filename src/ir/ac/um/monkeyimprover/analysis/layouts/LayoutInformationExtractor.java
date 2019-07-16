package ir.ac.um.monkeyimprover.analysis.layouts;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexityAnalyzer;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;
import ir.ac.um.monkeyimprover.utils.Utils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LayoutInformationExtractor {
    private MonkeyImprover monkeyImprover;

    public LayoutInformationExtractor(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public List<String> getContextClassNames(File xmlFile) {
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                List<String> contextClassNames = new ArrayList<>();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(xmlFile, new DefaultHandler() {
                    private boolean rootElementVisited = false;

                    @Override
                    public void startElement(String uri, String localName,
                                             String qName, Attributes attributes) throws SAXException {
                        if (!rootElementVisited) {
                            rootElementVisited = true;
                            for (int i = 0; i < attributes.getLength(); i++) {
                                String attributeQualifiedName = attributes.getQName(i);
                                if (attributeQualifiedName != null
                                        && attributeQualifiedName.toLowerCase().equalsIgnoreCase("tools:context")) {
                                    contextClassNames.add(attributes.getValue(i));
                                }
                            }
                        }
                    }
                });
                return contextClassNames;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
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

    public List<CallbackMethodInfo> getCallbackMethodInfos(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<CallbackMethodInfo> list1 = getCallbackMethodInfosByMethod1(projectBaseDirectory, layoutFile);
        List<CallbackMethodInfo> list2 = getCallbackMethodInfosByMethod2(projectBaseDirectory, layoutFile);
        list1.addAll(list2);
        return list1;
    }

    private List<String> getViewIds(File xmlFile) {
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
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * This method collects information about the onclick event handlers by
     * searching for @OnClick(R.id.view_id) in the java file that is mentioned
     * as the context of the layout file
     *
     * @param projectBaseDirectory
     * @param layoutFile
     * @return
     */
    private List<CallbackMethodInfo> getCallbackMethodInfosByMethod2(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<CallbackMethodInfo> infoList = new ArrayList<>();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        List<String> viewIds = getViewIds(xmlFile);
        if (viewIds != null) {
            ClassFinder classFinder = new ClassFinder(monkeyImprover);
            MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
            for (String viewId : viewIds) {
                Utils.showMessage("\t\t\t\tViewID: " + viewId);
                List<VirtualFile> relatedJavaFiles = classFinder.findRelatedJavaFile(projectBaseDirectory, layoutFile, viewId);
                if (relatedJavaFiles != null && !relatedJavaFiles.isEmpty()) {
                    CallbackMethodInfo info = methodComplexityAnalyzer.getCallbackMethodInfoByViewId(viewId, relatedJavaFiles);
                    if (info != null) {
                        infoList.add(info);
                    }
                }
            }
        }
        return infoList;
    }

    /**
     * This method collects information about onclick event handlers by
     * searching for onclick attribute of the view elements in the layout file
     *
     * @param projectBaseDirectory
     * @param layoutFile
     * @return
     */
    private List<CallbackMethodInfo> getCallbackMethodInfosByMethod1(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<CallbackMethodInfo> infoList = new ArrayList<>();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        List<String> callbackMethodNames = getCallbackMethodNames(xmlFile);
        MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
        if (callbackMethodNames != null && !callbackMethodNames.isEmpty()) {
            ClassFinder classFinder = new ClassFinder(monkeyImprover);
            List<VirtualFile> relatedJavaFiles = classFinder.findRelatedJavaFile(projectBaseDirectory, layoutFile,null);
            if (relatedJavaFiles != null && !relatedJavaFiles.isEmpty()) {
                for (String callbackMethodName : callbackMethodNames) {
                    CallbackMethodInfo info = methodComplexityAnalyzer.getCallbackMethodInfo(callbackMethodName, relatedJavaFiles);
                    infoList.add(info);
                }
            }
        }
        return infoList;
    }

}