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
import sun.font.GlyphLayout;

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
    private List<PsiClass> projectJavaClasses;

    public MonkeyImprover(Project project, PsiElement psiElement, ConsoleView consoleView) {
        this.project = project;
        this.psiElement = psiElement;
        this.consoleView = consoleView;
    }

    @Override
    public void run() {
        showMessage("Started processing project " + project.getName());
        showMessage("Collecting project Java classes...");
        ProjectInformationExtractor projectInformationExtractor = new ProjectInformationExtractor(psiElement);
        this.projectJavaClasses = projectInformationExtractor.getProjectJavaClasses();
        showMessage("Extracting layouts files...");
        List<VirtualFile> layoutFiles = projectInformationExtractor.getLayoutXMLFiles(project.getBaseDir());
        showMessage("Creating backup for layout files...");
        createBackup(project.getBaseDir(), layoutFiles);
        showMessage("Refactorying layout files...");
        LayoutInformationExtractor layoutInformationExtractor = new LayoutInformationExtractor(this);
        for (VirtualFile layoutFile : layoutFiles) {
            showMessage("\tLayout " + layoutFile.getName());
            List<CallbackMethodInfo> info = layoutInformationExtractor.getCallbackMethodInfos(project.getBaseDir(), layoutFile);
            refactorLayout(new LayoutInfo(layoutFile, info));
        }

        showMessage("Finished");
    }

    public List<PsiClass> getProjectJavaClasses() {
        return this.projectJavaClasses;
    }

    private void refactorLayout(LayoutInfo layoutInfo) {
        VirtualFile layoutFile = layoutInfo.getLayoutFile();
        List<CallbackMethodInfo> callbackMethodInfos = layoutInfo.getCallbackMethodInfoList();
        File xmlFile = new File(layoutFile.getCanonicalPath());
        RefactoryEngine refactoryEngine = new RefactoryEngine(this);
        refactoryEngine.refactorLayout(xmlFile, callbackMethodInfos);
    }

    private void createBackup(VirtualFile directory, List<VirtualFile> layoutFiles) {
        BackupCreator backupCreator = new BackupCreator();
        backupCreator.createBackup(directory, layoutFiles);
    }

    public void showMessage(String message) {
        consoleView.print(String.format("%s%n", message),
                ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public Project getProject() {
        return project;
    }

    public PsiClass getProjectClassByName(String className) {
        for (PsiClass projectJavaClass : projectJavaClasses) {
            if (projectJavaClass.getQualifiedName().equals(className)) {
                return projectJavaClass;
            }
        }
        return null;
    }

}
