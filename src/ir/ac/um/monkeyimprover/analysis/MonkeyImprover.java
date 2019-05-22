package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInfo;
import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInformationExtractor;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;
import ir.ac.um.monkeyimprover.analysis.project.BackupCreator;
import ir.ac.um.monkeyimprover.analysis.project.ProjectInformationExtractor;
import ir.ac.um.monkeyimprover.analysis.utils.Utils;

import java.io.File;
import java.util.List;


/**
 * @author Samad Paydar
 */
public class MonkeyImprover implements Runnable {
    private Project project;
    private PsiElement psiElement;
    private List<PsiClass> projectJavaClasses;

    public MonkeyImprover(Project project, PsiElement psiElement) {
        this.project = project;
        this.psiElement = psiElement;
    }

    @Override
    public void run() {
        Utils.showMessage("Started processing project " + project.getName());
        Utils.showMessage("Collecting project Java classes...");
        ProjectInformationExtractor projectInformationExtractor = new ProjectInformationExtractor(psiElement);
        this.projectJavaClasses = projectInformationExtractor.getProjectJavaClasses();
        Utils.showMessage("Extracting layouts files...");
        List<VirtualFile> layoutFiles = projectInformationExtractor.getLayoutXMLFiles(project.getBaseDir());
        Utils.showMessage("Creating backup for layout files...");
        createBackup(project.getBaseDir(), layoutFiles);
        Utils.showMessage("Refactorying layout files...");
        LayoutInformationExtractor layoutInformationExtractor = new LayoutInformationExtractor(this);
        for (VirtualFile layoutFile : layoutFiles) {
            Utils.showMessage("\tLayout " + layoutFile.getName());
            List<CallbackMethodInfo> info = layoutInformationExtractor.getCallbackMethodInfos(project.getBaseDir(), layoutFile);

            refactorLayout(new LayoutInfo(layoutFile, info));
        }

        Utils.showMessage("Finished");
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
