package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.*;

import java.util.List;

public class IntentComplexityAnalyzer {
    private MonkeyImprover monkeyImprover;

    public IntentComplexityAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public double getComplexity(PsiMethod method) {
        IntentAnalyzer intentAnalyzer = new IntentAnalyzer(monkeyImprover);
        method.accept(intentAnalyzer);
        List<String> intentClassNames = intentAnalyzer.getIntentClassNames();
        List<PsiClass> projectClasses = monkeyImprover.getProjectJavaClasses();
        double complexity = 0.0;
        for (String intentClassName : intentClassNames) {
            for (PsiClass projectClass : projectClasses) {
                if (intentClassName.equals(projectClass.getQualifiedName())) {
                    complexity += getClassComplexity(projectClass);
                    break;
                }
            }
        }
        return complexity;
    }

    private double getClassComplexity(PsiClass projectClass) {
        double complexity = 0.0;
        CyclomaticComplexityAnalyzer cyclomaticComplexityAnalyzer = new CyclomaticComplexityAnalyzer();
        for (PsiMethod method : projectClass.getMethods()) {
            complexity += cyclomaticComplexityAnalyzer.getComplexity(method);
        }
        return complexity;
    }


}
