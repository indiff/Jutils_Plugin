/* * Classname
   * 1.0.0
   * Date
   * Copyright (c) 2007-2019 qwop版权所有
   *
 */
package com.tan.gen.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.tan.gen.util.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 批量生成无参的构造方法
 * @autor qwop
 * @date 2020-05-11
 */
public class BatchGenNoArgConstructorAction extends AnAction {

    /**
     * 菜单点击操作处理
     * @param anActionEvent
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if ( project.isInitialized()  && project.isOpen() ){
            PsiElement theSelect = anActionEvent.getData(DataKeys.PSI_ELEMENT);
            VirtualFile[] datas = anActionEvent.getData(DataKeys.VIRTUAL_FILE_ARRAY);
            PsiFile psiFile = anActionEvent.getData(DataKeys.PSI_FILE);
            VirtualFile vf = anActionEvent.getData(DataKeys.VIRTUAL_FILE);
            // 如果选择了java文件
            if (theSelect instanceof PsiClass ) {
                PsiClass selClazz = (PsiClass) theSelect;
                gen4class(project, selClazz);
            } else if (theSelect instanceof PsiDirectory) {  // 如果选择是目录结构，则遍历包结构
                PsiDirectory dir = ((PsiDirectory) theSelect );
                PsiPackage thePackage = JavaDirectoryService.getInstance().getPackage(dir);
                gen4package(project, thePackage);
            } else if ( theSelect == null && null != datas && datas.length > 0 ){  // 如果选择是目录结构，则遍历包结构
                final PsiManager psiManager = PsiManager.getInstance( project );
                for (int i = 0; i < datas.length; i++) {
                    VirtualFile vFile = datas[i];
                    PsiFile vPsiFile = psiManager.findFile(vFile);
                    if (vFile.isDirectory()) {
                        PsiDirectory psiDir = psiManager.findDirectory(vFile);
                        PsiPackage thePackage = JavaDirectoryService.getInstance().getPackage(psiDir);
                        gen4package(project, thePackage);
                    } else if ( null != vPsiFile && vPsiFile instanceof PsiJavaFile ){
                        PsiJavaFile psiJavaFile = (PsiJavaFile) vPsiFile;
                        PsiClass[] classes = psiJavaFile.getClasses();
                        for (PsiClass aClass : classes) {
                            gen4class(project, aClass);
                        }
                    }
                }
            } else if ( theSelect == null && null != psiFile && psiFile instanceof PsiJavaFile){
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                PsiClass[] classes = psiJavaFile.getClasses();
                for (PsiClass aClass : classes) {
                    gen4class(project, aClass);
                }
            } else{
                JOptionPane.showMessageDialog( null, "请选择Java类或者包结构");
            }
        }
    }


    private PsiClass findExtensionClass( PsiElement element )
    {
        PsiFile containingFile = element.getContainingFile();
        if( !(containingFile instanceof PsiJavaFileImpl) )
        {
            return null;
        }

        PsiJavaFileImpl file = (PsiJavaFileImpl)containingFile;
        for( PsiClass psiClass : file.getClasses() )
        {

        }

        return null;
    }

    /**
     * 通过包进行遍历java文件
     * @param project
     * @param thePackage
     */
    private void gen4package(Project project, PsiPackage thePackage) {
        if ( null != thePackage ) {
            PsiClass[] clazzs = thePackage.getClasses();
            if (clazzs == null || clazzs.length == 0) {
                JOptionPane.showMessageDialog( null, "包目录未找到Java类");
            } else{
                for (PsiClass clazz : clazzs) {
                    gen4class(project, clazz);
                }
            }
        }
    }

    /**
     * 针对java class进行构造无参构造方法
     * @param project
     * @param clazz
     */
    private void gen4class(Project project, PsiClass clazz) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);

        final Runnable genRunnable = () -> {
            PsiMethod constructor  = elementFactory.createConstructor(  clazz.getName() );
            PsiMethod[] constructors = clazz.getConstructors();
            if (constructors == null || constructors.length == 0 ) {
                clazz.add(constructor);
                Toast.make(project, MessageType.INFO, clazz.getName() + "：添加无参构造方法成功" );
            }
            else {
                constructors[0].replace(constructor);
                Toast.make(project, MessageType.INFO, clazz.getName() + "：替换无参构造方法成功" );
            }

//            String commentText = "/**\n" +
//                    "* 无参构造方法()\n" +
//                    "*/";
//
//            PsiDocComment psiDocComment = elementFactory.createDocCommentFromText(commentText, null);
//            clazz.addBefore(psiDocComment, constructor);
        };
        WriteCommandAction.runWriteCommandAction(project, genRunnable);
    }


    private PsiStatement findSetContentView(PsiClass mClass){
        PsiStatement result = null;
        PsiMethod onCreate = mClass.findMethodsByName("onCreate", false)[0];
        for (PsiStatement statement : onCreate.getBody().getStatements()) {
            if (statement.getFirstChild() instanceof PsiMethodCallExpression) {
                PsiReferenceExpression methodExpression
                        = ((PsiMethodCallExpression) statement.getFirstChild())
                        .getMethodExpression();
                if (methodExpression.getText().equals("setContentView")) {
                    result = statement;
                    break;
                }
            }
        }
        return result;
    }


    public void genOnClick(PsiClass mClass, PsiElementFactory mFactory) {
        PsiStatement statement = findSetContentView(mClass);
        String code = getOnClickCode("int a = 0;", "id");
        insertOnclickCode(mClass, mFactory, statement, code);
    }


    protected void insertOnclickCode(PsiClass mClass, PsiElementFactory mFactory, PsiStatement statement, String code) {
        try {
            statement.addAfter(mFactory.createStatementFromText(
                    code, mClass), statement);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    protected String getOnClickCode(String methodString, String id) {
        return "findViewById("+id+").setOnClickListener(new View.OnClickListener() {\n" +
                " @Override\n" +
                " public void onClick(View v){\n"+
                methodString +
                "}"+
                "});";
    }


    @Nullable
    private PsiJavaFile getOrCreatePackageInfoFile(PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            return null;
        }

        PsiPackageStatement packageStatement = ((PsiJavaFile) file).getPackageStatement();
        if (packageStatement == null) {
            return null;
        }

        PsiJavaCodeReferenceElement packageReference = packageStatement.getPackageReference();
        PsiElement target = packageReference.resolve();
        if (!(target instanceof PsiPackage)) {
            return null;
        }

        PsiJavaFile packageInfoFile = null; // packageInfoFile((PsiPackage) target, file.getContainingDirectory());
        if (packageInfoFile == null) {
          //   packageInfoFile = createPackageInfoFile(file, (PsiPackage) target);
        }

        return packageInfoFile;
    }



/*    public BatchGenNoArgConstructorAction() {
        super( new GenerateNoArgConstructorHandler() );
    }*/



//    public BatchGenNoArgConstructorAction(CodeInsightActionHandler handler) {
//        super(handler);
//    }

    /*
                Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(FileTypeIndex.NAME  ,
                JavaFileType.INSTANCE,
                GlobalSearchScope.projectScope(project));

                System.out.println( psiFile );

                VirtualFile[] files = anActionEvent.getData(DataKeys.VIRTUAL_FILE_ARRAY);
                PsiFile psiFile = anActionEvent.getData(DataKeys.PSI_FILE);
                VirtualFile vf = anActionEvent.getData(DataKeys.VIRTUAL_FILE);
                AnalysisScope analysisScope = new AnalysisScope(project);
                Set<VirtualFile> vfs = analysisScope.getFiles();
    //            PsiDirectory directory = PsiManager.getInstance(project).findDirectory( vf );

    //            JavaPsiFacade.getInstance(project )
                PsiPackage aPackage = JavaPsiFacade.getInstance(project).findPackage("com.tan" );
                PsiClass[] classes = aPackage.getClasses();

                PsiManager psiMan = PsiManager.getInstance( project );


                //                        PsiMethod constructor = elementFactory.createConstructor();
//                        elementFactory.create
                        String clazzQualifiedName = clazz.getQualifiedName();

//                        PsiMethod methodFromText = elementFactory.createMethodFromText("public void " + clazzQualifiedName + "(){} ", null);
//                        PsiMethod var10000 = this.createMethodFromText("public " + GenericsUtil.getVariableTypeByExpressionType(returnType).getCanonicalText(true) + " " + name + "() {}", context);

//                        PsiMethod  = elementFactory.createMethodFromText("public void " + clazzQualifiedName + "(){} ", null);
//                        CodeStyleManager.getInstance(project).reformat( constructor );
//                        clazz.getContainingFile().addBefore(constructor, clazz.getContainingFile().getFirstChild());
//                        clazz.add(constructor);
//                        clazz.getContainingFile().add(constructor);

//                        PsiField fieldFromText = elementFactory.createFieldFromText("private int i;", null);
//                        clazz.add(fieldFromText);
//                        clazz.add(comment);


//                        PsiDocComment comment = elementFactory.createDocCommentFromText("comment ", null);

//                        PsiMethod constructor1 = elementFactory.createConstructor();
//                        PsiMethod constructor  = elementFactory.createConstructor(  clazz.getName() );
//                        clazz.getContainingFile().addBefore(constructor, clazz.getContainingFile().getLastChild());
//                        genOnClick(clazz, elementFactory);
         */

}
