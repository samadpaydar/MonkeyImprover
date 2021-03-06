package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;

public class CyclomaticComplexityAnalyzer {

    public int getComplexity(PsiMethod method) {
        PsiCodeBlock methodBody = method.getBody();

        //TODO modify the implementation
        /* for a simple method that has if-then-else statement, complexity should be 2, but 3 is returned.
         the reason is that the default value of 1 is added to the complexity of the method.
         if a method has no branch, its complexity should be 1, but if it has some branches, the initial value of complexity should be 0.
        */
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
