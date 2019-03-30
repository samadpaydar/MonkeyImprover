package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.*;

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
        PsiMethod[] calledMethods = getMethodsDirectlyCalledBy(method);
        for(PsiMethod calledMethod: calledMethods) {
            if(isLocalMethod(calledMethod)) {
                complexity += getMethodComplexity(calledMethod);
            } else {
                complexity += getAPIComplexity(calledMethod);
            }
        }
        return complexity;
    }

    private boolean invokes(PsiMethod caller, PsiMethod callee) {
        MethodCallAnalyzer methodCallAnalyzer = new MethodCallAnalyzer(caller, callee);
        caller.accept(methodCallAnalyzer);
        return methodCallAnalyzer.hasCalled();
    }
}
