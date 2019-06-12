package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import ir.ac.um.monkeyimprover.utils.Utils;

/**
 * @author Samad Paydar
 */
public class MethodFinder {
//TODO doesnt it need to care about the parameters it is possible that there  are to methods name back. It should look for a method with a View parameter
    public PsiMethod findMethodByName(PsiJavaFile javaFile, String methodName) {
        PsiClass[] classes = javaFile.getClasses();
        for(PsiClass cls: classes) {
            PsiMethod[] methods = cls.getAllMethods();
            for(PsiMethod method: methods) {
                if(method.getName().equals(methodName)) {
                    return method;
                }
            }
        }
        return null;
    }

    public PsiMethod findMethodByOnClickAnnotation(PsiJavaFile javaFile, String viewId) {
        PsiClass[] classes = javaFile.getClasses();
        for(PsiClass cls: classes) {
            PsiMethod[] methods = cls.getAllMethods();
            for(PsiMethod method: methods) {
                PsiAnnotation[] annotations = method.getAnnotations();
                for(PsiAnnotation annotation: annotations) {
                    Utils.showMessage("annotation.getQualifiedName() : " + annotation.getQualifiedName());
                    Utils.showMessage("annotation.getText() : " + annotation.getText());
                }
                //if(method.getName().equals(methodName)) {
                //    return method;
                //}
            }
        }
        return null;
    }

}
