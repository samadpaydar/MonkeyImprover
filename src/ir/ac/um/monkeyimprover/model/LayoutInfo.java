package ir.ac.um.monkeyimprover.model;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.model.InteractableViewComplexity;

import java.util.List;

/**
 * @author Samad Paydar
 */
public class LayoutInfo {
    private VirtualFile layoutFile;
    private List<InteractableViewComplexity> interactableViewList;

    public LayoutInfo(VirtualFile layoutFile, List<InteractableViewComplexity> interactableViewList) {
        this.setLayoutFile(layoutFile);
        this.setInteractableViewList(interactableViewList);
    }

    public VirtualFile getLayoutFile() {
        return layoutFile;
    }

    public void setLayoutFile(VirtualFile layoutFile) {
        this.layoutFile = layoutFile;
    }

    public List<InteractableViewComplexity> getInteractableViewList() {
        return interactableViewList;
    }

    public void setInteractableViewList(List<InteractableViewComplexity> interactableViewList) {
        this.interactableViewList = interactableViewList;
    }
}
