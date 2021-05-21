package com.dev.hh.asm_plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.dev.hh.asm_plugin.asm.AutoTraceTransform
import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * Package: com.dev.hh.gradle_plugin
 * User: hehao3
 * Email: hehao3@jd.com
 * Date: 2021/4/26
 * Time: 下午6:51
 * Description: 插件入口
 */
class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        println('project.plugins.hasPlugin(AppPlugin.class):' + project.plugins.hasPlugin(AppPlugin.class))
        if (project.plugins.hasPlugin(AppPlugin.class)) {
            AppExtension extension = project.extensions.getByType(AppExtension)
            extension.registerTransform(
                    new AutoTraceTransform(project, true))
        }


        println('project.plugins.hasPlugin(LibraryPlugin.class):' + project.plugins.hasPlugin(LibraryPlugin.class))
        //AppExtension就是app中build.gradle中android块
        if (project.plugins.hasPlugin(LibraryPlugin.class)) {
            LibraryExtension extension1 = project.extensions.getByType(LibraryExtension)
            extension1.registerTransform(
                    new AutoTraceTransform(project, false))
        }

        project.afterEvaluate {
//            Logger.setDebug(project.xiaoqingwa.isDebug)
            // 用户配置解析
        }
    }
}