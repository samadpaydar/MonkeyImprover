package ir.ac.um.monkeyimprover.analysis;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInfo;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInformationExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Samad Paydar
 */
public class MonkeyImprover implements Runnable {
    private Project project;
    private PsiElement psiElement;
    private ConsoleView consoleView;
    private LayoutAnalyzer layoutAnalyzer;
    private ClassFinder classFinder;
    private MethodAnalyzer methodAnalyzer;
    private MethodFinder methodFinder;
    private List<PsiClass> projectJavaClasses;

    public MonkeyImprover(Project project, PsiElement psiElement, ConsoleView consoleView) {
        this.project = project;
        this.psiElement = psiElement;
        this.consoleView = consoleView;
        projectJavaClasses = new ArrayList<>();
    }

    @Override
    public void run() {
        showMessage("Started processing project " + project.getName());
        layoutAnalyzer = new LayoutAnalyzer();
        classFinder = new ClassFinder();
        methodFinder = new MethodFinder();
        methodAnalyzer = new MethodAnalyzer(this);
        showMessage("Collecting project Java classes...");
        collectProjectJavaClasses();
        LayoutRefactory layoutRefactory = new LayoutRefactory(this);
        showMessage("Extracting layouts files...");
        List<VirtualFile> layoutFiles = layoutAnalyzer.getLayoutFiles(project.getBaseDir());
        for (VirtualFile layoutFile : layoutFiles) {
            showMessage("Processing layouts file " + layoutFile.getName() + "...");
            List<CallbackMethodInfo> info = processLayoutFile(layoutFile);
            layoutRefactory.refactorLayout(new LayoutInfo(layoutFile, info));
        }

        showMessage("Finished");
    }

    private void collectProjectJavaClasses() {
        JavaClassCollector javaClassCollector = new JavaClassCollector();
        psiElement.accept(javaClassCollector);
        this.projectJavaClasses = javaClassCollector.getProjectJavaClasses();
    }

    public List<PsiClass> getProjectJavaClasses() {
        return this.projectJavaClasses;
    }

    private List<CallbackMethodInfo> processLayoutFile(VirtualFile layoutFile) {
        List<CallbackMethodInfo> infoList = new ArrayList<>();
        LayoutInformationExtractor layoutInformationExtractor = new LayoutInformationExtractor();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        List<String> callbackMethodNames = layoutInformationExtractor.getCallbackMethodNames(xmlFile);
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
