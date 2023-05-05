package com.tan.gen.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.tan.gen.util.Editor;

import javax.swing.*;
import java.io.File;

/**
 * 外部编辑器打开源文件
 * @autor qwop
 * @date 2020-05-11
 */
public class EditorAction extends AnAction {
    public static final String WINDOWS = "windows";
    public static final String LINUX = "linux";
    private String systemBrowser = "explorer";
    private String line;
    private boolean isWindows;
    private String configEditorPath = null; // 检查 editorPath 是否变更
    private boolean isEditplusRight;
    private String editplusPath;

    public EditorAction() {
        String os = System.getProperty( "sun.desktop" );
        if ( WINDOWS.equalsIgnoreCase(os) ){
            systemBrowser = "explorer";
            isWindows = true;
        }
        else if (LINUX.equalsIgnoreCase(os) ) {
            systemBrowser = "nautilus";
        }
        line = System.getProperty("line.separator", "\r\n");
    }

    public static void main(String[] args) {
        System.getProperties().list(System.out);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if ( project.isInitialized()  && project.isOpen() ){
            PsiElement theSelect = anActionEvent.getData(DataKeys.PSI_ELEMENT);
            VirtualFile[] datas = anActionEvent.getData(DataKeys.VIRTUAL_FILE_ARRAY);
            DataContext dataContext = anActionEvent.getDataContext();
            PsiFile psiFile = anActionEvent.getData(DataKeys.PSI_FILE);
            VirtualFile vf = anActionEvent.getData(DataKeys.VIRTUAL_FILE);
            if (vf != null) {
                editor(project, vf);
                return;
            }
            if (theSelect instanceof PsiClass) {
                PsiClass selClazz = (PsiClass) theSelect;
                editor(project, selClazz);
            } else if (theSelect instanceof PsiDirectory) {
                PsiDirectory dir = ((PsiDirectory) theSelect );
                PsiPackage thePackage = JavaDirectoryService.getInstance().getPackage(dir);
                editor(project, thePackage);
            } else if ( theSelect == null && null != datas && datas.length > 0 ){
                final PsiManager psiManager = PsiManager.getInstance( project );
                for (int i = 0; i < datas.length; i++) {
                    VirtualFile vFile = datas[i];
                    PsiFile vPsiFile = psiManager.findFile(vFile);
                    if (vFile.isDirectory()) {
                        PsiDirectory psiDir = psiManager.findDirectory(vFile);
                        PsiPackage thePackage = JavaDirectoryService.getInstance().getPackage(psiDir);
                        editor(project, thePackage);
                    } else if ( null != vPsiFile && vPsiFile instanceof PsiJavaFile ){
                        PsiJavaFile psiJavaFile = (PsiJavaFile) vPsiFile;
                        PsiClass[] classes = psiJavaFile.getClasses();
                        for (PsiClass aClass : classes) {
                            editor(project, aClass);
                        }
                    }
                }
            } else if ( theSelect == null && null != psiFile && psiFile instanceof PsiJavaFile){
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                PsiClass[] classes = psiJavaFile.getClasses();
                for (PsiClass aClass : classes) {
                    editor(project, aClass);
                }
            } else{
                JOptionPane.showMessageDialog( null, "请选择Java类或者包结构");
            }
        }
    }

    private void editor(Project project, PsiClass selClazz) {
        PsiFile containingFile = selClazz.getContainingFile();
        String canonicalPath = containingFile.getVirtualFile().getCanonicalPath();
        command( canonicalPath );
    }

    private void editor(Project project, VirtualFile vf) {
        String canonicalPath = vf.getCanonicalPath();
        command( canonicalPath );
    }


    private void editor(Project project, PsiPackage psiPackage) {
        PsiClass[] classes = psiPackage.getClasses();
        if (classes != null) {
            for (PsiClass aClass : classes) {
                editor(project, aClass);
            }
        }
    }


    private void command( final String location ) {
        if ( isWindows) { // 如果是 windows 则加载Editplus.
            loadEditplus();
        }

        if ( !isEditplusRight ) {
            JOptionPane.showMessageDialog( null, "编辑器文件未找到,请安装或配置!");
//			action.setEnabled(false);
            return;
        }


        if ( StringUtil.isEmpty( location ) ) {
            return;
        }
        StringBuffer command = new StringBuffer();
        Runtime runtime = Runtime.getRuntime();
        try {
            if ( isWindows ) {
                command.append(editplusPath)
                        .append(" \"")
                        .append(location)
                        .append("\"")
                ;
            }
            else {
                command.append(systemBrowser)
                        .append(" \"")
                        .append(location)
                        .append("\"")
                ;
            }
            runtime.exec(command.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog( null, e.getMessage());
            e.printStackTrace();
        } finally {
/*            log(new Object[]{
                    "command:", command,line
            });*/
            command = null;
            runtime = null;
        }
    }


    private void loadEditplus() {
        configEditorPath = "";

        // 如果配置了编辑器的路径
        if ( StringUtil.isNotEmpty( configEditorPath ) ) {

            if ( !checkEditplusRight( configEditorPath ) ) {
              /*  log(
                        new Object[]{
                                "编辑器配置有误！:", configEditorPath
                        }
                );*/
            }
        }

        editplusPath = configEditorPath;

        if ( StringUtil.isEmpty( editplusPath ) ) {
            editplusPath = new Editor().getEditplusPath();
            if ( checkEditplusRight( editplusPath ) ) {
                // add by qwop 2013 06 08
//                SourceManipulator.PREF_STORE.setValue( PreferenceConstants.EDITOR_PATH, editplusPath);
            }
        }

    }


    private boolean checkEditplusRight( final String path ) {
        if (StringUtil.isEmpty( path )) {

            return false ;
        } else {
            File f ;
            String absolutePath = path ;
            if (absolutePath.charAt(0) == '\"') {
                absolutePath = absolutePath.substring(1);
            }
            int len = absolutePath.length();
            if (absolutePath.charAt(len - 1) == '\"')  {
                absolutePath = absolutePath.substring(0, len - 1);
            }
            f = new File(absolutePath);
            if (f.exists() &&
                    f.isFile() &&
                    f.getName().toLowerCase().indexOf(".exe") >= 0) {
                isEditplusRight = true;
                return true;
            } else {
               /* log(
                        new Object[] {
                                "文件不存在或者是目录或者文件后缀名不为Exe:",editplusPath
                        }
                );*/
            }
            f = null;
            absolutePath = null;
        }

        return false;
    }

}
