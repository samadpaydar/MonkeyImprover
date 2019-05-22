package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.utils.Utils;

/**
 * @author Samad Paydar
 */
public class CallbackMethodInfo {
    /**
     * the name of the callback as written in onClick attribute of a View in a layouts file
     */
    private String callbackName;
    /**
     * the matched callback method in a Java file
     */
    private PsiMethod callbackMethod;
    /**
     * complexity of the callback method
     */
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

    @Override
    public String toString() {
        return "[callbackName: " + callbackName
                + ", callbackMethod: " + (callbackMethod!= null ? Utils.getMethodQualifiedName(callbackMethod) : "N/A")
                + ", callbackMethodComplexity: " + callbackMethodComplexity + "]";
    }
}
