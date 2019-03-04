package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

public class MethodFinder {

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
}
