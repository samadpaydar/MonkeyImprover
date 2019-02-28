package ir.ac.um.monkeyimprover.analysis;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.vfs.VirtualFile;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MonkeyImprover implements Runnable {
    private Project project;
    private ConsoleView consoleView;

    public MonkeyImprover(Project project, ConsoleView consoleView) {
        this.project = project;
        this.consoleView = consoleView;
    }

    @Override
    public void run() {
        update("started processing project " + project.getName());
        VirtualFile baseDirectory = project.getBaseDir();
        List<VirtualFile> layoutFiles = getLayoutFiles(baseDirectory);
        update("layout files:");
        for (VirtualFile layoutFile : layoutFiles) {
            update(layoutFile.getName());
        }
        update("Finished");
        List<String> callbackMethodNames = getCallbackMethodNames(layoutFiles);
        for(String callbackMethodName: callbackMethodNames) {
            update("Callback Method Name: " + callbackMethodName);
        }
    }

    private List<String> getCallbackMethodNames(List<VirtualFile> layoutXMLFiles) {
        List<String> callbackMethodNames = new ArrayList<>();
        for (VirtualFile layoutXMLFile : layoutXMLFiles) {
            callbackMethodNames.addAll(getCallbackMethodNames(layoutXMLFile));
        }
        return callbackMethodNames;
    }

    private List<String> getCallbackMethodNames(VirtualFile layoutXMLFile) {
        List<String> callbackMethodNames = new ArrayList<>();
        String path = layoutXMLFile.getCanonicalPath();
        File xmlFile = new File(path);
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                LayoutXMLFileHandler handler = new LayoutXMLFileHandler(this);
                saxParser.parse(xmlFile, handler);
                callbackMethodNames = handler.getCallbackMethodNames();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return callbackMethodNames;
    }

    private List<VirtualFile> getLayoutFiles(VirtualFile directory) {
        List<VirtualFile> layoutFiles = new ArrayList<>();
        VirtualFile[] children = directory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                if (child.getName().equals("layout")) {
                    layoutFiles = getLayoutXMLFiles(child);
                    break;
                } else {
                    List<VirtualFile> innerLayoutFiles = getLayoutFiles(child);
                    layoutFiles.addAll(innerLayoutFiles);
                }
            }
        }
        return layoutFiles;
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

    public void update(String message) {
        consoleView.print(String.format("%s%n", message),
                ConsoleViewContentType.NORMAL_OUTPUT);
    }
}
