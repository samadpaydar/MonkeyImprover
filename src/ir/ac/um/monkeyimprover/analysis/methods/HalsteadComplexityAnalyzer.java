package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.classes.ClassComplexityAnalyzer;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HalsteadComplexityAnalyzer {

    Set<String> operatorSet;
    Set<String> operandSet;

    List<String> operatorList;
    List<String> operandList;

    private String[] operatorElements={
            "{",
            "}",
            "(",
            ")",
            ";",
            ":",
            ",",
            "[",
            "]",
            "++",
            "--",
            "+",
            "-",
            "!",
            "*",
            "/",
            "%",
            "<",
            ">",
            "<=",
            ">=",
            "==",
            "!=",
            "&",
            "&&",
            "|",
            "||",
            "?",
            "=",
            "+=",
            "-=",
            "*=",
            "/=",
            "%="
    };

    boolean operatorSearch(String token){
        for(String element : operatorElements){
            if(element.equalsIgnoreCase(token))
                return true;
        }

        return false;
    }

    boolean operandSearch(String token){
        for(String element : operandSet){
            if(element.equalsIgnoreCase(token))
                return true;
        }

        return false;
    }



    public double getComplexity(PsiMethod method) {

        operatorSet = new HashSet<>();
        operandSet = new HashSet<>();

        operatorList = new ArrayList<>();
        operandList = new ArrayList<>();


        // detecting just unique variables
        method.getBody().accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitVariable(PsiVariable variable) {
                super.visitVariable(variable);

                operandSet.add(variable.getName());
            }
        });

        // detecting all keywords
        method.getBody().accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitKeyword(PsiKeyword keyword) {
                super.visitKeyword(keyword);

                operatorSet.add(keyword.getText());
                operatorList.add(keyword.getText());

            }
        });

        // detecting all constants
        method.getBody().accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitLiteralExpression(PsiLiteralExpression expression) {
                super.visitLiteralExpression(expression);

                operandSet.add(expression.getText());
            }
        });








        method.getBody().accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitJavaToken(PsiJavaToken token) {
                super.visitJavaToken(token);

                if(operatorSearch(token.getText())){
                    operatorList.add(token.getText());
                    operatorSet.add(token.getText());
                }

                if(operandSearch(token.getText())){
                    operandList.add(token.getText());
                }

            }
        });


        int N1 = operatorList.size();
        int n1 = operatorSet.size();

        int N2 = operandList.size();
        int n2 = operandSet.size();

        double result = 0;
        if(n2 != 0)
            result =  ((double) n1/2) * ((double) N2/n2);





        return result;
    }





}
