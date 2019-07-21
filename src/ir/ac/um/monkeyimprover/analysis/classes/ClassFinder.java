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


    public List<VirtualFile> getAllJavaFilesInSrcDirectory() {
        return getAllJavaFiles(monkeyImprover.getSourceDirectory());
    }

    private List<VirtualFile> getAllJavaFiles(VirtualFile directory) {
        VirtualFile[] children = directory.getChildren();
        List<VirtualFile> javaFiles = new ArrayList<>();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                List<VirtualFile> innerJavaFiles = getAllJavaFiles(child);
                javaFiles.addAll(innerJavaFiles);
            } else {
                String childName = child.getName();
                if(childName.endsWith(".java")) {
                    javaFiles.add(child);
                }
            }
        }
        return javaFiles;
    }

}
