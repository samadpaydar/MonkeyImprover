package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexityAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for finding Dynamic Callbacks, i.e. those callbacks that are
 * set in the Java source files. For instance:
 * view.setOnClickListener(new OnClickListener() {...})
 */
public class DynamicCallbackFinder implements CallbackFinder {
    private MonkeyImprover monkeyImprover;

    public DynamicCallbackFinder(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    @Override
    public List<CallbackMethodInfo> getCallbackMethodInfos(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<CallbackMethodInfo> infoList = new ArrayList<>();
        return infoList;
    }
}
