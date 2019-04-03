package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.methods.CyclomaticComplexityAnalyzer;
import ir.ac.um.monkeyimprover.analysis.methods.IntentComplexityAnalyzer;
import ir.ac.um.monkeyimprover.analysis.methods.MethodCallAnalyzer;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;

import java.util.List;

public class MethodAnalyzer {
    private MonkeyImprover monkeyImprover;

    public MethodAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    private double getMethodComplexity(PsiMethod method) {
        double complexity = getCyclomaticComplexity(method);
        List<PsiMethod> calledMethods = getMethodsDirectlyCalledBy(method);

        monkeyImprover.showMessage("\t\t\tMethods Called by: " + method.getName());
        for(PsiMethod m: calledMethods) {
            monkeyImprover.showMessage("\t\t\t\t" + AnalysisUtils.getMethodQualifiedName(m));
        }
        for (PsiMethod calledMethod : calledMethods) {
            if (calledMethod.equals(method)) {
                //ignore recursive calls
            } else if (isLocalMethod(calledMethod)) {
                complexity += getMethodComplexity(calledMethod);
            } else {
                complexity += getAPIComplexity(calledMethod);
            }
        }
        complexity += getIntentComplexity(method);
        return complexity;
    }

    public CallbackMethodInfo getCallbackMethodInfo(String callbackMethodName, List<VirtualFile> relatedJavaFiles) {
        double complexity = -1;
        PsiMethod method = null;
        MethodFinder methodFinder = new MethodFinder();
        for (VirtualFile relatedJavaFile : relatedJavaFiles) {
            PsiFile file = PsiManager.getInstance(monkeyImprover.getProject()).findFile(relatedJavaFile);
            if (file != null && file instanceof PsiJavaFile) {
                PsiMethod relatedMethod = methodFinder.findMethodByName((PsiJavaFile) file, callbackMethodName);
                if (relatedMethod != null) {
                    method = relatedMethod;
                    complexity = getMethodComplexity(relatedMethod);
                    break;
                }
            }
        }
        return new CallbackMethodInfo(callbackMethodName, method, complexity);
    }

    private double getCyclomaticComplexity(PsiMethod method) {
        CyclomaticComplexityAnalyzer cyclomaticComplexityAnalyzer = new CyclomaticComplexityAnalyzer();
        return cyclomaticComplexityAnalyzer.getComplexity(method);
    }

    private double getIntentComplexity(PsiMethod method) {
        IntentComplexityAnalyzer intentComplexityAnalyzer = new IntentComplexityAnalyzer(monkeyImprover);
        return intentComplexityAnalyzer.getComplexity(method);
    }

    private boolean isLocalMethod(PsiMethod calledMethod) {
        String calledMethodClassName = calledMethod.getContainingClass().getQualifiedName();
        List<PsiClass> projectJavaClasses = monkeyImprover.getProjectJavaClasses();
        for (PsiClass projectJavaClass : projectJavaClasses) {
            String className = projectJavaClass.getQualifiedName();
            if (className != null && className.equals(calledMethodClassName)) {
                return true;
            }
        }
        return false;
    }

    private List<PsiMethod> getMethodsDirectlyCalledBy(PsiMethod method) {
        MethodCallAnalyzer methodCallAnalyzer = new MethodCallAnalyzer(method);
        method.accept(methodCallAnalyzer);
        return methodCallAnalyzer.getCalledMethods();
    }

    private double getAPIComplexity(PsiMethod calledMethod) {
        String calledMethodClassName = calledMethod.getContainingClass().getQualifiedName();
        String[] classNames = {"android.database.sqlite.SQLiteDatabase", "android.database.sqlite.SQLiteStatement"};
        double[] weights = {3.0, 0.5};
        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            if (className.equals(calledMethodClassName)) {
                return weights[i];
            }
        }
        return 1.0;
    }

}
