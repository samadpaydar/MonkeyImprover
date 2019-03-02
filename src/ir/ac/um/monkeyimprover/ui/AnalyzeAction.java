package ir.ac.um.monkeyimprover.ui;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.content.Content;
import ir.ac.um.monkeyimprover.utils.Constants;
import org.jetbrains.annotations.NotNull;
import ir.ac.um.monkeyimprover.analysis.MonkeyImprover;

public class AnalyzeAction extends AnAction {
    private ConsoleView consoleView;

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Navigatable navigatable = anActionEvent.getData(CommonDataKeys.NAVIGATABLE);
        if (navigatable != null) {
            Project project = anActionEvent.getProject();
            PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
            processProject(project, psiElement);
        }
    }

    private void processProject(Project project, PsiElement psiElement) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(Constants.PLUGIN_NAME);
        if (consoleView == null) {
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), Constants.PLUGIN_NAME, true);
            toolWindow.getContentManager().addContent(content);
        }
        toolWindow.show(null);
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runReadAction(new MonkeyImprover(project, psiElement, consoleView));
            }
        });

    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
        boolean enabled = project != null && (psiElement instanceof PsiDirectory)
                && ((PsiDirectory) psiElement).getVirtualFile().getCanonicalPath().equals(project.getBaseDir().getCanonicalPath());
        anActionEvent.getPresentation().setEnabledAndVisible(enabled);
    }

}
