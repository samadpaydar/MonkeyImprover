package ir.ac.um.monkeyimprover.model;

import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;

/**
 * @author Samad Paydar
 */
public class InteractableViewComplexity {
    private InteractableView interactableView;
    private MethodComplexity methodComplexity;

    private boolean isBoundByAnnotation;

    public InteractableViewComplexity(InteractableView interactableView, MethodComplexity methodComplexity) {
        setInteractableView(interactableView);
        setMethodComplexity(methodComplexity);
    }


    public MethodComplexity getMethodComplexity() {
        return methodComplexity;
    }

    private void setMethodComplexity(MethodComplexity methodComplexity) {
        this.methodComplexity = methodComplexity;
    }

    @Override
    public String toString() {
        return "[" + interactableView
                + ", methodComplexity: " + methodComplexity
                + ", isBoundByAnnotation: " + isBoundByAnnotation
                + "]";
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

    public InteractableView getInteractableView() {
        return interactableView;
    }

    public void setInteractableView(InteractableView interactableView) {
        this.interactableView = interactableView;
    }
}
