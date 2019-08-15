package ir.ac.um.monkeyimprover.analysis.utils;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import ir.ac.um.monkeyimprover.utils.Constants;
import ir.ac.um.monkeyimprover.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Samad Paydar
 */
public class AnalysisUtils {



    public static String getMethodQualifiedName(PsiMethod method) {
        String result = null;
        try {
            String methodSemiQualifiedName = getMethodSemiQualifiedName(method);
            String className = method.getContainingClass().getQualifiedName();
            if (className == null) {
                return "AnonymousClass_" + methodSemiQualifiedName;
            }
            result = className + Constants.UNDERLINE_CHAR + methodSemiQualifiedName;
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
        return result;
    }

    private static String prepareName(String name) {
        if (name != null) {
            name = name.replace(Constants.DOT_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.LEFT_BRACKET_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.RIGHT_BRACKET_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.LESS_THAN_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.GREATER_THAN_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.COMMA_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.BLANK_SPACE_CHAR, Constants.UNDERLINE_CHAR);
        }
        return name;
    }


    private static String getMethodSemiQualifiedName(PsiMethod method) {
        StringBuilder methodNameBuilder = new StringBuilder();
        methodNameBuilder.append(method.getName());
        try {
            if (method.getParameters().length > 0) {
                for (PsiParameter parameter : method.getParameterList().getParameters()) {
                    String parameterType = parameter.getTypeElement().getType().getCanonicalText();
                    parameterType = AnalysisUtils.prepareName(parameterType);
                    methodNameBuilder.append(Constants.UNDERLINE_CHAR).append(parameterType);
                }
            } else {
                methodNameBuilder.append(Constants.UNDERLINE_CHAR);
            }
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
        return methodNameBuilder.toString();
    }

    public static int getPhysicalLOC(String code) {
        Matcher matcher = Pattern.compile("\r\n|\r|\n").matcher(code);
        int lines = 1;
        while (matcher.find()) {
            lines++;
        }
        return lines;
    }

    public static boolean isAnAndroidView(String elementType) {
        String[] nonViewTypes = {"LinearLayout", "ScrollView", "GridLayout", "CardView"};
        for (String viewType : nonViewTypes) {
            if (viewType.equals(elementType) || (elementType != null && elementType.endsWith(viewType))) {
                return false;
            }
        }
        String[] viewTypes = {"TextView", "EditText", "Button", "ImageView",
                "ImageButton", "CheckBox", "RadioButton", "RadioGroup", "Spinner",
                "AutoCompleteTextView", "View"};
        for (String viewType : viewTypes) {
            if (viewType.equals(elementType) || (elementType != null && elementType.endsWith(viewType))) {
                return true;
            }
        }
        return false;
    }

//    TODO complete this list
    public static boolean isAnAndroidInputView(String elementType) {
        String[] inputViewTypes = {"EditText", "CheckBox", "RadioButton", "RadioGroup", "Spinner",
                "AutoCompleteTextView"};
        for (String viewType : inputViewTypes) {
            if (viewType.equals(elementType) || (elementType != null && elementType.endsWith(viewType))) {
                return true;
            }
        }
        return false;
    }
}

