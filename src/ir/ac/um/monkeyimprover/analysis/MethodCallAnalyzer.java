package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.*;

public class MethodCallAnalyzer extends JavaRecursiveElementVisitor {
    private PsiMethod caller;
    private PsiMethod callee;
    private boolean called;

    public MethodCallAnalyzer(PsiMethod caller, PsiMethod callee) {
        this.caller = caller;
        this.callee = callee;
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        try {
            PsiElement element = expression.getMethodExpression().getReference().resolve();
            if (element instanceof PsiMethod) {
                PsiMethod calledMethod = (PsiMethod) element;
                String callerMethodQualifiedName = AnalysisUtils.getMethodQualifiedName(caller);
                String calledMethodQualifiedName = AnalysisUtils.getMethodQualifiedName(calledMethod);
                String calleeQualifiedName = AnalysisUtils.getMethodQualifiedName(callee);
                boolean recursiveCall = callerMethodQualifiedName.equalsIgnoreCase(calledMethodQualifiedName);
                if (calledMethodQualifiedName != null && calledMethodQualifiedName.equals(calleeQualifiedName)) {
                    this.called = true;
                } else if (!recursiveCall) {
                    MethodCallAnalyzer analyzer = new MethodCallAnalyzer(caller, callee);
                    calledMethod.accept(analyzer);
                    boolean indirectlyCalled = analyzer.hasCalled();
                    if (indirectlyCalled) {
                        called = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasCalled() {
        return called;
    }
}

