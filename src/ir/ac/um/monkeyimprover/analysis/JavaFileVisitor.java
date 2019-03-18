package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

/**
 * @author Samad Paydar
 */
public class JavaFileVisitor extends JavaRecursiveElementVisitor {
    private MonkeyImprover monkeyImprover;

    public JavaFileVisitor(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    @Override
    public void visitJavaFile(PsiJavaFile psiJavaFile) {
        super.visitFile(psiJavaFile);
        MethodAnalyzer methodAnalyzer = new MethodAnalyzer();
        PsiClass[] psiClasses = psiJavaFile.getClasses();
        for (PsiClass psiClass : psiClasses) {
            PsiMethod[] psiMethods = psiClass.getMethods();
            for (PsiMethod psiMethod : psiMethods) {
                double complexity = methodAnalyzer.getMethodComplexity(psiMethod);
                monkeyImprover.showMessage("class: " + psiClass.getQualifiedName() + "\tmethod: " + psiMethod.getName()
                        + "\tcomplexity: " + complexity);
            }
        }

    }
}
