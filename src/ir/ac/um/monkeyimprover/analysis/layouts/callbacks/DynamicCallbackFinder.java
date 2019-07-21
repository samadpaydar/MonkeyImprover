package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiDeclarationStatementImpl;
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
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
        if (viewIds != null) {
            ClassFinder classFinder = new ClassFinder(monkeyImprover);
            List<VirtualFile> allJavaFiles = classFinder.getAllJavaFilesInSrcDirectory();
            for (String viewId : viewIds) {
                Utils.showMessage("\t\tViewId: " + viewId);
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
                public void visitAssignmentExpression(PsiAssignmentExpression expression) {
                    super.visitExpression(expression);
               //     if (setsOnClickListenerForView(expression, viewId)) {
               //         relatedFiles.add(javaFile);
               //     }
                }

                @Override
                public void visitDeclarationStatement(PsiDeclarationStatement statement) {
                    super.visitStatement(statement);
                    PsiDeclarationStatementImpl declarationStatement = (PsiDeclarationStatementImpl) statement;
                    String temp = statement.getText();
                    if(temp != null && temp.contains("signUpButton")) {
                        try {
                            Utils.showMessage("\t\t\t\t\t" + statement.getDeclaredElements()[0]);
                            PsiElement[] children = statement.getChildren();
                            for(PsiElement child: children) {
                                Utils.showMessage("\t\t\t\t\t\t" + child.getText() + " " +child.getClass());
                            }
                        }catch (Exception e) {}
                    }
                }
            });
        }
        return !relatedFiles.isEmpty();
    }

    private boolean setsOnClickListenerForView(PsiAssignmentExpression assignmentExpression, String viewId) {
        boolean result = false;
        String text = assignmentExpression.getText();
        if(text.contains("sign")) {
            Utils.showMessage(">>>>>>>>>>>>>>>>>>>>> " + text);
            PsiDeclarationStatement p;
        }
        PsiExpression rightExpression = assignmentExpression.getRExpression();
        PsiExpression leftExpression = assignmentExpression.getLExpression();
        if (rightExpression instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) rightExpression;
            String calledMethodName = methodCallExpression.getMethodExpression().getReferenceName();
            if (calledMethodName.equals("findViewById")) {
                PsiExpressionList arguments = methodCallExpression.getArgumentList();
                PsiExpression firstArgument = arguments.getExpressions()[0];
                if (firstArgument.getText().equals("R.id." + viewId) && viewId.contains("sign")) {
                    Utils.showMessage("left: " + leftExpression.getText());
                }
            }
        }

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
