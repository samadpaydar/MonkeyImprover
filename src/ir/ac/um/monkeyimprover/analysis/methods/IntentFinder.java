package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class IntentFinder extends JavaRecursiveElementVisitor {
    private MonkeyImprover monkeyImprover;
    private List<PsiClass> intentClasses;

    public IntentFinder(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
        intentClasses = new ArrayList<>();
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        try {
            PsiJavaCodeReferenceElement reference = expression.getClassReference();
            if (reference!= null && reference.getQualifiedName()!=null && reference.getQualifiedName().equals("android.content.Intent")) {
                PsiExpressionList list = expression.getArgumentList();
                if (list.getExpressionCount() > 1) {
                    PsiExpression secondArgument = list.getExpressions()[1];
                    String className = secondArgument.getType().getCanonicalText();
                    final String CLASS_PREFIX = "java.lang.Class";
                    if (className != null && className.startsWith(CLASS_PREFIX)) {
                        className = className.replace(CLASS_PREFIX, "");
                        className = className.replace("<", "");
                        className = className.replace(">", "");
                        PsiClass theClass = monkeyImprover.getProjectClassByName(className);
                        if (theClass != null) {
                            intentClasses.add(theClass);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
    }

    public List<PsiClass> getIntentClasses() {
        return intentClasses;
    }


}

