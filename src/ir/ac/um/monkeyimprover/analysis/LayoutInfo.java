package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;

public class LayoutInfo {
    private VirtualFile layoutFile;
    private List<CallbackMethodInfo> callbackMethodInfoList;

    public LayoutInfo(VirtualFile layoutFile) {
        this.layoutFile = layoutFile;
        callbackMethodInfoList = new ArrayList<>();
    }

    public void addCallbackMethodInfo(CallbackMethodInfo info) {
        callbackMethodInfoList.add(info);
    }

}
