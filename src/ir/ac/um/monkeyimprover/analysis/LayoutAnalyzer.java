package ir.ac.um.monkeyimprover.analysis;

import com.intellij.openapi.vfs.VirtualFile;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class LayoutAnalyzer {

    public LayoutAnalyzer() {
    }

    public List<VirtualFile> getLayoutFiles(VirtualFile directory) {
        VirtualFile layoutDirectory = getLayoutDirectory(directory);
        return getLayoutXMLFiles(layoutDirectory);
    }

    private VirtualFile getLayoutDirectory(VirtualFile directory) {
        VirtualFile layoutDirectory = null;
        VirtualFile[] children = directory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                if (child.getName().equals("layout")) {
                    layoutDirectory = child;
                    break;
                } else {
                    VirtualFile temp = getLayoutDirectory(child);
                    if (temp != null) {
                        layoutDirectory = child;
                        break;
                    }
                }
            }
        }
        return layoutDirectory;
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
        List<String> callbackMethodNames = new ArrayList<>();
        String path = layoutXMLFile.getCanonicalPath();
        File xmlFile = new File(path);
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                LayoutXMLHandler handler = new LayoutXMLHandler();
                saxParser.parse(xmlFile, handler);
                callbackMethodNames = handler.getCallbackMethodNames();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return callbackMethodNames;
    }

    public List<String> getContextClassNames(VirtualFile layoutXMLFile) {
        List<String> contextClassNames = new ArrayList<>();
        String path = layoutXMLFile.getCanonicalPath();
        File xmlFile = new File(path);
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                LayoutXMLHandler handler = new LayoutXMLHandler();
                saxParser.parse(xmlFile, handler);
                contextClassNames.addAll(handler.getContexts());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return contextClassNames;
    }
}
