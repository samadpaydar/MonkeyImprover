package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class MethodCallAnalyzer extends JavaRecursiveElementVisitor {
    private List<PsiMethod> calledMethods;

    public MethodCallAnalyzer(PsiMethod caller) {
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
            Utils.showException(e);
            e.printStackTrace();
        }
    }

    public List<PsiMethod> getCalledMethods() {
        return calledMethods;
    }
}

