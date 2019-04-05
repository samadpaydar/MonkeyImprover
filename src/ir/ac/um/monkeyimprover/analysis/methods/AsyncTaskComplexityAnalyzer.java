package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassComplexityAnalyzer;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;

import java.util.List;

public class AsyncTaskComplexityAnalyzer {
    private MonkeyImprover monkeyImprover;

    public AsyncTaskComplexityAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public double getComplexity(PsiMethod method) {
        AsyncTaskFinder asyncTaskFinder = new AsyncTaskFinder(monkeyImprover);
        method.accept(asyncTaskFinder);
        List<PsiClass> asyncTaskClasses = asyncTaskFinder.getAsyncTaskClasses();
        ClassComplexityAnalyzer classComplexityAnalyzer = new ClassComplexityAnalyzer(monkeyImprover);
        double complexity = 0.0;
        for (PsiClass asyncTaskClass : asyncTaskClasses) {
            double asyncTaskComplexity =classComplexityAnalyzer.getComplexity(asyncTaskClass);;
            complexity += asyncTaskComplexity;
        }
        return complexity;
    }

}
