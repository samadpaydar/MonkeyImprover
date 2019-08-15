package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexityAnalyzer;
import ir.ac.um.monkeyimprover.model.InteractableView;
import ir.ac.um.monkeyimprover.model.InteractableViewComplexity;
import ir.ac.um.monkeyimprover.model.InteractableViewFinderType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for finding View Accessors, i.e. those methods that use findViewById to access
 * a view, e.g. an EditText
 */
public class ViewAccessorFinder extends InteractableViewFinder {
    public ViewAccessorFinder(MonkeyImprover monkeyImprover) {
        super(monkeyImprover);
    }

    @Override
    public List<InteractableViewComplexity> getInteractableViewInfo(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<InteractableViewComplexity> result = new ArrayList<>();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        List<String> viewIds = getInputViewIds(xmlFile);
        if (viewIds != null) {
            ClassFinder classFinder = new ClassFinder(monkeyImprover);
            List<VirtualFile> allJavaFiles = classFinder.getAllJavaFilesInSrcDirectory();
            for (String viewId : viewIds) {
                for (VirtualFile javaFile : allJavaFiles) {
                    List<InteractableViewComplexity> list = getViewAccessorsInfo(javaFile, viewId);
                    if (!list.isEmpty()) {
                        result.addAll(list);
                    }
                }
            }
        }
        return result;
    }

    private List<InteractableViewComplexity> getViewAccessorsInfo(VirtualFile javaFile, String viewId) {
        List<InteractableViewComplexity> result = new ArrayList<>();
        PsiFile psiFile = PsiManager.getInstance(monkeyImprover.getProject()).findFile(javaFile);
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            ViewAccessorVisitor visitor = new ViewAccessorVisitor(viewId);
            psiJavaFile.accept(visitor);
            List<InteractableView> interactableViews = visitor.getInteractableViews();
            if (!interactableViews.isEmpty()) {
                MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
                for (InteractableView interactableView : interactableViews) {
                    result.add(new InteractableViewComplexity(interactableView,
                            methodComplexityAnalyzer.getComplexity(interactableView.getMethod(), true)));
                }
            }
        }
        return result;

    }
}

class ViewAccessorVisitor extends JavaRecursiveElementVisitor {
    private List<InteractableView> interactableViews = new ArrayList<>();
    private String viewId;

    public ViewAccessorVisitor(String viewId) {
        this.viewId = viewId;
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitCallExpression(expression);
        String calledMethodName = expression.getMethodExpression().getReferenceName();
        if (calledMethodName.equals("findViewById")) {
            PsiExpressionList arguments = expression.getArgumentList();
            PsiExpression firstArgument = arguments.getExpressions()[0];
            if (firstArgument.getText().equals("R.id." + viewId)) {
                PsiMethod method = getParentMethod(expression);
                if (method != null) {
                    getInteractableViews().add(new InteractableView(viewId, method.getName(), method, InteractableViewFinderType.VIEW_ACCESSOR_FINDER));
                }
            }
        }
    }

    private PsiMethod getParentMethod(PsiElement element) {
        PsiMethod parent = null;
        while (element != null) {
            PsiElement parentElement = element.getParent();
            if (parentElement instanceof PsiMethod) {
                parent = (PsiMethod) parentElement;
                break;
            } else {
                element = parentElement;
            }
        }
        return parent;
    }


    public List<InteractableView> getInteractableViews() {
        return interactableViews;
    }
}
