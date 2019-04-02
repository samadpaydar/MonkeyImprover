package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class IntentAnalyzer extends JavaRecursiveElementVisitor {
    private MonkeyImprover monkeyImprover;
    private PsiMethod method;
    private List<PsiClass> intentClasses;

    public IntentAnalyzer(MonkeyImprover monkeyImprover, PsiMethod method) {
        this.monkeyImprover = monkeyImprover;
        this.method = method;
        intentClasses = new ArrayList<>();
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        try {
            PsiElement element = expression.getMethodExpression().getReference().resolve();
            if (element instanceof PsiMethod) {
                PsiMethod calledMethod = (PsiMethod) element;
                if(calledMethod.isConstructor() && calledMethod.getName().equals("Intent")) {
                    monkeyImprover.showMessage("method: " + method.getName() + " calledMethod " + calledMethod.getName());
                    PsiExpressionList list = expression.getArgumentList();
                    for(int i=0;i<list.getExpressionCount(); i++) {
                        monkeyImprover.showMessage(list.getExpressions()[i].getText());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

