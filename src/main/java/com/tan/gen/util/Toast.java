package com.tan.gen.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

import javax.swing.*;

/**
 * 提示信息组件
 * @autor qwop
 * @date 2020-05-11
 */
public class Toast {

    /**
     * Display simple notification of given type
     * 按照指定类型显示通知消息
     * @param project     project
     * @param messageType messageType
     * @param text        text
     */
    public static void make(Project project, JComponent jComponent, MessageType messageType, String text) {
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, messageType, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(jComponent), Balloon.Position.above);
    }

    /**
     * Display simple notification of given type
     * 按照指定类型显示通知消息
     * @param project     project
     * @param messageType messageType
     * @param text        text
     */
    public static void make(Project project, MessageType messageType, String text) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, messageType, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }
}
