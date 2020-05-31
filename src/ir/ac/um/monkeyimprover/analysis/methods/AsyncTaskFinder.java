package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class AsyncTaskFinder extends JavaRecursiveElementVisitor {

    private MonkeyImprover monkeyImprover;
    private List<PsiClass> asyncTaskClasses;

    public AsyncTaskFinder(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
        asyncTaskClasses = new ArrayList<>();
    }

    @Override
    public void visitClassObjectAccessExpression(PsiClassObjectAccessExpression expression) {
        super.visitClassObjectAccessExpression(expression);
    }

    // expression.getAnonymousClass().getBaseClassReference().getQualifiedName()

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);




        try {
            if (expression.getClassReference() != null){
                PsiJavaCodeReferenceElement reference = expression.getClassReference();
                if (reference != null) {
                    String className = reference.getQualifiedName();
                    PsiClass theClass = monkeyImprover.getProjectClassByName(className);
                    if (theClass != null && isAnAsyncTask(theClass)) {
                        asyncTaskClasses.add(theClass);
                    }
                }

            } else if (expression.getAnonymousClass() != null){
                PsiClass theClass = expression.getAnonymousClass();
//                asyncTaskClasses.add(theClass);
                if (theClass != null && isAnAsyncTask(theClass)) {
                    asyncTaskClasses.add(theClass);
                }
            }



        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
    }

    public List<PsiClass> getAsyncTaskClasses() {
        return asyncTaskClasses;
    }

    private boolean isAnAsyncTask(PsiClass theClass) {
        PsiClass[] superClasses = theClass.getSupers();
        for (PsiClass superClass : superClasses) {
            if (isMatch(superClass.getQualifiedName()))
                return true;

//            if (superClass.getQualifiedName().startsWith("android.os.AsyncTask")) {
//                return true;
//            }
        }
        return false;
    }

    private boolean isMatch(String className){
        String[] asyncLib = {
                "android.os.AsyncTask",
                "io.reactivex.Observable",
                "io.reactivex.Observer"
        };

        for(String lib : asyncLib){
            if(className.contains(lib))
                return true;
        }

        return false;
    }
}

