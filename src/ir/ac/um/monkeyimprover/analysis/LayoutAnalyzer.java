package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class LayoutAnalyzer {

    public LayoutAnalyzer() {
    }

    public List<VirtualFile> getLayoutFiles(VirtualFile projectBaseDirectory) {
        VirtualFile srcDirectory = getSourceDirectory(projectBaseDirectory);
        VirtualFile resourcesDirectory = getResourcesDirectory(srcDirectory);
        VirtualFile layoutDirectory = getLayoutDirectory(resourcesDirectory);
        List<VirtualFile> layoutFiles = getLayoutXMLFiles(layoutDirectory);
        createBackup(projectBaseDirectory, layoutFiles);
        return layoutFiles;
    }

    private void createBackup(VirtualFile directory, List<VirtualFile> layoutFiles) {
        BackupCreator backupCreator = new BackupCreator();
        backupCreator.createBackup(directory, layoutFiles);
    }


    private VirtualFile getChildDirectory(String childDirectoryName, VirtualFile parentDirectory) {
        VirtualFile result = null;
        VirtualFile[] children = parentDirectory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                if (child.getName().equals(childDirectoryName)) {
                    result = child;
                    break;
                } else {
                    VirtualFile temp = getChildDirectory(childDirectoryName, child);
                    if (temp != null) {
                        result = temp;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private VirtualFile getSourceDirectory(VirtualFile directory) {
        return getChildDirectory("src", directory);
    }

    private VirtualFile getResourcesDirectory(VirtualFile directory) {
        return getChildDirectory("res", directory);
    }

    private VirtualFile getLayoutDirectory(VirtualFile directory) {
        return getChildDirectory("layout", directory);
    }

    private List<VirtualFile> getLayoutXMLFiles(VirtualFile layoutDirectory) {
        List<VirtualFile> layoutFiles = new ArrayList<>();
        VirtualFile[] children = layoutDirectory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                List<VirtualFile> innerLayoutFiles = getLayoutXMLFiles(child);
                layoutFiles.addAll(innerLayoutFiles);
            } else if (child.getName().toLowerCase().endsWith(".xml")) {
                layoutFiles.add(child);
            }
        }
        return layoutFiles;
    }

    public List<String> getCallbackMethodNames(VirtualFile layoutXMLFile) {
        File xmlFile = new File(layoutXMLFile.getCanonicalPath());
        LayoutInformationExtractor layoutInformationExtractor = new LayoutInformationExtractor();
        return layoutInformationExtractor.getCallbackMethodNames(xmlFile);
    }

    public List<String> getContextClassNames(VirtualFile layoutXMLFile) {
        File xmlFile = new File(layoutXMLFile.getCanonicalPath());
        LayoutInformationExtractor layoutInformationExtractor = new LayoutInformationExtractor();
        return layoutInformationExtractor.getContextClassNames(xmlFile);
    }
}
