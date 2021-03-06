package ir.ac.um.monkeyimprover.model;

import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;
import ir.ac.um.monkeyimprover.utils.Utils;

public class MethodComplexity {
    private PsiMethod method;

    public MethodComplexity(PsiMethod method) {
        this.method = method;
    }

    private double cyclomaticComplexity;
    private double calledMethodComplexity;
    private double intentComplexity;
    private double asyncComplexity;


    public double getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(double cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public double getCalledMethodComplexity() {
        return calledMethodComplexity;
    }

    public void setCalledMethodComplexity(double calledMethodComplexity) {
        this.calledMethodComplexity = calledMethodComplexity;
    }

    public double getIntentComplexity() {
        return intentComplexity;
    }

    public void setIntentComplexity(double intentComplexity) {
        this.intentComplexity = intentComplexity;
    }

    public double getAsyncComplexity() {
        return asyncComplexity;
    }

    public void setAsyncComplexity(double asyncComplexity) {
        this.asyncComplexity = asyncComplexity;
    }

    public double getTotalComplexity() {
        return cyclomaticComplexity + calledMethodComplexity
                + intentComplexity + asyncComplexity;
    }

    @Override
    public String toString() {
        double totalComplexity = getTotalComplexity();
        return "method " + AnalysisUtils.getMethodQualifiedName(method)
                + " cyclomaticComplexity: " + String.format("%.2f", cyclomaticComplexity)
                + " calledMethodComplexity: " + String.format("%.2f", calledMethodComplexity)
                + " intentComplexity: " + String.format("%.2f", intentComplexity)
                + " asyncComplexity: " + String.format("%.2f", asyncComplexity)
                + " totalComplexity: " + String.format("%.2f", totalComplexity);
    }
}
