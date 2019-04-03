package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;

import java.util.List;

public class AsyncTaskComplexityAnalyzer {
    private MonkeyImprover monkeyImprover;

    public AsyncTaskComplexityAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public double getComplexity(PsiMethod method) {
        AsyncTaskAnalyzer asyncTaskAnalyzer = new AsyncTaskAnalyzer(monkeyImprover);
        method.accept(asyncTaskAnalyzer);
        List<PsiClass> asyncTaskClasses = asyncTaskAnalyzer.getAsyncTaskClasses();
        monkeyImprover.showMessage("\t\t\tmethod " + AnalysisUtils.getMethodQualifiedName(method));
        for(PsiClass ss: asyncTaskClasses) {
            monkeyImprover.showMessage("\t\t\t\t" + ss.getQualifiedName() );
        }
        double complexity = 0.0;
        for (PsiClass aa : asyncTaskClasses) {
                    complexity += getClassComplexity(aa);
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
