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
    private LayoutAnalyzer layoutAnalyzer;
    private ClassFinder classFinder;
    private MethodAnalyzer methodAnalyzer;
    private MethodFinder methodFinder;

    public MonkeyImprover(Project project, PsiElement psiElement, ConsoleView consoleView) {
        this.project = project;
        this.psiElement = psiElement;
        this.consoleView = consoleView;
    }

    @Override
    public void run() {
        showMessage("started processing project " + project.getName());
        layoutAnalyzer = new LayoutAnalyzer(this);
        classFinder = new ClassFinder(this);
        methodFinder = new MethodFinder();
        methodAnalyzer = new MethodAnalyzer();

        List<VirtualFile> layoutFiles = layoutAnalyzer.getLayoutFiles(project.getBaseDir());

        for (VirtualFile layoutFile : layoutFiles) {
            processLayoutFile(layoutFile);
        }
        //VirtualFile baseDirectory = project.getBaseDir();
        //psiElement.accept(new JavaFileVisitor(this));
        showMessage("Finished");
    }


    private void processLayoutFile(VirtualFile layoutFile) {
        showMessage("=====================================================");
        showMessage("Layout File: " + layoutFile.getName());
        List<String> callbackMethodNames = layoutAnalyzer.getCallbackMethodNames(layoutFile);
        showMessage("\tCallback Methods: " + callbackMethodNames);
        if (!callbackMethodNames.isEmpty()) {
            List<VirtualFile> relatedJavaFiles = classFinder.findRelatedJavaFile(project.getBaseDir(), layoutFile);
            showMessage("\tRelated Java Files: " + relatedJavaFiles);
            if (relatedJavaFiles != null && !relatedJavaFiles.isEmpty()) {
                for (String callbackMethodName : callbackMethodNames) {
                    processCallBack(callbackMethodName, relatedJavaFiles);
                }
            } else {
                showMessage("\trelatedJavaFile Not Found");
            }
        }
        showMessage("=====================================================");
    }

    private void processCallBack(String callbackMethodName, List<VirtualFile> relatedJavaFiles) {
        showMessage("Callback Method: " + callbackMethodName);
        for (VirtualFile relatedJavaFile : relatedJavaFiles) {
            PsiFile file = PsiManager.getInstance(project).findFile(relatedJavaFile);
            if (file != null && file instanceof PsiJavaFile) {
                PsiMethod relatedMethod = methodFinder.findMethodByName((PsiJavaFile) file, callbackMethodName);
                if (relatedMethod != null) {
                    showMessage("Related Method");
                    showMessage(relatedMethod.getText());
                    showMessage("...................................");
                    showMessage("\t\tcomplexity: " + methodAnalyzer.getMethodComplexity(relatedMethod));
                    break;
                }
            }
        }
    }


    public void showMessage(String message) {
        consoleView.print(String.format("%s%n", message),
                ConsoleViewContentType.NORMAL_OUTPUT);
    }
}
