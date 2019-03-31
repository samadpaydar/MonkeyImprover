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
    private MonkeyImprover monkeyImprover;

    public JavaClassCollector(MonkeyImprover monkeyImprover) {
        projectJavaClasses = new ArrayList<>();
        this.monkeyImprover = monkeyImprover;
    }

    @Override
    public void visitJavaFile(PsiJavaFile psiJavaFile) {
        super.visitFile(psiJavaFile);
        if (psiJavaFile.getName().endsWith(".java") && !psiJavaFile.getName().equals("R.java")) {
            PsiClass[] psiClasses = psiJavaFile.getClasses();
            for (PsiClass psiClass : psiClasses) {
                projectJavaClasses.add(psiClass);
                monkeyImprover.showMessage(psiJavaFile.getName() + " " + psiClass.getQualifiedName());
            }
        }
    }

    public List<PsiClass> getProjectJavaClasses() {
        return this.projectJavaClasses;
    }
}
