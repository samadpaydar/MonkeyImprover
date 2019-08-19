package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.arrangement.JavaArrangementVisitor;
import com.intellij.psi.impl.source.tree.java.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.model.InteractableView;
import ir.ac.um.monkeyimprover.model.InteractableViewComplexity;
import ir.ac.um.monkeyimprover.model.InteractableViewFinderType;
import ir.ac.um.monkeyimprover.model.MethodComplexity;
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
public class DynamicInteractableViewFinder extends InteractableViewFinder {
    public DynamicInteractableViewFinder(MonkeyImprover monkeyImprover) {
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
                for (VirtualFile javaFile : allJavaFiles) {
                    InteractableViewComplexity info = getCallbackMethodInfo(javaFile, viewId);
                    if (info != null) {
                        infoList.add(info);
                    }
                }
            }
        }
        return infoList;
    }

    private InteractableViewComplexity getCallbackMethodInfo(VirtualFile javaFile, String viewId) {
        InteractableViewComplexity info = null;
        PsiFile psiFile = PsiManager.getInstance(monkeyImprover.getProject()).findFile(javaFile);
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            DynamicCallbackVisitor visitor = new DynamicCallbackVisitor(viewId);
            psiJavaFile.accept(visitor);
            InteractableView temp = visitor.getDynamicCallbackInfo();
            if (temp != null) {
                MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
                info = new InteractableViewComplexity(new InteractableView(viewId, temp.getMethod().getName(),
                        temp.getMethod(), InteractableViewFinderType.DYNAMIC_FINDER),
                        methodComplexityAnalyzer.getComplexity(temp.getMethod(), true));
            }
        }
        return info;
    }
}


class DynamicCallbackVisitor extends JavaRecursiveElementVisitor {
    private InteractableView interactableView = null;
    private String viewId;

    DynamicCallbackVisitor(String viewId) {
        this.viewId = viewId;
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitCallExpression(expression);
        try {
            String calledMethodName = expression.getMethodExpression().getReferenceName();
            if (calledMethodName.equals("findViewById")) {
                PsiExpressionList arguments = expression.getArgumentList();
                PsiExpression firstArgument = arguments.getExpressions()[0];
                if (firstArgument.getText().equals("R.id." + viewId)) {
                    if(viewId.contains("map_empty_panel")) {
                        PsiElement sibling = expression.getNextSibling();
                        while(sibling != null) {
                            Utils.showMessage("### SIBLING " + sibling);
                            sibling = sibling.getNextSibling();
                        }
                    }
                }
            }
/*
            PsiElement qualifier = expression.getMethodExpression().getQualifier();
            if (qualifier != null) {
                String name = qualifier.getText();
                if (name != null && name.equals(variableName)) {
                    String calledMethodName = expression.getMethodExpression().getReferenceName();
                    if (calledMethodName.equals("setOnClickListener")) {
                        PsiExpressionList arguments = expression.getArgumentList();
                        PsiExpression firstArgument = arguments.getExpressions()[0];
                        if (firstArgument instanceof PsiNewExpressionImpl) {
                            handleCase1((PsiNewExpressionImpl) firstArgument);
                        } else if (firstArgument instanceof PsiThisExpressionImpl) {
                            handleCase2((PsiThisExpressionImpl) firstArgument);
                        } else if (firstArgument instanceof PsiReferenceExpressionImpl) {
                            handleCase3((PsiReferenceExpressionImpl) firstArgument);
                        } else {
                            Utils.showMessage("TODO: handle " + firstArgument.getClass());
                        }
                    }
                }
            }
            */
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        super.visitExpression(expression);
        InteractableView info = findDynamicCallbackInfoForView(expression);
        if (info != null) {
            interactableView = info;
        }
    }

    @Override
    public void visitDeclarationStatement(PsiDeclarationStatement statement) {
        super.visitStatement(statement);
        InteractableView info = findDynamicCallbackInfoForView(statement);
        if (info != null) {
            interactableView = info;
        }
    }

    private InteractableView findDynamicCallbackInfoForView(PsiDeclarationStatement statement) {
        InteractableView result = null;
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
                        if (hasAccessToView((PsiMethodCallExpression) operand)) {
                            OnClickFinder finder = new OnClickFinder(variable.getName());
                            PsiElement[] siblings = statement.getParent().getChildren();
                            for (PsiElement sibling : siblings) {
                                sibling.accept(finder);
                                if (finder.getHandlerMethod() != null) {
                                    result = new InteractableView(viewId, finder.getHandlerMethod().getName(), finder.getHandlerMethod(), InteractableViewFinderType.DYNAMIC_FINDER);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
        return result;
    }

    private InteractableView findDynamicCallbackInfoForView(PsiAssignmentExpression assignmentExpression) {
        InteractableView result = null;
        PsiExpression rightExpression = assignmentExpression.getRExpression();
        PsiExpression leftExpression = assignmentExpression.getLExpression();
        if (rightExpression instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) rightExpression;
            if (hasAccessToView(methodCallExpression)) {
                String variableName = leftExpression.getReference().getElement().getText();
                OnClickFinder finder = new OnClickFinder(variableName);
                //In the following statement, the two calls to getParent() is intentional, not a mistake, since it does not work with single call
                PsiElement[] siblings = assignmentExpression.getParent().getParent().getChildren();
                for (PsiElement sibling : siblings) {
                    sibling.accept(finder);
                    if (finder.getHandlerMethod() != null) {
                        result = new InteractableView(viewId, finder.getHandlerMethod().getName(), finder.getHandlerMethod(), InteractableViewFinderType.DYNAMIC_FINDER);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean hasAccessToView(PsiMethodCallExpression methodCallExpression) {
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

    public InteractableView getDynamicCallbackInfo() {
        return interactableView;
    }
}

class OnClickFinder extends JavaRecursiveElementVisitor {
    private String variableName;
    private PsiMethod handlerMethod;

    OnClickFinder(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitCallExpression(expression);
        try {
            PsiElement qualifier = expression.getMethodExpression().getQualifier();
            if (qualifier != null) {
                String name = qualifier.getText();
                if (name != null && name.equals(variableName)) {
                    String calledMethodName = expression.getMethodExpression().getReferenceName();
                    if (calledMethodName.equals("setOnClickListener")) {
                        PsiExpressionList arguments = expression.getArgumentList();
                        PsiExpression firstArgument = arguments.getExpressions()[0];
                        if (firstArgument instanceof PsiNewExpressionImpl) {
                            handleCase1((PsiNewExpressionImpl) firstArgument);
                        } else if (firstArgument instanceof PsiThisExpressionImpl) {
                            handleCase2((PsiThisExpressionImpl) firstArgument);
                        } else if (firstArgument instanceof PsiReferenceExpressionImpl) {
                            handleCase3((PsiReferenceExpressionImpl) firstArgument);
                        } else {
                            Utils.showMessage("TODO: handle " + firstArgument.getClass());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
    }

    /**
     * This is for detecting handlers that are set by setOnClickListener(new ...)
     *
     * @param newExpression
     */
    private void handleCase1
    (PsiNewExpressionImpl newExpression) {
        newExpression.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                if (method.getName().equals("onClick")) {
                    handlerMethod = method;
                }
            }
        });
    }

    /**
     * This is for detecting handlers that are set by setOnClickListener(this)
     *
     * @param thisExpression
     */
    private void handleCase2(PsiThisExpressionImpl thisExpression) {
        PsiClass parentClass = getParentClass(thisExpression);
        if (parentClass != null) {
            PsiMethod onClickMethod = getOnClickMethod(parentClass);
            if (onClickMethod != null) {
                handlerMethod = onClickMethod;
            }
        }
    }

    /**
     * This is for detecting handlers that are set by setOnClickListener(listenerVariable)
     * where listenerVariable is defined in the same class
     *
     * @param referenceExpression
     */
    private void handleCase3(PsiReferenceExpressionImpl referenceExpression) {
        PsiElement referenceNameElement = referenceExpression.getReferenceNameElement();
        final String VARIABLE_NAME = referenceNameElement.getText();
        PsiClass parentClass = getParentClass(referenceNameElement);
        parentClass.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitField(PsiField field) {
                super.visitVariable(field);
                if (field.getName().equals(VARIABLE_NAME)) {
                    getHandlerForField(field, VARIABLE_NAME);
                }
            }
        });
    }

    private void getHandlerForField(PsiField field, String variableName) {
        field.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                super.visitCallExpression(expression);
                if (expression.getText().contains("OnClickListener()")) {
                    PsiElement assignedVariable = getAssignedVariable(expression, variableName);
                    if (assignedVariable != null) {
                        PsiClass referenceClass = expression.getAnonymousClass();
                        for (PsiMethod method : referenceClass.getMethods()) {
                            if (method.getName().equals("onClick")) {
                                handlerMethod = method;
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private PsiElement getAssignedVariable(PsiExpression expression, String variableName) {
        PsiElement result = null;
        PsiElement sibling = expression.getPrevSibling();
        while (sibling != null) {
            if (sibling.getText().equals(variableName)) {
                result = sibling;
                break;
            }
            sibling = sibling.getPrevSibling();
        }
        return result;
    }

    private PsiMethod getOnClickMethod(PsiClass psiClass) {
        for (PsiMethod method : psiClass.getMethods()) {
            if (method.getName().equals("onClick")) {
                return method;
            }
        }
        return null;
    }

    private PsiClass getParentClass(PsiElement element) {
        PsiClass parentClass = null;

        while (element != null) {
            element = element.getParent();
            if (element instanceof PsiClass) {
                parentClass = (PsiClass) element;
                break;
            }
        }
        return parentClass;
    }

    public PsiMethod getHandlerMethod() {
        return handlerMethod;
    }
}