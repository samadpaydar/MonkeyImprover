package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class AsyncTaskAnalyzer extends JavaRecursiveElementVisitor {

    private MonkeyImprover monkeyImprover;
    private List<PsiClass> asyncTaskClasses;

    public AsyncTaskAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
        asyncTaskClasses = new ArrayList<>();
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        try {
            PsiJavaCodeReferenceElement reference = expression.getClassReference();
            if (reference != null) {
                String className = reference.getQualifiedName();
                PsiClass theClass = findRelatedProjecClass(className);
                if (theClass != null && isAnAsyncTask(theClass)) {
                    asyncTaskClasses.add(theClass);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<PsiClass> getAsyncTaskClasses() {
        return asyncTaskClasses;
    }

    private PsiClass findRelatedProjecClass(String className) {
        List<PsiClass> projectJavaClasses = monkeyImprover.getProjectJavaClasses();
        for (PsiClass projectJavaClass : projectJavaClasses) {
            if (projectJavaClass.getQualifiedName().equals(className)) {
                return projectJavaClass;
            }
        }
        return null;
    }

    private boolean isAnAsyncTask(PsiClass theClass) {
        PsiClass[] superClasses = theClass.getSupers();
        for (PsiClass superClass : superClasses) {
            if (superClass.getQualifiedName().startsWith("android.os.AsyncTask")) {
                monkeyImprover.showMessage(superClass.getQualifiedName());
                return true;
            }
        }
        return false;
    }
}
