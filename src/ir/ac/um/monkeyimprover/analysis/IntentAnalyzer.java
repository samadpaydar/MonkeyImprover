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
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        try {
            if (expression.getClassReference().getQualifiedName().equals("android.content.Intent")) {
                PsiExpressionList list = expression.getArgumentList();
                if(list.getExpressionCount()>1) {
                    PsiExpression secondArgument = list.getExpressions()[1];
                    monkeyImprover.showMessage(secondArgument.getType().getCanonicalText());
                    monkeyImprover.showMessage(secondArgument.getText());

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

