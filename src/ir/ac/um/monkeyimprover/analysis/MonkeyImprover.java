package ir.ac.um.monkeyimprover.analysis;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import java.util.ArrayList;
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
        showMessage("Started processing project " + project.getName());
        layoutAnalyzer = new LayoutAnalyzer();
        classFinder = new ClassFinder();
        methodFinder = new MethodFinder();
        methodAnalyzer = new MethodAnalyzer();
        LayoutRefactory layoutRefactory = new LayoutRefactory();
        showMessage("Extracting layout files...");
        List<VirtualFile> layoutFiles = layoutAnalyzer.getLayoutFiles(project.getBaseDir());
        for (VirtualFile layoutFile : layoutFiles) {
            showMessage("Processing layout file " + layoutFile.getName() + "...");
            List<CallbackMethodInfo> info = processLayoutFile(layoutFile);
            layoutRefactory.refactorLayout(new LayoutInfo(layoutFile, info));
        }
        //VirtualFile baseDirectory = project.getBaseDir();
        //psiElement.accept(new JavaFileVisitor(this));
        showMessage("Finished");
    }

    private List<CallbackMethodInfo> processLayoutFile(VirtualFile layoutFile) {
        List<CallbackMethodInfo> infoList = new ArrayList<>();
        List<String> callbackMethodNames = layoutAnalyzer.getCallbackMethodNames(layoutFile);
        if (!callbackMethodNames.isEmpty()) {
            List<VirtualFile> relatedJavaFiles = classFinder.findRelatedJavaFile(project.getBaseDir(), layoutFile);
            if (relatedJavaFiles != null && !relatedJavaFiles.isEmpty()) {
                for (String callbackMethodName : callbackMethodNames) {
                    CallbackMethodInfo info = processCallBack(callbackMethodName, relatedJavaFiles);
                    infoList.add(info);
                }
            }
        }
        return infoList;
    }

    private CallbackMethodInfo processCallBack(String callbackMethodName, List<VirtualFile> relatedJavaFiles) {
        double complexity = -1;
        PsiMethod method = null;
        for (VirtualFile relatedJavaFile : relatedJavaFiles) {
            PsiFile file = PsiManager.getInstance(project).findFile(relatedJavaFile);
            if (file != null && file instanceof PsiJavaFile) {
                PsiMethod relatedMethod = methodFinder.findMethodByName((PsiJavaFile) file, callbackMethodName);
                if (relatedMethod != null) {
                    method = relatedMethod;
                    complexity = methodAnalyzer.getMethodComplexity(relatedMethod);
                    break;
                }
            }
        }
        return new CallbackMethodInfo(callbackMethodName, method, complexity);
    }


    public void showMessage(String message) {
        consoleView.print(String.format("%s%n", message),
                ConsoleViewContentType.NORMAL_OUTPUT);
    }
}
