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
        PsiStatement[] statements = codeBlock.getStatements();
        int length = statements.length;
        for (int i = 0; i < length; i++) {

            if (isBranch(statements[i])) {
                for (PsiElement element : statements[i].getChildren()) {
                    if (element instanceof PsiBlockStatement) {
                        PsiBlockStatement code = (PsiBlockStatement) element;
                        PsiCodeBlock met = code.getCodeBlock();
                        if (element.getChildren()[0].getChildren().length > 3) {
                            cyclomatic = getCyclomaticComplexity(met, ++cyclomatic);
                            if (element.getParent().getPrevSibling().getPrevSibling() instanceof PsiSwitchLabelStatement) {
                                cyclomatic--;
                            }
                        }
                    }
                    if (element instanceof PsiCodeBlock) {
                        PsiCodeBlock code = (PsiCodeBlock) element;
                        for (PsiStatement switchStatement : code.getStatements()) {
                            if (isBranch(switchStatement)) {
                                ++cyclomatic;
                            }
                        }

                        cyclomatic = getCyclomaticComplexity(code, ++cyclomatic);

                    }

                    if (element instanceof PsiIfStatement) {
                        if ((statements[i] instanceof PsiIfStatement) && (i < codeBlock.getStatementCount())) {
                            ++cyclomatic;
                        }
                        PsiIfStatement ifStatement = (PsiIfStatement) element;
                        PsiStatement[] stmt = new PsiStatement[statements.length + 1];
                        System.arraycopy(statements, 0, stmt, 0, statements.length);
                        stmt[stmt.length - 1] = ifStatement;
                        length = stmt.length;
                        statements = stmt;
                        ++cyclomatic;

                    }
                }
            }
        }
//        if (isBranch(statement)) {
//            for (PsiElement element : statement.getChildren()) {
//                if (element instanceof PsiBlockStatement) {
//                    PsiBlockStatement blockStatement = (PsiBlockStatement) element;
//                    PsiCodeBlock childCodeBlock = blockStatement.getCodeBlock();
//                    cyclomatic = getCyclomaticComplexity(childCodeBlock, ++cyclomatic);
//                }
//            }
//        }
//    }

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
            System.out.println(statement.getText());
            branchStatement = true;
        } else if (statement instanceof PsiSwitchLabelStatement) {
            branchStatement = true;
        }

        return branchStatement;
    }

}
