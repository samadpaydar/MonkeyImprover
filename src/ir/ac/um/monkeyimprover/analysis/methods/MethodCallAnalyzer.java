package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class MethodCallAnalyzer extends JavaRecursiveElementVisitor {
    private PsiMethod caller;
    private List<PsiMethod> calledMethods;

    public MethodCallAnalyzer(PsiMethod caller) {
        this.caller = caller;
        calledMethods = new ArrayList<>();
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        try {
            PsiElement element = expression.getMethodExpression().getReference().resolve();
            if (element instanceof PsiMethod) {
                calledMethods.add((PsiMethod) element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<PsiMethod> getCalledMethods() {
        return calledMethods;
    }
}

