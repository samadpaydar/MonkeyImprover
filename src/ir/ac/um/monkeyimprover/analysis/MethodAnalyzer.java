package ir.ac.um.monkeyimprover.analysis;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.impl.JavaFileTypeFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.*;
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
        int cyclomatic = 1;
        cyclomatic = getCyclomaticComplexity(methodBody,cyclomatic);
       // if(methodBody!=null) {
         //   return methodBody.getStatements().length;
        //} else {
           // return 0;
       // }
        return cyclomatic;
    }
    private int getCyclomaticComplexity(PsiCodeBlock methodBody,int cyclomatic){
        if(methodBody == null || methodBody.getStatementCount()==0) {
            // System.out.println(method.getText());
            return cyclomatic;
        }
        for(PsiStatement statement:methodBody.getStatements()){
            if(checkStatement(statement)) {
                for (PsiElement element : statement.getChildren()) {
                    if(element instanceof PsiBlockStatement){
                        PsiBlockStatement code = (PsiBlockStatement) element;
                        PsiCodeBlock met = code.getCodeBlock();
                        cyclomatic = getCyclomaticComplexity(met,++cyclomatic);

                    }
                }
            }
        }

        return cyclomatic;
    }
    private boolean checkStatement(PsiStatement statement){
        boolean branchStatement = false;
        if(statement instanceof PsiIfStatement){

            branchStatement = true;
        }
        else if(statement instanceof PsiForStatement){
            branchStatement = true;
        }
        else if(statement instanceof PsiWhileStatement){
            branchStatement = true;
        }
        else if(statement instanceof PsiDoWhileStatement){
            branchStatement = true;
        }
        else if(statement instanceof PsiSwitchStatement){

        }
        else if(statement instanceof PsiTryStatement){

        }
        return branchStatement;
    }

}
