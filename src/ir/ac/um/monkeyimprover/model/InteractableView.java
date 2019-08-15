package ir.ac.um.monkeyimprover.model;

import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;

public class InteractableView {
    private String viewId;
    private String methodName;
    /**
     * the method that is associated with this view,
     * i.e. the event handler of the view (e.g. for a Button),
     * or the method that accesses this view (e.g. for an EditText)
     */
    private PsiMethod method;
    private InteractableViewFinderType finderType;

    /**
     * complexity of the method
     */

    public InteractableView(String viewId, String methodName, PsiMethod method, InteractableViewFinderType finderType) {
        this.viewId = viewId;
        this.methodName = methodName;
        this.method = method;
        this.finderType = finderType;
    }

    public PsiMethod getMethod() {
        return method;
    }

    public String getViewId() {
        return viewId;
    }

    /**
     * the name of the method that is associated with this view,
     * i.e. the event handler of the view (e.g. for a Button),
     * or the method that accesses this view (e.g. for an EditText)
     */
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return "viewId: " + viewId
                + ", methodName: " + methodName
                + ", method: " + (method != null ? AnalysisUtils.getMethodQualifiedName(method) : "N/A")
                + ", Finder: " + finderType;
    }
}
