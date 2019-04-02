package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.*;

public class IntentComplexityAnalyzer {
    private MonkeyImprover monkeyImprover;
    public IntentComplexityAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public int getComplexity(PsiMethod method) {
        IntentAnalyzer intentAnalyzer = new IntentAnalyzer(monkeyImprover, method);
        method.accept(intentAnalyzer);
        return 1;
    }


}
