package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class IntentAnalyzer extends JavaRecursiveElementVisitor {
    private MonkeyImprover monkeyImprover;
    private List<String> intentClassNames;

    public IntentAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
        intentClassNames = new ArrayList<>();
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        try {
            if (expression.getClassReference().getQualifiedName().equals("android.content.Intent")) {
                PsiExpressionList list = expression.getArgumentList();
                monkeyImprover.showMessage(">> " +list.getExpressionCount());
                if(list.getExpressionCount()>1) {
                    PsiExpression secondArgument = list.getExpressions()[1];
                    String typeName = secondArgument.getType().getCanonicalText();
                    final String CLASS_PREFIX = "java.lang.Class";
                    if(typeName!= null && typeName.startsWith(CLASS_PREFIX)) {
                        typeName = typeName.replace(CLASS_PREFIX, "");
                        typeName = typeName.replace("<", "");
                        typeName = typeName.replace(">", "");
                        intentClassNames.add(typeName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getIntentClassNames() {
        return intentClassNames;
    }
}

