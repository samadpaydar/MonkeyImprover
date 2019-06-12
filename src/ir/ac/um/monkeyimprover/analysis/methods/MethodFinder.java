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
        for (PsiClass cls : classes) {
            PsiMethod[] methods = cls.getAllMethods();
            for (PsiMethod method : methods) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
        }
        return null;
    }

    public PsiMethod findMethodByOnClickAnnotation(PsiJavaFile javaFile, String viewId) {
        final String TOKEN1 = "@OnClick(";
        PsiClass[] classes = javaFile.getClasses();
        for (PsiClass cls : classes) {
            PsiMethod[] methods = cls.getAllMethods();
            for (PsiMethod method : methods) {
                Utils.showMessage("method: " + method.getName());
                PsiAnnotation[] annotations = method.getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    String text = annotation.getText();
                    Utils.showMessage("\ttext: " + text);
                    if (text.startsWith(TOKEN1)) {
                        int index = text.lastIndexOf(')');
                        if (index != -1) {
                            text = text.substring(TOKEN1.length(), index).trim();
                            index = text.indexOf('{');
                            if (index != -1) {//multiple ids for the same annotation @OnClick({R.id.btn_decimal, R.id.btn_0, R.id.btn_1})
                                int index2 = text.lastIndexOf('}');
                                if (index2 != -1) {
                                    text = text.substring(index + 1, index2);
                                }
                            }
                            String[] ids = text.split(",");
                            for(String id: ids) {
                                Utils.showMessage("\t\tid: " + id + " viewId: " + viewId);
                                if(id.equals("R.id." + viewId)) {
                                    return method;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}
