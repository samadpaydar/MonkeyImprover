package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.analysis.layouts.callbacks.CallbackFinder;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexityAnalyzer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for finding Annotated Callbacks, i.e. those callbacks that are
 * declared in the Java source code using @OnClick annotation before a method. For instance:
 * @OnClick(R.id.view_id) before a method definition means that the method handles events on
 * the view with the given id
 */
public class AnnotatedCallbackFinder extends CallbackFinder {
    private MonkeyImprover monkeyImprover;

    public AnnotatedCallbackFinder(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    @Override
    public List<CallbackMethodInfo> getCallbackMethodInfos(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<CallbackMethodInfo> infoList = new ArrayList<>();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        List<String> viewIds = getViewIds(xmlFile);
        if (viewIds != null) {
            ClassFinder classFinder = new ClassFinder(monkeyImprover);
            MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
            for (String viewId : viewIds) {
                List<VirtualFile> relatedJavaFiles = classFinder.findRelatedJavaFile(projectBaseDirectory, layoutFile, viewId);
                if (relatedJavaFiles != null && !relatedJavaFiles.isEmpty()) {
                    CallbackMethodInfo info = methodComplexityAnalyzer.getCallbackMethodInfoByViewId(viewId, relatedJavaFiles);
                    if (info != null) {
                        infoList.add(info);
                    }
                }
            }
        }
        return infoList;
    }

}
