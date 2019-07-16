package ir.ac.um.monkeyimprover.analysis.classes;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInformationExtractor;
import ir.ac.um.monkeyimprover.analysis.methods.MethodFinder;
import ir.ac.um.monkeyimprover.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class ClassFinder {

    private MonkeyImprover monkeyImprover;

    public ClassFinder(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public List<VirtualFile> findRelatedJavaFile(VirtualFile directory, VirtualFile layoutXMLFile, String viewId) {
        List<VirtualFile> relatedJavaFiles = findRelatedJavaFileByContext(directory, layoutXMLFile);
        if (relatedJavaFiles.isEmpty()) {
            VirtualFile temp = findRelatedJavaFileByName(directory, layoutXMLFile);
            if (temp != null) {
                relatedJavaFiles.add(temp);
            }
        }
        if (relatedJavaFiles.isEmpty() && viewId != null) {
            VirtualFile temp = findRelatedJavaFileByAnnotatedViewId(directory, viewId);
            if (temp != null) {
                relatedJavaFiles.add(temp);
            }
        }
        return relatedJavaFiles;
    }

    private List<VirtualFile> findRelatedJavaFileByContext(VirtualFile directory, VirtualFile layoutXMLFile) {
        List<VirtualFile> relatedJavaFiles = new ArrayList<>();
        LayoutInformationExtractor layoutInformationExtractor = new LayoutInformationExtractor(monkeyImprover);
        File xmlFile = new File(layoutXMLFile.getCanonicalPath());
        List<String> contextClassNames = layoutInformationExtractor.getContextClassNames(xmlFile);
        if (contextClassNames != null && !contextClassNames.isEmpty()) {
            relatedJavaFiles = new ArrayList<>();
            for (String contextClassName : contextClassNames) {
                VirtualFile temp = findRelatedJavaFile(directory, contextClassName);
                if (temp != null) {
                    relatedJavaFiles.add(temp);
                }
            }
        }
        return relatedJavaFiles;
    }

    private VirtualFile findRelatedJavaFileByAnnotatedViewId(VirtualFile directoryOrFile, String viewId) {
        if (directoryOrFile.isDirectory()) {
            VirtualFile[] children = directoryOrFile.getChildren();
            for (VirtualFile child : children) {
                VirtualFile temp = findRelatedJavaFileByAnnotatedViewId(child, viewId);
                if (temp != null) {
                    return temp;
                }
            }
        } else if (containsAnnotationForView(directoryOrFile, viewId)) {
            return directoryOrFile;
        }
        return null;
    }

    private boolean containsAnnotationForView(VirtualFile file, String viewId) {
        boolean result = false;
        if (file.getName().endsWith(".java")) {
            PsiFile javaFile = PsiManager.getInstance(monkeyImprover.getProject()).findFile(file);
            if (javaFile != null && javaFile instanceof PsiJavaFile) {
                MethodFinder methodFinder = new MethodFinder();
                PsiMethod relatedMethod = methodFinder.findMethodByOnClickAnnotation((PsiJavaFile) javaFile, viewId);
                if (relatedMethod != null) {
                    result = true;
                }
            }
        }
        return result;
    }

    private VirtualFile findRelatedJavaFileByName(VirtualFile directory, VirtualFile layoutXMLFile) {
        String layoutFileName = layoutXMLFile.getName();
        layoutFileName = layoutFileName.substring(0, layoutFileName.lastIndexOf('.'));
        String[] parts = layoutFileName.split("_");
        String relatedJavaFileName = "";
        for (String part : parts) {
            relatedJavaFileName = Utils.capitalize(part) + relatedJavaFileName;
        }
        relatedJavaFileName += ".java";
        return findRelatedJavaFile(directory, relatedJavaFileName);
    }

    private VirtualFile findRelatedJavaFile(VirtualFile directory, String relatedJavaFileName) {
        VirtualFile[] children = directory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                VirtualFile file = findRelatedJavaFile(child, relatedJavaFileName);
                if (file != null) {
                    return file;
                }
            } else {
                String childName = child.getNameWithoutExtension();
                String childPath = createParentHierarchy(child);
                String packageName = getPackageName(relatedJavaFileName);
                String name = getName(relatedJavaFileName);
                if (name != null && name.equals(childName) && packageName != null && childPath.endsWith(packageName)) {
                    return child;
                }
            }
        }
        return null;
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
}
