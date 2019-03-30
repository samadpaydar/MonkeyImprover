package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.util.List;

public class MethodAnalyzer {
    public MethodAnalyzer() {

    }

    /**
     * @param method
     * @return the complexity of the given method
     */
    public double getMethodComplexity(PsiMethod method) {
        CyclomaticComplexityAnalyzer cyclomaticComplexityAnalyzer = new CyclomaticComplexityAnalyzer();
        double complexity = cyclomaticComplexityAnalyzer.getComplexity(method);
        List<PsiMethod> calledMethods = getMethodsDirectlyCalledBy(method);
        for (PsiMethod calledMethod : calledMethods) {
            if (calledMethod.equals(method)) {
                //ignore recursive calls
            } else if (isLocalMethod(method, calledMethod)) {
                complexity += getMethodComplexity(calledMethod);
            } else {
                complexity += getAPIComplexity(calledMethod);
            }
        }
        return complexity;
    }

    private boolean isLocalMethod(PsiMethod callerMethod, PsiMethod calledMethod) {
        Project project1 = callerMethod.getContainingClass().getProject();
        Project project2 = calledMethod.getContainingClass().getProject();
        return project1.getName().equals(project2.getName());
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
