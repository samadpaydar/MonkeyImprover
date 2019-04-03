package ir.ac.um.monkeyimprover.analysis.project;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

public class ProjectInformationExtractor {

    public List<VirtualFile> getLayoutXMLFiles(VirtualFile projectBaseDirectory) {
        VirtualFile srcDirectory = getSourceDirectory(projectBaseDirectory);
        VirtualFile resourcesDirectory = getResourcesDirectory(srcDirectory);
        VirtualFile layoutDirectory = getLayoutDirectory(resourcesDirectory);
        return getLayouts(layoutDirectory);
    }

    private List<VirtualFile> getLayouts(VirtualFile layoutDirectory) {
        List<VirtualFile> layoutFiles = new ArrayList<>();
        VirtualFile[] children = layoutDirectory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                List<VirtualFile> innerLayoutFiles = getLayouts(child);
                layoutFiles.addAll(innerLayoutFiles);
            } else if (child.getName().toLowerCase().endsWith(".xml")) {
                layoutFiles.add(child);
            }
        }
        return layoutFiles;
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
