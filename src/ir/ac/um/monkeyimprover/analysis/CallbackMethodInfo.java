package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.PsiMethod;

public class CallbackMethodInfo {
    private String callbackName;
    private PsiMethod callbackMethod;
    private double callbackMethodComplexity;

    public CallbackMethodInfo(String callbackName, PsiMethod callbackMethod, double callbackMethodComplexity) {
        setCallbackName(callbackName);
        setCallbackMethod(callbackMethod);
        setCallbackMethodComplexity(callbackMethodComplexity);

    }

    public String getCallbackName() {
        return callbackName;
    }

    private void setCallbackName(String callbackName) {
        this.callbackName = callbackName;
    }

    public PsiMethod getCallbackMethod() {
        return callbackMethod;
    }

    private void setCallbackMethod(PsiMethod callbackMethod) {
        this.callbackMethod = callbackMethod;
    }

    public double getCallbackMethodComplexity() {
        return callbackMethodComplexity;
    }

    private void setCallbackMethodComplexity(double callbackMethodComplexity) {
        this.callbackMethodComplexity = callbackMethodComplexity;
    }
}
