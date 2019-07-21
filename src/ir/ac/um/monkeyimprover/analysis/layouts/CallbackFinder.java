package ir.ac.um.monkeyimprover.analysis.layouts;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;

import java.util.List;

/**
 * This interface defines the behavior of a CallbackFinder, a component which is responsible for
 * finding methods that handle UI events, e.g. an onClick handler of a Button
 */
public interface CallbackFinder {
    List<CallbackMethodInfo> getCallbackMethodInfos(VirtualFile projectBaseDirectory, VirtualFile layoutFile);
}
