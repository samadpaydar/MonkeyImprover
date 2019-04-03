package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Samad Paydar
 */
public class AsyncTaskAnalyzer extends JavaRecursiveElementVisitor {
    private List<String> asyncTaskClassNames;

    public AsyncTaskAnalyzer() {
        asyncTaskClassNames = new ArrayList<>();
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        try {
            PsiJavaCodeReferenceElement reference = expression.getClassReference();
            asyncTaskClassNames.add(reference.getClass().toString() + " " + reference.getQualifiedName());
            if(reference instanceof PsiClass){
                PsiClass theClass = (PsiClass) reference;

            }
            /*if (expression.getClassReference().getQualifiedName().equals("android.content.Intent")) {
                PsiExpressionList list = expression.getArgumentList();
                if(list.getExpressionCount()>1) {
                    PsiExpression secondArgument = list.getExpressions()[1];
                    String typeName = secondArgument.getType().getCanonicalText();
                    final String CLASS_PREFIX = "java.lang.Class";
                    if(typeName!= null && typeName.startsWith(CLASS_PREFIX)) {
                        typeName = typeName.replace(CLASS_PREFIX, "");
                        typeName = typeName.replace("<", "");
                        typeName = typeName.replace(">", "");
                        intentClassNames.add(typeName);
                    }
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getAsyncTaskClassNames() {
        return asyncTaskClassNames;
    }
}

