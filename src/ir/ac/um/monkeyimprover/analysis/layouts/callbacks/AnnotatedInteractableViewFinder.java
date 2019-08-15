package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.model.InteractableView;
import ir.ac.um.monkeyimprover.model.InteractableViewComplexity;
import ir.ac.um.monkeyimprover.model.InteractableViewFinderType;
import ir.ac.um.monkeyimprover.model.MethodComplexity;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexityAnalyzer;
import ir.ac.um.monkeyimprover.analysis.methods.MethodFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for finding Annotated Callbacks, i.e. those callbacks that are
 * declared in the Java source code using @OnClick annotation before a method. For instance:
 *
 * @OnClick(R.id.view_id) before a method definition means that the method handles events on
 * the view with the given id
 */
public class AnnotatedInteractableViewFinder extends InteractableViewFinder {
    public AnnotatedInteractableViewFinder(MonkeyImprover monkeyImprover) {
        super(monkeyImprover);
    }

    @Override
    public List<InteractableViewComplexity> getInteractableViewInfo(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<InteractableViewComplexity> infoList = new ArrayList<>();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        List<String> viewIds = getViewIds(xmlFile);
        if (viewIds != null) {
            ClassFinder classFinder = new ClassFinder(monkeyImprover);
            List<VirtualFile> allJavaFiles = classFinder.getAllJavaFilesInSrcDirectory();
            for (String viewId : viewIds) {
                List<VirtualFile> relatedJavaFiles = findRelatedJavaFile(allJavaFiles, layoutFile, viewId);
                if (relatedJavaFiles != null && !relatedJavaFiles.isEmpty()) {
                    InteractableViewComplexity info = getCallbackMethodInfoByViewId(viewId, relatedJavaFiles);
                    if (info != null) {
                        infoList.add(info);
                    }
                }
            }
        }
        return infoList;
    }

    public List<VirtualFile> findRelatedJavaFile(List<VirtualFile> javaFiles, VirtualFile layoutXMLFile, String viewId) {
        List<VirtualFile> relatedJavaFiles = new ArrayList<>();
        for (VirtualFile javaFile : javaFiles) {
            if (containsAnnotationForView(javaFile, viewId)) {
                relatedJavaFiles.add(javaFile);
            }
        }
        return relatedJavaFiles;
    }

    private boolean containsAnnotationForView(VirtualFile file, String viewId) {
        boolean result = false;
        if (file.getName().endsWith(".java")) {
            PsiFile javaFile = PsiManager.getInstance(monkeyImprover.getProject()).findFile(file);
            if (javaFile != null && javaFile instanceof PsiJavaFile) {
                MethodFinder methodFinder = new MethodFinder();
                PsiMethod relatedMethod = methodFinder.findMethodByOnClickAnnotation((PsiJavaFile) javaFile, viewId);
                if (relatedMethod != null) {
                    result = true;
                }
            }
        }
        return result;
    }

    private InteractableViewComplexity getCallbackMethodInfoByViewId(String viewId, List<VirtualFile> relatedJavaFiles) {
        MethodFinder methodFinder = new MethodFinder();
        InteractableViewComplexity info = null;
        MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
        for (VirtualFile relatedJavaFile : relatedJavaFiles) {
            PsiFile file = PsiManager.getInstance(monkeyImprover.getProject()).findFile(relatedJavaFile);
            if (file != null && file instanceof PsiJavaFile) {
                PsiMethod relatedMethod = methodFinder.findMethodByOnClickAnnotation((PsiJavaFile) file, viewId);
                if (relatedMethod != null) {
                    MethodComplexity methodComplexity = methodComplexityAnalyzer.getComplexity(relatedMethod, true);
                    info = new InteractableViewComplexity(
                            new InteractableView(viewId, relatedMethod.getName(), relatedMethod, InteractableViewFinderType.ANNOTATED_FINDER), methodComplexity);
                    info.setBoundByAnnotation(true);
                    break;
                }
            }
        }
        return info;
    }

}
