package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexity;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexityAnalyzer;
import ir.ac.um.monkeyimprover.analysis.methods.MethodFinder;
import ir.ac.um.monkeyimprover.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for finding Dynamic Callbacks, i.e. those callbacks that are
 * set in the Java source files. For instance:
 * view.setOnClickListener(new OnClickListener() {...})
 */
public class DynamicCallbackFinder extends CallbackFinder {
    public DynamicCallbackFinder(MonkeyImprover monkeyImprover) {
        super(monkeyImprover);
    }

    @Override
    public List<CallbackMethodInfo> getCallbackMethodInfos(VirtualFile projectBaseDirectory, VirtualFile layoutFile) {
        List<CallbackMethodInfo> infoList = new ArrayList<>();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        List<String> viewIds = getViewIds(xmlFile);
        Utils.showMessage("viewIds " + viewIds);
        if (viewIds != null) {
            ClassFinder classFinder = new ClassFinder(monkeyImprover);
            List<VirtualFile> allJavaFiles = classFinder.getAllJavaFiles(projectBaseDirectory);
            Utils.showMessage("allJavaFiles " + allJavaFiles.size());
            for (String viewId : viewIds) {
                List<VirtualFile> relatedJavaFiles = new ArrayList<>();
                for (VirtualFile javaFile : allJavaFiles) {
                    if (isRelated(javaFile, viewId)) {
                        relatedJavaFiles.add(javaFile);
                    }
                }
                if (relatedJavaFiles != null && !relatedJavaFiles.isEmpty()) {
                    CallbackMethodInfo info = getCallbackMethodInfoByViewId(viewId, relatedJavaFiles);
                    if (info != null) {
                        infoList.add(info);
                    }
                }
            }
        }
        return infoList;
    }

    private boolean isRelated(VirtualFile javaFile, String viewId) {
        final List<VirtualFile> relatedFiles = new ArrayList<>();
        PsiFile psiFile = PsiManager.getInstance(monkeyImprover.getProject()).findFile(javaFile);
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;

            psiJavaFile.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitMethod(PsiMethod method) {
                    super.visitMethod(method);
                    if (setsOnClickListenerForView(method, viewId)) {
                        relatedFiles.add(javaFile);
                    }
                }
            });
        }
        return !relatedFiles.isEmpty();
    }

    private boolean setsOnClickListenerForView(PsiMethod method, String viewId) {
        boolean result = false;
        method.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                try {
                    String calledMethodName = expression.getMethodExpression().getReferenceName();
                    if (calledMethodName.equals("findViewById")) {
                        PsiExpressionList arguments = expression.getArgumentList();
                        PsiExpression firstArgument = arguments.getExpressions()[0];
                        Utils.showMessage(">> " + firstArgument.getText());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return result;
    }

    private CallbackMethodInfo getCallbackMethodInfoByViewId(String viewId, List<VirtualFile> relatedJavaFiles) {
        MethodFinder methodFinder = new MethodFinder();
        CallbackMethodInfo info = null;
        MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
        for (VirtualFile relatedJavaFile : relatedJavaFiles) {
            PsiFile file = PsiManager.getInstance(monkeyImprover.getProject()).findFile(relatedJavaFile);
            if (file != null && file instanceof PsiJavaFile) {
                PsiMethod relatedMethod = methodFinder.findMethodByOnClickAnnotation((PsiJavaFile) file, viewId);
                if (relatedMethod != null) {
                    MethodComplexity methodComplexity = methodComplexityAnalyzer.getComplexity(relatedMethod, true);
                    info = new CallbackMethodInfo(viewId, relatedMethod.getName(), relatedMethod, methodComplexity);
                    info.setBoundByAnnotation(true);
                    break;
                }
            }
        }
        return info;
    }

}
