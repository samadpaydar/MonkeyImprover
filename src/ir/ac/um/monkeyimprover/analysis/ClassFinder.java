package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.utils.Utils;

public class ClassFinder {
    private MonkeyImprover monkeyImprover;

    public ClassFinder(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public VirtualFile findRelatedJavaFile(VirtualFile directory, VirtualFile layoutXMLFile) {
        VirtualFile relatedJavaFile = findRelatedJavaFileByContext(directory, layoutXMLFile);
        if (relatedJavaFile == null) {
            relatedJavaFile = findRelatedJavaFileByName(directory, layoutXMLFile);
        }
        return relatedJavaFile;
    }

    private VirtualFile findRelatedJavaFileByContext(VirtualFile directory, VirtualFile layoutXMLFile) {
        VirtualFile relatedJavaFile = null;
        LayoutAnalyzer layoutAnalyzer = new LayoutAnalyzer(this.monkeyImprover);
        String contextClassName = layoutAnalyzer.getContextClassName(layoutXMLFile);
        monkeyImprover.showMessage("contextClassName " + contextClassName);
        if(contextClassName != null) {
            relatedJavaFile = findRelatedJavaFile(directory, contextClassName);
        }
        return relatedJavaFile;
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
            } else if (child.getName().equals(relatedJavaFileName)) {
                return child;
            }
        }
        return null;
    }

}
