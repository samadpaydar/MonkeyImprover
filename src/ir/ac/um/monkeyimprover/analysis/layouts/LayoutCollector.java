package ir.ac.um.monkeyimprover.analysis.layouts;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

public class LayoutCollector {
    public List<VirtualFile> getLayouts(VirtualFile layoutDirectory) {
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

}
