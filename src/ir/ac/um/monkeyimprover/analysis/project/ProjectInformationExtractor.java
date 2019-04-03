package ir.ac.um.monkeyimprover.analysis.project;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import ir.ac.um.monkeyimprover.analysis.classes.JavaClassCollector;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutCollector;

import java.util.List;

public class ProjectInformationExtractor {
    private PsiElement projectElement;

    public ProjectInformationExtractor(PsiElement projectElement) {
        this.projectElement = projectElement;
    }

    public List<PsiClass> getProjectJavaClasses() {
        JavaClassCollector javaClassCollector = new JavaClassCollector();
        projectElement.accept(javaClassCollector);
        return javaClassCollector.getJavaClasses();
    }

    public List<VirtualFile> getLayoutXMLFiles(VirtualFile projectBaseDirectory) {
        VirtualFile srcDirectory = getSourceDirectory(projectBaseDirectory);
        VirtualFile resourcesDirectory = getResourcesDirectory(srcDirectory);
        VirtualFile layoutDirectory = getLayoutDirectory(resourcesDirectory);
        LayoutCollector layoutCollector = new LayoutCollector();
        return layoutCollector.getLayouts(layoutDirectory);
    }

    private VirtualFile getSourceDirectory(VirtualFile directory) {
        return getChildDirectory("src", directory);
    }

    private VirtualFile getResourcesDirectory(VirtualFile directory) {
        return getChildDirectory("res", directory);
    }

    private VirtualFile getLayoutDirectory(VirtualFile directory) {
        return getChildDirectory("layouts", directory);
    }

    private VirtualFile getChildDirectory(String childDirectoryName, VirtualFile parentDirectory) {
        VirtualFile result = null;
        VirtualFile[] children = parentDirectory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                if (child.getName().equals(childDirectoryName)) {
                    result = child;
                    break;
                } else {
                    VirtualFile temp = getChildDirectory(childDirectoryName, child);
                    if (temp != null) {
                        result = temp;
                        break;
                    }
                }
            }
        }
        return result;
    }

}
