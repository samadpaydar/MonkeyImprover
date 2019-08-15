package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInformationExtractor;
import ir.ac.um.monkeyimprover.model.InteractableView;
import ir.ac.um.monkeyimprover.model.InteractableViewComplexity;
import ir.ac.um.monkeyimprover.model.InteractableViewFinderType;
import ir.ac.um.monkeyimprover.model.MethodComplexity;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexityAnalyzer;
import ir.ac.um.monkeyimprover.analysis.methods.MethodFinder;
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
 * This class is responsible for finding Static Callbacks, i.e. those callbacks that are
 * directly declared in the XML layout files. For instance:
 * <Button .... onClick="exit" ....></Button>
 */
public class StaticInteractableViewFinder extends InteractableViewFinder {
    public StaticInteractableViewFinder(MonkeyImprover monkeyImprover) {
        super(monkeyImprover);
    }

    @Override
    public List<InteractableViewComplexity> getInteractableViewInfo(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<InteractableViewComplexity> infoList = new ArrayList<>();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        //List<String> callbackMethodNames = getCallbackMethodNames(xmlFile);
        List<String> viewIds = getViewIds(xmlFile);
        if (viewIds != null && !viewIds.isEmpty()) {
            for (String viewId : viewIds) {
                String callbackMethodName = getCallbackMethodNameForView(xmlFile, viewId);
                if (callbackMethodName != null) {
                    ClassFinder classFinder = new ClassFinder(monkeyImprover);
                    List<VirtualFile> allJavaFiles = classFinder.getAllJavaFilesInSrcDirectory();
                    List<VirtualFile> relatedJavaFiles = new ArrayList<>();
                    for (VirtualFile javaFile : allJavaFiles) {
                        if (isRelated(javaFile, layoutFile)) {
                            relatedJavaFiles.add(javaFile);
                        }
                    }
                    if (relatedJavaFiles.isEmpty()) {
                        relatedJavaFiles = allJavaFiles;
                    }
                    InteractableViewComplexity info = getInteractableView(callbackMethodName, relatedJavaFiles, viewId);
                    infoList.add(info);
                }
            }
        }
        return infoList;
    }

    private InteractableViewComplexity getInteractableView(String callbackMethodName, List<VirtualFile> relatedJavaFiles, String viewId) {
        MethodComplexity methodComplexity = null;
        PsiMethod method = null;
        MethodFinder methodFinder = new MethodFinder();
        MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
        for (VirtualFile relatedJavaFile : relatedJavaFiles) {
            PsiFile file = PsiManager.getInstance(monkeyImprover.getProject()).findFile(relatedJavaFile);
            if (file != null && file instanceof PsiJavaFile) {
                PsiMethod relatedMethod = methodFinder.findMethodByName((PsiJavaFile) file, callbackMethodName);
                if (relatedMethod != null) {
                    method = relatedMethod;
                    methodComplexity = methodComplexityAnalyzer.getComplexity(relatedMethod, true);
                    break;
                }
            }
        }
        return new InteractableViewComplexity(
                new InteractableView(viewId, callbackMethodName, method, InteractableViewFinderType.STATIC_FINDER), methodComplexity);
    }

    private boolean isRelated(VirtualFile javaFile, VirtualFile layoutXMLFile) {
        return isRelatedByContext(javaFile, layoutXMLFile) || isRelatedByName(javaFile, layoutXMLFile);
    }

    private boolean isRelatedByContext(VirtualFile javaFile, VirtualFile layoutXMLFile) {
        boolean result = false;
        LayoutInformationExtractor layoutInformationExtractor = new LayoutInformationExtractor(monkeyImprover);
        File xmlFile = new File(layoutXMLFile.getCanonicalPath());
        List<String> contextClassNames = layoutInformationExtractor.getContextClassNames(xmlFile);
        if (contextClassNames != null && !contextClassNames.isEmpty()) {
            for (String contextClassName : contextClassNames) {
                String childName = javaFile.getNameWithoutExtension();
                String childPath = createParentHierarchy(javaFile);
                String packageName = getPackageName(contextClassName);
                String name = getName(contextClassName);
                if (name != null && name.equals(childName)
                        && packageName != null && childPath.endsWith(packageName)) {
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean isRelatedByName(VirtualFile javaFile, VirtualFile layoutXMLFile) {
        String layoutFileName = layoutXMLFile.getName();
        layoutFileName = layoutFileName.substring(0, layoutFileName.lastIndexOf('.'));
        String[] parts = layoutFileName.split("_");
        String relatedJavaFileName = "";
        for (String part : parts) {
            relatedJavaFileName = Utils.capitalize(part) + relatedJavaFileName;
        }
        relatedJavaFileName += ".java";
        return relatedJavaFileName.equals(javaFile.getName());
    }

    private String createParentHierarchy(VirtualFile child) {
        VirtualFile parent = child.getParent();
        if (parent != null) {
            return createParentHierarchy(parent) + "." + parent.getName();
        } else {
            return "";
        }
    }

    private String getName(String javaFileName) {
        String name = null;
        int index = javaFileName.lastIndexOf('.');
        if (index != -1) {
            name = javaFileName.substring(index + 1);
        }
        return name;
    }


    private String getPackageName(String javaFileName) {
        String packageName = null;
        int index = javaFileName.lastIndexOf('.');
        if (index != -1) {
            packageName = javaFileName.substring(0, index);
        }
        return packageName;
    }

    private String getCallbackMethodNameForView(File xmlFile, String viewId) {
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                final List<String> callbackMethodNames = new ArrayList<>();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(xmlFile, new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName,
                                             String qName, Attributes attributes) throws SAXException {
                        String tempViewId = null;
                        String tempCallbackMethodName = null;
                        for (int i = 0; i < attributes.getLength(); i++) {
                            String attributeQualifiedName = attributes.getQName(i);
                            if (attributeQualifiedName != null
                                    && attributeQualifiedName.toLowerCase()
                                    .equalsIgnoreCase("android:id")) {
                                String temp = attributes.getValue(i);
                                int index = temp.lastIndexOf('/');
                                if (index != -1) {
                                    temp = temp.substring(index + 1).trim();
                                }
                                tempViewId = temp;
                            } else if (attributeQualifiedName != null
                                    && attributeQualifiedName.toLowerCase()
                                    .equalsIgnoreCase("android:onclick")) {
                                tempCallbackMethodName = attributes.getValue(i);
                            }
                        }
                        if (tempViewId != null && tempViewId.equals(viewId)) {
                            callbackMethodNames.add(tempCallbackMethodName);
                        }
                    }
                });
                return callbackMethodNames.get(0);
            } catch (Exception e) {
                Utils.showException(e);
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
                Utils.showException(e);
                e.printStackTrace();
            }
        }
        return null;
    }

}
