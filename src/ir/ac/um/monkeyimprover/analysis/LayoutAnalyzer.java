package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.analysis.layout.LayoutInformationExtractor;
import ir.ac.um.monkeyimprover.analysis.project.ProjectInformationExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class LayoutAnalyzer {

    public LayoutAnalyzer() {
    }

    public List<VirtualFile> getLayoutFiles(VirtualFile projectBaseDirectory) {
        ProjectInformationExtractor projectInformationExtractor = new ProjectInformationExtractor();
        List<VirtualFile> layoutFiles = projectInformationExtractor.getLayoutXMLFiles(projectBaseDirectory);
        createBackup(projectBaseDirectory, layoutFiles);
        return layoutFiles;
    }

    private void createBackup(VirtualFile directory, List<VirtualFile> layoutFiles) {
        BackupCreator backupCreator = new BackupCreator();
        backupCreator.createBackup(directory, layoutFiles);
    }

}
