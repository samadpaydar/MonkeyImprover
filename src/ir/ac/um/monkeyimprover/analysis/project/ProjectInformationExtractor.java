package ir.ac.um.monkeyimprover.analysis.project;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import ir.ac.um.monkeyimprover.analysis.classes.JavaClassCollector;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutCollector;
import ir.ac.um.monkeyimprover.utils.Utils;

import java.util.ArrayList;
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
        List<VirtualFile> resultOnFailure = new ArrayList<>();
        VirtualFile srcDirectory = getSourceDirectory(projectBaseDirectory);
        if (srcDirectory == null) {
            Utils.showMessage("Failed to detect source directory.");
            return resultOnFailure;
        }
        Utils.showMessage("srcDirectory: " + srcDirectory.getCanonicalPath());
        VirtualFile mainDirectory = getMainDirectory(srcDirectory);
        if (mainDirectory == null) {
            Utils.showMessage("Failed to detect main directory.");
            return resultOnFailure;
        }
        Utils.showMessage("mainDirectory: " + mainDirectory.getCanonicalPath());
        VirtualFile resourcesDirectory = getResourcesDirectory(mainDirectory);
        if (resourcesDirectory == null) {
            Utils.showMessage("Failed to detect resources directory.");
            return resultOnFailure;
        }
        Utils.showMessage("resourcesDirectory: " + resourcesDirectory.getCanonicalPath());
        VirtualFile layoutDirectory = getLayoutDirectory(resourcesDirectory);
        if (layoutDirectory == null) {
            Utils.showMessage("Failed to detect layouts directory.");
            return resultOnFailure;
        }
        Utils.showMessage("layoutDirectory: " + layoutDirectory.getCanonicalPath());
        LayoutCollector layoutCollector = new LayoutCollector();
        return layoutCollector.getLayouts(layoutDirectory);
    }

    public VirtualFile getSourceDirectory(VirtualFile directory) {

        return getChildDirectory("src", directory);
    }

    private VirtualFile getMainDirectory(VirtualFile directory) {
        return getChildDirectory("main", directory);
    }

    private VirtualFile getResourcesDirectory(VirtualFile directory) {
        return getChildDirectory("res", directory);
    }

    private VirtualFile getLayoutDirectory(VirtualFile directory) {
        return getChildDirectory("layout", directory);
    }

    private boolean hasAndroidManifest(VirtualFile directory) {
        boolean result = false;
        VirtualFile[] children = directory.getChildren();
        for (VirtualFile child : children) {
            if (child.getName().equals("AndroidManifest.xml")) {
                result = true;
                break;
            }
        }
        return result;
    }

    private VirtualFile getChildDirectory(String childDirectoryName, VirtualFile parentDirectory) {
        VirtualFile result = null;
        VirtualFile[] children = parentDirectory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                if (child.getName().equals(childDirectoryName)) {
                    if (childDirectoryName.equals("src")) {
                        if (containsSubDirectories(child, "main", "androidTest")
                                || containsSubDirectories(child, "main", "test")
                                || (containsSubDirectories(child, "main") && hasAndroidManifest(child))) {
                            result = child;
                            break;
                        }
                    } else {
                        result = child;
                        break;
                    }
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

    private boolean containsSubDirectories(VirtualFile directory, String... subDirectoryNames) {
        boolean result = true;
        VirtualFile[] children = directory.getChildren();
        for (String subDirectoryName : subDirectoryNames) {
            boolean exists = false;
            for (VirtualFile child : children) {
                if (child.isDirectory() && child.getName().equals(subDirectoryName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                result = false;
                break;
            }
        }
        return result;
    }
}
