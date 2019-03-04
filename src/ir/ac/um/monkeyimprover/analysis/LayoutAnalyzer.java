package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.utils.Utils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LayoutAnalyzer {
    private MonkeyImprover monkeyImprover;

    public LayoutAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public List<VirtualFile> getLayoutFiles(VirtualFile directory) {
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

    public VirtualFile findRelatedJavaFileBasedOnName(VirtualFile directory, VirtualFile layoutXMLFile) {
        String layoutFileName = layoutXMLFile.getName();
        layoutFileName = layoutFileName.substring(0, layoutFileName.lastIndexOf('.'));
        String[] parts = layoutFileName.split("_");
        String relatedJavaFileName = "";
        for (String part : parts) {
            relatedJavaFileName = Utils.capitalize(part) + relatedJavaFileName;
        }
        relatedJavaFileName += ".java";
        return findRelatedJavaFile(directory, relatedJavaFileName);
    }

    private VirtualFile findRelatedJavaFile(VirtualFile directory, String relatedJavaFileName) {
        VirtualFile[] children = directory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                VirtualFile file = findRelatedJavaFile(child, relatedJavaFileName);
                if (file != null) {
                    return file;
                }
            } else if (child.getName().equals(relatedJavaFileName)) {
                return child;
            }
        }
        return null;
    }

    public List<String> getCallbackMethodNames(VirtualFile layoutXMLFile) {
        List<String> callbackMethodNames = new ArrayList<>();
        String path = layoutXMLFile.getCanonicalPath();
        File xmlFile = new File(path);
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                LayoutXMLFileHandler handler = new LayoutXMLFileHandler(this.monkeyImprover);
                saxParser.parse(xmlFile, handler);
                callbackMethodNames = handler.getCallbackMethodNames();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return callbackMethodNames;
    }

}
