package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class LayoutRefactory {
    private MonkeyImprover monkeyImprover;

    public LayoutRefactory(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public void refactorLayout(LayoutInfo layoutInfo) {
        VirtualFile layoutFile = layoutInfo.getLayoutFile();
        List<CallbackMethodInfo> callbackMethodInfoList = layoutInfo.getCallbackMethodInfoList();
        String path = layoutFile.getCanonicalPath();
        File xmlFile = new File(path);
        if (xmlFile.exists() && xmlFile.isFile()) {
            LayoutXMLHandlerForRefactory handler = new LayoutXMLHandlerForRefactory(monkeyImprover, xmlFile);
            handler.run();
        }
    }
}
