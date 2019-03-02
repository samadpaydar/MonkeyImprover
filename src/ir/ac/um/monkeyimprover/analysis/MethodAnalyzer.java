package ir.ac.um.monkeyimprover.analysis;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.impl.JavaFileTypeFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaFileImpl;

import java.util.ArrayList;
import java.util.List;

public class MethodAnalyzer {
    public MethodAnalyzer() {

    }

    /**
     * @param method
     * @return the complexity of the given method
     */
    public double getMethodComplexity(PsiMethod method) {
        return getMethodCyclomaticComplexity(method);
    }

    private int getMethodCyclomaticComplexity(PsiMethod method) {
//        TODO provide a correct implementation
        PsiCodeBlock methodBody = method.getBody();
        if(methodBody!=null) {
            return methodBody.getStatements().length;
        } else {
            return 0;
        }
    }

}
