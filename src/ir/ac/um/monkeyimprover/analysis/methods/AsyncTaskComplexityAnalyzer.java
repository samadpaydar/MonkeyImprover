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
        AsyncTaskAnalyzer asyncTaskAnalyzer = new AsyncTaskAnalyzer();
        method.accept(asyncTaskAnalyzer);
        List<String> asyncTaskClassNames = asyncTaskAnalyzer.getAsyncTaskClassNames();
        monkeyImprover.showMessage("\t\t\tmethod " + AnalysisUtils.getMethodQualifiedName(method));
        for(String s: asyncTaskClassNames) {
            monkeyImprover.showMessage("\t\t\t\t" + s );
        }
        List<PsiClass> projectClasses = monkeyImprover.getProjectJavaClasses();
        double complexity = 0.0;
        for (String asyncTaskClassName : asyncTaskClassNames) {
            for (PsiClass projectClass : projectClasses) {
                if (asyncTaskClassName.equals(projectClass.getQualifiedName())) {
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
