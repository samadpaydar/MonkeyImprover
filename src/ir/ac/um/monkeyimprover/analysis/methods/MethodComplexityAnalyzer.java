package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;

import java.util.List;

public class MethodComplexityAnalyzer {
    private MonkeyImprover monkeyImprover;

    public MethodComplexityAnalyzer(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public double getComplexity(PsiMethod method) {
        //false is passed to prevent from infinite loop, when this method is called from ClassComplexityAnalyzer
        return getComplexity(method, false);
    }

    private double getComplexity(PsiMethod method, boolean includeCalledLocalMethods) {
        double cyclomaticComplexity = getCyclomaticComplexity(method);
        List<PsiMethod> calledMethods = getMethodsDirectlyCalledBy(method);

        calledMethods = replaceAbstractsWithConcretes(calledMethods);

        double calledMethodComplexity = 0.0;
        for (PsiMethod calledMethod : calledMethods) {
            if (calledMethod.equals(method)) {
                //ignore recursive calls
            } else if (isLocalMethod(calledMethod)){
                if(includeCalledLocalMethods) {
                    calledMethodComplexity += getComplexity(calledMethod, includeCalledLocalMethods);
                }
            } else {
                calledMethodComplexity += getAPIComplexity(calledMethod);
            }
        }
        double intentComplexity = 0.0;
        if (includeCalledLocalMethods) {
            intentComplexity = getIntentComplexity(method);
        }
        double asyncComplexity = getAsyncTaskComplexity(method);

        double complexity = cyclomaticComplexity + calledMethodComplexity + intentComplexity + asyncComplexity;
//        if (AnalysisUtils.getMethodQualifiedName(method).startsWith("ir.ac.um.pardisban.MainActivity_")) {
//            monkeyImprover.showMessage("\tmethod " + AnalysisUtils.getMethodQualifiedName(method)
//                    + " cyclomaticComplexity: " + cyclomaticComplexity
//                    + " calledMethodComplexity: " + calledMethodComplexity
//                    + " intentComplexity: " + intentComplexity
//                    + " asyncComplexity: " + asyncComplexity
//                    + " complexity: " + complexity);
//        }
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
                    complexity = getComplexity(relatedMethod, true);
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

    private double getAsyncTaskComplexity(PsiMethod method) {
        AsyncTaskComplexityAnalyzer asyncTaskComplexityAnalyzer = new AsyncTaskComplexityAnalyzer(monkeyImprover);
        return asyncTaskComplexityAnalyzer.getComplexity(method);
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

//    TODO Since polymorphism is used in the database-related methods, the Android database API methods are not directly called
//    hence, this method does not match anything
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
        return 0.0;
    }

    /**
     * this method addresses the problem caused by interfaces and abstract classes.
     * When a method calls a method which belongs to an interface, it is required to look for
     * the concrete implementation of the abstract method.
     * If this is not considered, then using API Complexity concept is not effective
     * @param methods
     * @return
     */
    private List<PsiMethod> replaceAbstractsWithConcretes(List<PsiMethod> methods) {
        for(PsiMethod method: methods) {
            PsiClass theClass = method.getContainingClass();
            if(theClass.isInterface() || isAbstract(method)) {
               // monkeyImprover.showMessage("\tcallMethod: " + AnalysisUtils.getMethodQualifiedName(callMethod) + " class: " + theClass.getQualifiedName() );
            }

        }
        return methods;
    }

    private boolean isAbstract(PsiMethod method) {
        JvmModifier[] modifiers = method.getModifiers();
        for(JvmModifier modifier: modifiers) {
            monkeyImprover.showMessage("modifier: " + modifier.toString());
        }
        return false;
    }
}
