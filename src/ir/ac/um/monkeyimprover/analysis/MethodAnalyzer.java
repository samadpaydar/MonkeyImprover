package ir.ac.um.monkeyimprover.analysis;

import com.intellij.psi.*;

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
        cyclomatic = getCyclomaticComplexity(methodBody, cyclomatic);
        // if(methodBody!=null) {
        //   return methodBody.getStatements().length;
        //} else {
        // return 0;
        // }
        return cyclomatic;
    }

    private int getCyclomaticComplexity(PsiCodeBlock codeBlock, int cyclomatic) {
        if (codeBlock == null || codeBlock.getStatementCount() == 0) {
            // System.out.println(method.getText());
            return cyclomatic;
        }
        for (PsiStatement statement : codeBlock.getStatements()) {
            if (isBranch(statement)) {
                for (PsiElement element : statement.getChildren()) {
                    if (element instanceof PsiBlockStatement) {
                        PsiBlockStatement blockStatement = (PsiBlockStatement) element;
                        PsiCodeBlock childCodeBlock = blockStatement.getCodeBlock();
                        cyclomatic = getCyclomaticComplexity(childCodeBlock, ++cyclomatic);
                    }
                }
            }
        }

        return cyclomatic;
    }

    private boolean isBranch(PsiStatement statement) {
        boolean branchStatement = false;
        if (statement instanceof PsiIfStatement) {

            branchStatement = true;
        } else if (statement instanceof PsiForStatement) {
            branchStatement = true;
        } else if (statement instanceof PsiWhileStatement) {
            branchStatement = true;
        } else if (statement instanceof PsiDoWhileStatement) {
            branchStatement = true;
        } else if (statement instanceof PsiSwitchStatement) {

        } else if (statement instanceof PsiTryStatement) {

        }
        return branchStatement;
    }

}
