/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tan.gen;


import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author qwop
 * @date
 */
public class JutilsIcons {
    public static final Icon GEN_NO_ARG_CONS = IconLoader.getIcon("/icons/gen-no-arg-cons.png");
    public static final Icon EDITOR_OPEN = load("/icons/editplus.ico");

    @NotNull
    public static Icon load(@NotNull String path) {
        return IconManager.getInstance().getIcon(path, AllIcons.Icons.class);
    }
}
