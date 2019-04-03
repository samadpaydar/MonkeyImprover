package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassComplexityAnalyzer;

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
        ClassComplexityAnalyzer classComplexityAnalyzer = new ClassComplexityAnalyzer();
        double complexity = 0.0;
        for (PsiClass intentClass : intentClasses) {
                complexity += classComplexityAnalyzer.getComplexity(intentClass);
        }
        return complexity;
    }


}
