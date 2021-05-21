package com.dev.hh.asm_plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

import java.util.stream.Stream

/**
 * Package: com.dev.hh.gradle_plugin
 * User: hehao3
 * Email: hehao3@jd.com
 * Date: 2021/4/29
 * Time: 下午10:22
 * Description:
 */
class MJavassistTransform  extends Transform {
    Project project
    boolean application

    MJavassistTransform(Project project, boolean application) {
        this.project = project
        this.application = application
    }

    /**
     * 设置我们自定义的Transform对应的Task名称
     * @return
     */
    @Override
    String getName() {
        return "JavassistTrans"
    }

    /**
     * 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型这样确保其他类型的文件不会传入
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指定Transform的作用范围
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否增量编译
     * @return
     */
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        try {

            println('transform')
            onTransform(
                    invocation.getContext(),
                    invocation.getInputs(),
                    invocation.getReferencedInputs(),
                    invocation.getOutputProvider(),
                    invocation.isIncremental())
        } catch (Throwable e) {
            println('Build failed with an exception: ${e.cause?.message}')
            e.fillInStackTrace()
            throw e
        } finally {
        }
    }

    void onTransform(
            Context gradleContext,
            Collection<TransformInput> inputs,
            Collection<TransformInput> referencedInputs,
            TransformOutputProvider outputProvider,
            boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

    }
}
