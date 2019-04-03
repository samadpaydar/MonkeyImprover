package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;

import java.util.List;

public class IntentComplexityAnalyzer {
    private MonkeyImprover monkeyImprover;

    public IntentComplexityAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public double getComplexity(PsiMethod method) {
        IntentFinder intentFinder = new IntentFinder(monkeyImprover);
        method.accept(intentFinder);
        List<PsiClass> intentClasses = intentFinder.getIntentClasses();
        double complexity = 0.0;
        for (PsiClass intentClass : intentClasses) {
                complexity += getClassComplexity(intentClass);
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
