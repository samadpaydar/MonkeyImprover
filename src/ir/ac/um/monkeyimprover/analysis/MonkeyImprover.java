package ir.ac.um.monkeyimprover.analysis;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.classes.ClassFinder;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInfo;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInformationExtractor;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;
import ir.ac.um.monkeyimprover.analysis.methods.MethodAnalyzer;
import ir.ac.um.monkeyimprover.analysis.methods.MethodFinder;
import ir.ac.um.monkeyimprover.analysis.project.BackupCreator;
import ir.ac.um.monkeyimprover.analysis.project.ProjectInformationExtractor;

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
        classFinder = new ClassFinder();
        methodFinder = new MethodFinder();
        methodAnalyzer = new MethodAnalyzer(this);
        showMessage("Collecting project Java classes...");
        ProjectInformationExtractor projectInformationExtractor = new ProjectInformationExtractor(psiElement);
        this.projectJavaClasses = projectInformationExtractor.getProjectJavaClasses();
        showMessage("Extracting layouts files...");
        List<VirtualFile> layoutFiles = projectInformationExtractor.getLayoutXMLFiles(project.getBaseDir());
        createBackup(project.getBaseDir(), layoutFiles);
        for (VirtualFile layoutFile : layoutFiles) {
            showMessage("Processing layouts file " + layoutFile.getName() + "...");
            List<CallbackMethodInfo> info = processLayoutFile(layoutFile);
            refactorLayout(new LayoutInfo(layoutFile, info));
        }

        showMessage("Finished");
    }

    private void refactorLayout(LayoutInfo layoutInfo) {
        VirtualFile layoutFile = layoutInfo.getLayoutFile();
        List<CallbackMethodInfo> callbackMethodInfoList = layoutInfo.getCallbackMethodInfoList();
        String path = layoutFile.getCanonicalPath();
        File xmlFile = new File(path);
        if (xmlFile.exists() && xmlFile.isFile()) {
            LayoutXMLHandlerForRefactory handler = new LayoutXMLHandlerForRefactory(this, xmlFile, callbackMethodInfoList);
            handler.run();
        }
    }

    private void createBackup(VirtualFile directory, List<VirtualFile> layoutFiles) {
        BackupCreator backupCreator = new BackupCreator();
        backupCreator.createBackup(directory, layoutFiles);
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
