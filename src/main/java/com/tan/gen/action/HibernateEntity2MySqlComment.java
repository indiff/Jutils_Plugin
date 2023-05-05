/* * GenMysqlComment4Entity
 * 1.0.0
 * Date
 * Copyright (c) 2007-2020 qwop版权所有
 *
 */
package com.tan.gen.action;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.tan.gen.util.DBUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Hibernate实体逆向生成ysql注释脚本
 * @autor qwop
 * @date 2020-05-11
 */
public class HibernateEntity2MySqlComment extends AnAction {
    /**
     * 最大的循环次数，控制递归方法的次数限制.
     */
    private static int MAX_COUNT_FLAG = 100 ;
    private int clazzCount = 0;
    private int runIdx = 1;
    /**
     *  int status = JOptionPane.showConfirmDialog(null, "是否需要配置数据库连接文件？");
     *         if (status == JOptionPane.YES_OPTION) {
     *             try {
     *                 DBUtil.config();
     *             } catch (Exception e) {
     *                 e.printStackTrace();
     *             }
     *         }
     * @param anActionEvent
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
//        String packageName = JOptionPane.showInputDialog(null, "请输入包名：例如 net.shopnc.b2b2c.domain");
        // 判断是否文件有变动
        if ( DBUtil.getInstance().requireConfig() ) {
            try {
                DBUtil.config();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ;
        }
       else {
            Project project = anActionEvent.getProject();
            if (project.isInitialized() && project.isOpen()) {
                final Project project2 = CommonDataKeys.PROJECT.getData(anActionEvent.getDataContext());
                PackageChooserDialog chooser = new PackageChooserDialog("请选择实体根目录包", project2);
                chooser.show();
                final List<PsiPackage> packages = chooser.getSelectedPackages();
                List<PsiClass> allClasses = getAllClasses(packages);
                clazzCount = allClasses.size();

                if ( clazzCount >0 ) {
                    PerformInBackgroundOption showProgress = PerformInBackgroundOption.DEAF;
                    Task.Backgroundable backgroundable = new Task.Backgroundable(project, "总共处理需" + clazzCount + "个", true, showProgress) {
                        @Override
                        public void run(@NotNull() final ProgressIndicator indicator) {
                            for (PsiClass clazz : allClasses) {
                                indicator.setText("正在处理第 " + runIdx +"个class:" + clazz.getQualifiedName()   );
                                gen4class(project, clazz);
                                runIdx++;
                                indicator.setFraction( runIdx / clazzCount );
                            }

                            // 数据是否全部处理完成打开文件？

                            // 处理完成是否打开文件？
                            int status = JOptionPane.showConfirmDialog(null, "处理完成，是否打开文件？");
                            if (status == JOptionPane.YES_OPTION) {
                                try {
                                    DBUtil.openDDl();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onCancel() {
                            super.onCancel();
                        }
                    };
                    // The progress manager is only good for foreground threads.
                    if (SwingUtilities.isEventDispatchThread()) {
                        ProgressManager.getInstance().run(backgroundable);
                    } else {
                        // Run the scan task when the thread is in the foreground.
                        SwingUtilities.invokeLater(() -> ProgressManager.getInstance().run(backgroundable));
                    }
                }


            } else {
                JOptionPane.showMessageDialog( null, "项目异常");
            }
        }
    }

        private List<PsiClass> getAllClasses(List<PsiPackage> packages) {
            List<PsiClass> allClasses = new ArrayList<PsiClass>();
            if ( packages != null && packages.size() > 0 ) {
                for (PsiPackage thePackage : packages) {
                    Collections.addAll(allClasses, thePackage.getClasses());
                    PsiPackage[] subPackages = thePackage.getSubPackages();
                    allClasses.addAll(getAllClasses(subPackages));
                }
            }
            return allClasses;
        }

        private List<PsiClass> getAllClasses(PsiPackage[] packages) {
            List<PsiClass> allClasses = new ArrayList<PsiClass>();
            if ( packages != null && packages.length > 0 ) {
                for (PsiPackage aPackage : packages) {
                    Collections.addAll(allClasses, aPackage.getClasses());
                    allClasses.addAll(getAllClasses(aPackage.getSubPackages()));
                }
            }
            return allClasses;
        }


    private void gen4class(Project project, PsiClass clazz) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiField[] allFields = clazz.getAllFields();
        Collection<PsiComment> comments = PsiTreeUtil.findChildrenOfAnyType(clazz, PsiComment.class);
        /**
         * 修改日期
         */
        String tableComment = "";
        PsiDocComment docComment = clazz.getDocComment();
        if ( null != docComment ){
            tableComment = comment( docComment.getText() );
        }
        //  hibernate 获取 Table 注解
        String tableName = getAnnoAttrVal(clazz, "javax.persistence.Table", "name");
//        System.out.println( tableName + "-> " + tableComment );
        StringBuffer buf = new StringBuffer();
        buf.append( "-- " ).append( tableName ).append( " ").append( tableComment ).append("  start.").append( com.tan.gen.util.StringUtil.LN );
        if (StringUtil.isNotEmpty(tableComment)) {
            buf.append( "alter table " ).append( tableName ).append( " comment \'").append( tableComment ).append("\'; ").append( com.tan.gen.util.StringUtil.LN );
        }
        if ( null != allFields && allFields.length > 0 ) {
            for (PsiField field : allFields) {
                resetCountFlag();
                PsiDocComment comment = field.getDocComment();
                String fieldName = field.getName();
                String columnName = getAnnoAttrVal(field, "javax.persistence.Column", "name");
                if (StringUtil.isEmpty(columnName)) {
                    // 如果字段名称为空的话， 则取字段名
                    columnName = fieldName;
                }
                String columnCommment = "";
                if ( comment != null ) {
                    columnCommment = comment( comment.getText() );
                }
//                QueryRunner run = new QueryRunner(dataSource);
                // 如果字段的注释不为空的话更新
                if ( StringUtil.isNotEmpty(columnCommment) ) {
//                    System.out.println( columnName + "-> " + columnCommment );
                    try {
                        String tableCommentDDL = DBUtil.getInstance().getTableCommentDDL(DBUtil.getInstance().getSchema(), tableName, columnName, columnCommment);
                        buf.append( tableCommentDDL ).append( com.tan.gen.util.StringUtil.LN );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        buf.append( "-- " ).append( tableName ).append( " ").append( tableComment ).append("  end.").append( com.tan.gen.util.StringUtil.LN );
        DBUtil.getInstance().writeDDl( buf.toString() );
    }

    private String getAnnoAttrVal(PsiJvmModifiersOwner clazz, String fqn,  String attributeName) {
        PsiAnnotation annotation = clazz.getAnnotation(fqn);
        if ( null != annotation) {
            PsiNameValuePair name = (PsiNameValuePair) annotation.findAttribute(attributeName);
            if ( name != null ) {
                String text = ((PsiNameValuePair) name).getLiteralValue();
                return text;
            }
        }
        return "";
    }

    /**
     *
     * @param text
     * @return
     */
    private static String comment(String text) {
        boolean notEmpty = StringUtil.isNotEmpty(text);
        if ( notEmpty ) {
            String trim = text.trim();
            return trim
                    .replaceAll( "\\*", "" )
                    .replaceAll( "\\@param", "" )
                    .replaceAll( "\\@copyright.*", "" )
                    .replaceAll( "\\@license.*", "" )
                    .replaceAll( "\\@link.*", "" )
                    .replaceAll( "\\@return", "" )
                    .replaceAll( "\\@author.+", "" )
                    .replaceAll( "Created.+", "" )
                    .replaceAll( "copyright.+", "" )
                    .replaceAll( "Copyright.+", "" )
                    .replaceAll( "\\/", "" )
                    .replaceAll( "\r\n", "" )
                    .replaceAll( "\n", "" )
                    .trim();
        } else {
            return "";
        }
    }

    public static void main(String[] args) {
        System.out.println(comment("/** 这是注释 */"));
        System.out.println(comment("// 我ye是注释"));
        System.out.println(comment("// 活动状态\n" ));
        System.out.println(comment("/**\n" +
                " * @copyright  Copyright (c) 2007-2017 ShopNC Inc. All rights reserved.\n" +
                " * @license    http://www.shopnc.net\n" +
                " * @link       http://www.shopnc.net\n" +
                " *\n" +
                " * 会员实体\n" +
                " *\n" +
                " * @author zxy\n" +
                " * Created 2017/4/13 10:38\n" +
                " */" )) ;
        System.out.println(comment("/**\n" +
                " * copyright  Copyright: Bizpower多用户商城系统\n" +
                " * Copyright: www.bizpower.com\n" +
                " * Copyright: 天津网城商动科技有限责任公司\n" +
                " *\n" +
                " * 平台活动数据表\n" +
                " *\n" +
                " * @author cj\n" +
                " * Created 2017-5-18 上午 8:44\n" +
                " */"));
        System.out.println(comment(" /**\n" +
                "     * 修改日期\n" +
                "     */"));
    }

    private void resetCountFlag() {
        countFlag = 0;
    }

    private int countFlag;


    private PsiComment getComment(PsiElement element) {
        if (element == null) {
            return null;
        }
        PsiElement prevSibling = element.getPrevSibling();
        if ( null != prevSibling) {
            System.err.println(prevSibling.getClass());
        }
        if ( null != prevSibling && prevSibling instanceof  PsiComment ) {
            return ( PsiComment ) prevSibling;
        }

        countFlag += 1;
        if ( countFlag >= MAX_COUNT_FLAG ) {
            System.err.println( " 超过 " + MAX_COUNT_FLAG + " 次！");
            return null;
        }
        return getComment( prevSibling );
    }
}
