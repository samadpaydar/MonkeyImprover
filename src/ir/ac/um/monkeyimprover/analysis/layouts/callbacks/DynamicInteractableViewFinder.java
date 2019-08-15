package ir.ac.um.monkeyimprover.analysis.layouts.callbacks;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
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
                    info = new InteractableViewComplexity(new InteractableView(viewId, relatedMethod.getName(), relatedMethod, InteractableViewFinderType.DYNAMIC_FINDER), methodComplexity);
                    info.setBoundByAnnotation(true);
                    break;
                }
            }
        }
        return info;
    }

}


class DynamicCallbackVisitor extends JavaRecursiveElementVisitor {
    private InteractableView interactableView = null;
    private String viewId;

    public DynamicCallbackVisitor(String viewId) {
        this.viewId = viewId;
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        super.visitExpression(expression);
        InteractableView info = findDynamicCallbackInfoForView(expression, viewId);
        if (info != null) {
            interactableView = info;
        }
    }

    @Override
    public void visitDeclarationStatement(PsiDeclarationStatement statement) {
        super.visitStatement(statement);
        InteractableView info = findDynamicCallbackInfoForView(statement, viewId);
        if (info != null) {
            interactableView = info;
        }
    }

    private InteractableView findDynamicCallbackInfoForView(PsiDeclarationStatement statement, String viewId) {
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
                        if (hasAccessToView((PsiMethodCallExpression) operand, viewId)) {
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

    private InteractableView findDynamicCallbackInfoForView(PsiAssignmentExpression assignmentExpression, String viewId) {
        InteractableView result = null;
        PsiExpression rightExpression = assignmentExpression.getRExpression();
        PsiExpression leftExpression = assignmentExpression.getLExpression();
        if (rightExpression instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) rightExpression;
            if (hasAccessToView(methodCallExpression, viewId)) {
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

    public InteractableView getDynamicCallbackInfo() {
        return interactableView;
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
            PsiElement qualifier = expression.getMethodExpression().getQualifier();
            if (qualifier != null) {
                String name = qualifier.getText();
                if (name != null && name.equals(variableName)) {
                    String calledMethodName = expression.getMethodExpression().getReferenceName();
                    if (calledMethodName.equals("setOnClickListener")) {
                        PsiExpressionList arguments = expression.getArgumentList();
                        PsiExpression firstArgument = arguments.getExpressions()[0];
                        if (firstArgument instanceof PsiNewExpressionImpl) {
                            //setOnClickListener(new ...)
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
                        } else if (firstArgument instanceof PsiThisExpressionImpl) {
                            //setOnClickListener(this)
                            PsiThisExpressionImpl thisArgument = (PsiThisExpressionImpl) firstArgument;
                            PsiClass parentClass = getParentClass(thisArgument);
                            if(parentClass != null) {
                                PsiMethod onClickMethod = getOnClickMethod(parentClass);
                                if(onClickMethod != null) {
                                    handlerMethod = onClickMethod;
                                }
                            }
                        } else {
                            Utils.showMessage("############# " + firstArgument.getClass());
                            Utils.showMessage("############# " + expression.getText());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
    }

    private PsiMethod getOnClickMethod(PsiClass psiClass) {
        for(PsiMethod method: psiClass.getMethods()) {
            if(method.getName().equals("onClick")) {
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