package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.util.List;

public class MethodAnalyzer {
    private MonkeyImprover monkeyImprover;

    public MethodAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    /**
     * @param method
     * @return the complexity of the given method
     */
    public double getMethodComplexity(PsiMethod method) {
        CyclomaticComplexityAnalyzer cyclomaticComplexityAnalyzer = new CyclomaticComplexityAnalyzer();
        double complexity = cyclomaticComplexityAnalyzer.getComplexity(method);
        List<PsiMethod> calledMethods = getMethodsDirectlyCalledBy(method);
        monkeyImprover.showMessage("Methods called by " + AnalysisUtils.getMethodQualifiedName(method));
        for (PsiMethod calledMethod : calledMethods) {
            monkeyImprover.showMessage(" >> " + AnalysisUtils.getMethodQualifiedName(calledMethod));
        }
        for (PsiMethod calledMethod : calledMethods) {
            monkeyImprover.showMessage(" >>>>> " + AnalysisUtils.getMethodQualifiedName(calledMethod));
            if (calledMethod.equals(method)) {
                monkeyImprover.showMessage(" >> RECURSIVE ");
                //ignore recursive calls
            } else if (isLocalMethod(calledMethod)) {
                monkeyImprover.showMessage(" >> Local ");
                complexity += getMethodComplexity(calledMethod);
            } else {
                monkeyImprover.showMessage(" >> ANDROID ");
                complexity += getAPIComplexity(calledMethod);
            }
        }
        return complexity;
    }

    private boolean isLocalMethod(PsiMethod calledMethod) {
        String calledMethodClassName = calledMethod.getContainingClass().getQualifiedName();
        List<String> projectClassNames = monkeyImprover.getProjectClassNames();
        for (String projectClassName : projectClassNames) {
            if (projectClassName.equals(calledMethodClassName)) {
                return true;
            }
        }
        return false;
    }

    private List<PsiMethod> getMethodsDirectlyCalledBy(PsiMethod method) {
        MethodCallAnalyzer methodCallAnalyzer = new MethodCallAnalyzer(method);
        method.accept(methodCallAnalyzer);
        return methodCallAnalyzer.getCalledMethods();
    }

    private double getAPIComplexity(PsiMethod calledMethod) {
        String calledMethodClassName = calledMethod.getContainingClass().getQualifiedName();
        String[] classNames = {"android.database.sqlite.SQLiteDatabase", "android.database.sqlite.SQLiteStatement"};
        double[] weights = {3.0, 0.5};
        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            if (className.equals(calledMethodClassName)) {
                return weights[i];
            }
        }
        return 1.0;
    }

}
