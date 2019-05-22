package ir.ac.um.monkeyimprover.analysis.methods;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.lang.jvm.JvmParameter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;

import java.util.ArrayList;
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
            } else if (isLocalMethod(calledMethod)) {
                calledMethodComplexity += getComplexity(calledMethod, includeCalledLocalMethods);
            } else {
                calledMethodComplexity += getAPIComplexity(calledMethod);
            }
        }
        double intentComplexity = 0.0;
        double asyncComplexity = 0.0;
        if (includeCalledLocalMethods) {
            intentComplexity = getIntentComplexity(method);
            asyncComplexity = getAsyncTaskComplexity(method);
        }
        double complexity = cyclomaticComplexity + calledMethodComplexity + intentComplexity + asyncComplexity;
//        if (Utils.getMethodQualifiedName(method).startsWith("ir.ac.um.pardisban.MainActivity_")) {
//            monkeyImprover.showMessage("\tmethod " + Utils.getMethodQualifiedName(method)
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
        String[] classNames = {
                "android.database.sqlite.SQLiteDatabase",
                "android.database.sqlite.SQLiteStatement",
                "android.database.Cursor",
                "java.net.URLConnection",
                "java.net.HttpURLConnection",
        };
        double[] weights = {3.0, 3.0, 3.0, 5.0, 5.0};
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
     *
     * @param methods
     * @return
     */
    private List<PsiMethod> replaceAbstractsWithConcretes(List<PsiMethod> methods) {
        for (int i = 0; i < methods.size(); i++) {
            PsiMethod method = methods.get(i);
            if (!isLocalMethod(method)) {
                continue;
            }
            PsiClass theClass = method.getContainingClass();
            if (theClass.isInterface()) {
                List<PsiClass> implementingClasses = getImplementingClasses(theClass);
                if (!implementingClasses.isEmpty()) {
                    for (PsiClass implementingClass : implementingClasses) {
                        PsiMethod concreteMethod = getConcreteMethod(implementingClass, method);
                        if (concreteMethod != null) {
                            methods.set(i, concreteMethod);
                        }
                    }
                }
            } else if (isAbstract(method)) {
//                TODO complete code
            }
        }
        return methods;
    }

    private boolean isAbstract(PsiMethod method) {
        JvmModifier[] modifiers = method.getModifiers();
        for (JvmModifier modifier : modifiers) {
            if (modifier.toString().equalsIgnoreCase("abstract")) {
                return true;
            }
        }
        return false;
    }

    private List<PsiClass> getImplementingClasses(PsiClass theInterface) {
        List<PsiClass> implementingClasses = new ArrayList<>();
        List<PsiClass> projectClasses = monkeyImprover.getProjectJavaClasses();
        for (PsiClass projectClass : projectClasses) {
            PsiClassType[] types = projectClass.getImplementsListTypes();
            for (PsiClassType type : types) {
                if (type.getClassName().equals(theInterface.getName())) {
                    implementingClasses.add(projectClass);
                }
            }
        }
        return implementingClasses;
    }

    private PsiMethod getConcreteMethod(PsiClass theClass, PsiMethod method) {
        PsiMethod[] concreteClassMethods = theClass.getMethods();
        for (PsiMethod concreteMethod : concreteClassMethods) {
            if (matches(concreteMethod, method)) {
                return concreteMethod;
            }
        }
        return null;
    }

    private boolean matches(PsiMethod method1, PsiMethod method2) {
        if (method1.getName().equals(method2.getName())) {
            JvmParameter[] parameters1 = method1.getParameters();
            JvmParameter[] parameters2 = method2.getParameters();
            boolean match = true;
            if (parameters1 != null && parameters2 != null) {
                match = parameters1.length == parameters2.length;
                if (match) {
                    for (int i = 0; i < parameters1.length; i++) {
                        JvmParameter parameter1 = parameters1[i];
                        JvmParameter parameter2 = parameters2[i];
                        if (!parameter1.getType().equals(parameter2.getType())) {
                            match = false;
                            break;
                        }
                    }
                }
            }
            return match;
        }
        return false;
    }
}
