package ir.ac.um.monkeyimprover.analysis;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.psi.PsiElement;


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
        //  LayoutAnalyzer layoutAnalyzer = new LayoutAnalyzer(this);
        //VirtualFile baseDirectory = project.getBaseDir();
        psiElement.accept(new JavaFileVisitor(this));
        showMessage("Finished");
    }


    public void showMessage(String message) {
        consoleView.print(String.format("%s%n", message),
                ConsoleViewContentType.NORMAL_OUTPUT);
    }
}
