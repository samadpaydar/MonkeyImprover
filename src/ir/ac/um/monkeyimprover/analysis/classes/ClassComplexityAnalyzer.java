package ir.ac.um.monkeyimprover.analysis.classes;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.methods.MethodComplexityAnalyzer;

public class ClassComplexityAnalyzer {

    private MonkeyImprover monkeyImprover;

    public ClassComplexityAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public double getComplexity(PsiClass theClass) {
        double complexity = 0.0;
        MethodComplexityAnalyzer methodComplexityAnalyzer = new MethodComplexityAnalyzer(monkeyImprover);
        for (PsiMethod method : theClass.getMethods()) {
            complexity += methodComplexityAnalyzer.getComplexity(method);
        }
        return complexity;
    }

}
