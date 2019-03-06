package ir.ac.um.monkeyimprover.analysis;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import java.util.List;


public class MonkeyImprover implements Runnable {
    private Project project;
    private PsiElement psiElement;
    private ConsoleView consoleView;

    public MonkeyImprover(Project project, PsiElement psiElement, ConsoleView consoleView) {
        this.project = project;
        this.psiElement = psiElement;
        this.consoleView = consoleView;
    }

    @Override
    public void run() {
        showMessage("started processing project " + project.getName());
        LayoutAnalyzer layoutAnalyzer = new LayoutAnalyzer(this);
        MethodFinder methodFiner = new MethodFinder();
        List<VirtualFile> layoutFiles = layoutAnalyzer.getLayoutFiles(project.getBaseDir());
        for (VirtualFile layoutFile : layoutFiles) {
            showMessage("layout " + layoutFile.getName());
            List<String> callbackMethodNames = layoutAnalyzer.getCallbackMethodNames(layoutFile);
            for (String callbackMethodName : callbackMethodNames) {
                showMessage("\tcallbackMethodName: " + callbackMethodName);
            }
            ClassFinder classFinder = new ClassFinder(this);
            VirtualFile relatedJavaFile = classFinder.findRelatedJavaFile(project.getBaseDir(), layoutFile);
            showMessage("relatedJavaFile " + (relatedJavaFile != null ? relatedJavaFile.getName() : " not found"));
            if (relatedJavaFile != null) {
                PsiFile file = PsiManager.getInstance(project).findFile(relatedJavaFile);
                if (file != null && file instanceof PsiJavaFile) {
                    for (String callbackMethodName : callbackMethodNames) {
                        PsiMethod relatedMethod = methodFiner.findMethodByName((PsiJavaFile) file, callbackMethodName);
                        showMessage("\t\tcallbackMethodName: " + callbackMethodName + " relatedMethod: " + relatedMethod.getText());
                    }
                }
            }

        }
        //VirtualFile baseDirectory = project.getBaseDir();
        psiElement.accept(new JavaFileVisitor(this));
        showMessage("Finished");
    }


    public void showMessage(String message) {
        consoleView.print(String.format("%s%n", message),
                ConsoleViewContentType.NORMAL_OUTPUT);
    }
}
