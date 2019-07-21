package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.*;
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
                for (VirtualFile javaFile : allJavaFiles) {
                    CallbackMethodInfo info = getCallbackMethodInfo(javaFile, viewId);
                    if (info != null) {
                        Utils.showMessage("###### Info: " + info);
                        infoList.add(info);
                    }
                }
            }
        }
        return infoList;
    }

    private CallbackMethodInfo getCallbackMethodInfo(VirtualFile javaFile, String viewId) {
        CallbackMethodInfo info = null;
        PsiFile psiFile = PsiManager.getInstance(monkeyImprover.getProject()).findFile(javaFile);
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            DynamicCallbackVisitor visitor = new DynamicCallbackVisitor(viewId);
            System.out.println("AAAAA " + System.currentTimeMillis());
            psiJavaFile.accept(visitor);
            System.out.println("BBBBB " + System.currentTimeMillis());
            DynamicCallBackInfo temp = visitor.getDynamicCallBackInfo();
            if (temp != null) {
                Utils.showMessage("AAAAAAAAAAAAAAAAAAA " + temp.getMethod());
                MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
                info = new CallbackMethodInfo(viewId, temp.getMethod().getName(),
                        temp.getMethod(),
                        methodComplexityAnalyzer.getComplexity(temp.getMethod(), true));
            }
        }
        return info;
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


class DynamicCallbackVisitor extends JavaRecursiveElementVisitor {
    private DynamicCallBackInfo dynamicCallBackInfo = null;
    private String viewId;

    public DynamicCallbackVisitor(String viewId) {
        this.viewId = viewId;
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        super.visitExpression(expression);
        DynamicCallBackInfo info = findDynamicCallbackInfoForView(expression, viewId);
        if (info != null) {
            System.out.println("CCCC " + System.currentTimeMillis());
            dynamicCallBackInfo = info;
        }
    }

    @Override
    public void visitDeclarationStatement(PsiDeclarationStatement statement) {
        super.visitStatement(statement);
        DynamicCallBackInfo info = findDynamicCallbackInfoForView(statement, viewId);
        if (info != null) {
            System.out.println("DDDD " + System.currentTimeMillis());
            dynamicCallBackInfo = info;
        }
    }

    private DynamicCallBackInfo findDynamicCallbackInfoForView(PsiDeclarationStatement statement, String viewId) {
        DynamicCallBackInfo result = null;
        PsiDeclarationStatementImpl declarationStatement = (PsiDeclarationStatementImpl) statement;
        try {
            PsiElement[] children = declarationStatement.getChildren();
            if (children.length > 0 && children[0] instanceof PsiLocalVariableImpl) {
                PsiLocalVariableImpl variable = (PsiLocalVariableImpl) children[0];
                PsiExpression expression = variable.getInitializer();
                if (expression instanceof PsiTypeCastExpressionImpl) {
                    PsiTypeCastExpressionImpl typeCastExpression = (PsiTypeCastExpressionImpl) expression;
                    PsiExpression operand = typeCastExpression.getOperand();
                    if (operand instanceof PsiMethodCallExpressionImpl) {
                        if (hasAccessToView((PsiMethodCallExpression) operand, viewId)) {
                            OnClickFinder finder = new OnClickFinder(variable.getName());
                            PsiElement[] siblings = statement.getParent().getChildren();
                            for (PsiElement sibling : siblings) {
                                sibling.accept(finder);
                                if (finder.getHandlerMethod() != null) {
                                    result = new DynamicCallBackInfo(viewId, finder.getHandlerMethod());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private DynamicCallBackInfo findDynamicCallbackInfoForView(PsiAssignmentExpression assignmentExpression, String viewId) {
        DynamicCallBackInfo result = null;
        PsiExpression rightExpression = assignmentExpression.getRExpression();
        PsiExpression leftExpression = assignmentExpression.getLExpression();
        if (rightExpression instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) rightExpression;
            if (hasAccessToView(methodCallExpression, viewId)) {
                String variableName = leftExpression.getReference().getElement().getText();
                OnClickFinder finder = new OnClickFinder(variableName);
                PsiElement[] siblings = assignmentExpression.getParent().getChildren();
                for (PsiElement sibling : siblings) {
                    sibling.accept(finder);
                    if (finder.getHandlerMethod() != null) {
                        result = new DynamicCallBackInfo(viewId, finder.getHandlerMethod());
                        break;
                    }
                }
            }
        }
        return result;
    }


    private boolean hasAccessToView(PsiMethodCallExpression methodCallExpression, String viewId) {
        boolean result = false;
        String calledMethodName = methodCallExpression.getMethodExpression().getReferenceName();
        if (calledMethodName.equals("findViewById")) {
            PsiExpressionList arguments = methodCallExpression.getArgumentList();
            PsiExpression firstArgument = arguments.getExpressions()[0];
            if (firstArgument.getText().equals("R.id." + viewId)) {
                result = true;
            }
        }

        return result;
    }

    public DynamicCallBackInfo getDynamicCallBackInfo() {
        return dynamicCallBackInfo;
    }
}


class DynamicCallBackInfo {
    private String viewId;
    private PsiMethod method;

    public DynamicCallBackInfo(String viewId, PsiMethod method) {
        this.viewId = viewId;
        this.method = method;
    }

    public PsiMethod getMethod() {
        return method;
    }

    public String getViewId() {
        return viewId;
    }
}

class OnClickFinder extends JavaRecursiveElementVisitor {
    private String variableName;
    private PsiMethod handlerMethod;

    public OnClickFinder(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitCallExpression(expression);
        try {
            String name = expression.getMethodExpression().getQualifier().getText();
            if (name != null && name.equals(variableName)) {
                String calledMethodName = expression.getMethodExpression().getReferenceName();
                if (calledMethodName.equals("setOnClickListener")) {
                    PsiExpressionList arguments = expression.getArgumentList();
                    PsiExpression firstArgument = arguments.getExpressions()[0];
                    if (firstArgument instanceof PsiNewExpressionImpl) {
                        PsiNewExpressionImpl newExpression = (PsiNewExpressionImpl) firstArgument;
                        newExpression.accept(new JavaRecursiveElementVisitor() {
                            @Override
                            public void visitMethod(PsiMethod method) {
                                super.visitMethod(method);
                                if (method.getName().equals("onClick")) {
                                    handlerMethod = method;
                                }
                            }
                        });
                    } else {
                        Utils.showMessage("############# " + firstArgument.getClass());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PsiMethod getHandlerMethod() {
        return handlerMethod;
    }
}