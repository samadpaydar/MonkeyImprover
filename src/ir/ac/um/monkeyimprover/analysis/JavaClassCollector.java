package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class JavaClassCollector extends JavaRecursiveElementVisitor {
    private List<PsiClass> projectJavaClasses;

    public JavaClassCollector() {
        projectJavaClasses = new ArrayList<>();
    }

    @Override
    public void visitJavaFile(PsiJavaFile psiJavaFile) {
        super.visitFile(psiJavaFile);
        PsiClass[] psiClasses = psiJavaFile.getClasses();
        for (PsiClass psiClass : psiClasses) {
            projectJavaClasses.add(psiClass);
        }
    }

    public List<PsiClass> getProjectJavaClasses() {
        return this.projectJavaClasses;
    }
}
