package ir.ac.um.monkeyimprover.analysis.layouts;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;

import java.util.List;

/**
 * @author Samad Paydar
 */
public class LayoutInfo {
    private VirtualFile layoutFile;
    private List<CallbackMethodInfo> callbackMethodInfoList;

    public LayoutInfo(VirtualFile layoutFile, List<CallbackMethodInfo> callbackMethodInfoList) {
        this.setLayoutFile(layoutFile);
        this.setCallbackMethodInfoList(callbackMethodInfoList);
    }

    public VirtualFile getLayoutFile() {
        return layoutFile;
    }

    public void setLayoutFile(VirtualFile layoutFile) {
        this.layoutFile = layoutFile;
    }

    public List<CallbackMethodInfo> getCallbackMethodInfoList() {
        return callbackMethodInfoList;
    }

    public void setCallbackMethodInfoList(List<CallbackMethodInfo> callbackMethodInfoList) {
        this.callbackMethodInfoList = callbackMethodInfoList;
    }
}
