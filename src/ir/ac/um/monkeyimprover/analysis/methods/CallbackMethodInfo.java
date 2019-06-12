package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;

/**
 * @author Samad Paydar
 */
public class CallbackMethodInfo {
    private String viewId;
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

    private boolean isBoundByAnnotation;

    public CallbackMethodInfo(String viewId, String callbackName, PsiMethod callbackMethod, double callbackMethodComplexity) {
        setViewId(viewId);
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
        return "[viewId: " + viewId
                + ",callbackName: " + callbackName
                + ", callbackMethod: " + (callbackMethod!= null ? AnalysisUtils.getMethodQualifiedName(callbackMethod) : "N/A")
                + ", callbackMethodComplexity: " + callbackMethodComplexity
                + ", isBoundByAnnotation: " + isBoundByAnnotation
                + "]";
    }

    /**
     * the id of the view that has an onClick
     */
    public String getViewId() {
        return viewId;
    }

    private void setViewId(String viewId) {
        this.viewId = viewId;
    }

    /**
     * determines whether the onclick handler is bound in the Java code
     * using @OnClick annotation
     */
    public boolean isBoundByAnnotation() {
        return isBoundByAnnotation;
    }

    public void setBoundByAnnotation(boolean boundByAnnotation) {
        isBoundByAnnotation = boundByAnnotation;
    }
}
