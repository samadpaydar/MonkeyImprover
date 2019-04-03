package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassComplexityAnalyzer;

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
        ClassComplexityAnalyzer classComplexityAnalyzer = new ClassComplexityAnalyzer();
        double complexity = 0.0;
        for (PsiClass asyncTaskClass : asyncTaskClasses) {
            complexity += classComplexityAnalyzer.getComplexity(asyncTaskClass);
        }
        return complexity;
    }

}
