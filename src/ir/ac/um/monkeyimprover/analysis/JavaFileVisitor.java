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
public class JavaFileVisitor extends JavaRecursiveElementVisitor {
    private MonkeyImprover monkeyImprover;
    private List<String> projectClassNames;

    public JavaFileVisitor(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
        projectClassNames = new ArrayList<>();
    }

    @Override
    public void visitJavaFile(PsiJavaFile psiJavaFile) {
        super.visitFile(psiJavaFile);
        MethodAnalyzer methodAnalyzer = new MethodAnalyzer(monkeyImprover);
        PsiClass[] psiClasses = psiJavaFile.getClasses();
        for (PsiClass psiClass : psiClasses) {
            projectClassNames.add(psiClass.getQualifiedName());
            PsiMethod[] psiMethods = psiClass.getMethods();
            for (PsiMethod psiMethod : psiMethods) {
                double complexity = methodAnalyzer.getMethodComplexity(psiMethod);
                monkeyImprover.showMessage("class: " + psiClass.getQualifiedName() + "\tmethod: " + psiMethod.getName()
                        + "\tcomplexity: " + complexity);
            }
        }
    }

    public List<String> getProjectClassNames() {
        return this.projectClassNames;
    }
}
