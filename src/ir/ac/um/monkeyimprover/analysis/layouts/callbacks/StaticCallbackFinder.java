package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInformationExtractor;
import ir.ac.um.monkeyimprover.analysis.layouts.callbacks.CallbackFinder;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexity;
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
public class StaticCallbackFinder extends CallbackFinder {
    public StaticCallbackFinder(MonkeyImprover monkeyImprover) {
        super(monkeyImprover);
    }

    @Override
    public List<CallbackMethodInfo> getCallbackMethodInfos(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<CallbackMethodInfo> infoList = new ArrayList<>();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        List<String> callbackMethodNames = getCallbackMethodNames(xmlFile);
        if (callbackMethodNames != null && !callbackMethodNames.isEmpty()) {
            ClassFinder classFinder = new ClassFinder(monkeyImprover);
            List<VirtualFile> allJavaFiles = classFinder.getAllJavaFilesInSrcDirectory();
            List<VirtualFile> relatedJavaFiles = new ArrayList<>();
            for (VirtualFile javaFile : allJavaFiles) {
                if (isRelated(javaFile, layoutFile)) {
                    relatedJavaFiles.add(javaFile);
                }
            }
            if(relatedJavaFiles.isEmpty()) {
                relatedJavaFiles = allJavaFiles;
            }
            for (String callbackMethodName : callbackMethodNames) {
                CallbackMethodInfo info = getCallbackMethodInfo(callbackMethodName, relatedJavaFiles);
                infoList.add(info);
            }
        }
        return infoList;
    }
    public CallbackMethodInfo getCallbackMethodInfo(String callbackMethodName, List<VirtualFile> relatedJavaFiles) {
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
        return new CallbackMethodInfo(null, callbackMethodName, method, methodComplexity);
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
