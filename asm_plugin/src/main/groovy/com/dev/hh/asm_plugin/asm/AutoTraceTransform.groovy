package com.dev.hh.asm_plugin.asm

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.dev.hh.asm_plugin.utils.AutoMatchUtil
import com.dev.hh.asm_plugin.utils.AutoTextUtil
import org.apache.commons.compress.utils.IOUtils
import org.gradle.api.Project
import groovy.io.FileType

/**
 * Package: com.dev.hh.gradle_plugin
 * User: hehao3
 * Email: hehao3@jd.com
 * Date: 2021/4/29
 * Time: 下午10:22
 * Description:
 */
class AutoTraceTransform extends Transform {
    private static final String VERSION = "v1.0.0"

    Project project
    boolean application

    AutoTraceTransform(Project project, boolean application) {
        this.project = project
        this.application = application
    }

    /**
     * 设置我们自定义的Transform对应的Task名称
     * @return
     */
    @Override
    String getName() {
        return "AutoTrack"
    }

    /**
     * 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型这样确保其他类型的文件不会传入
     *
     * 如果是要处理所有class和jar的字节码，ContentType我们一般使用TransformManager.CONTENT_CLASS。
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指定Transform的作用范围
     *
     * 如果是要处理所有class字节码，Scope我们一般使用TransformManager.SCOPE_FULL_PROJECT。
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
        println('transform')

        try {
            onTransform(
                    transformInvocation.getContext(),
                    transformInvocation.getInputs(),
                    transformInvocation.getReferencedInputs(),
                    transformInvocation.getOutputProvider(),
                    transformInvocation.isIncremental())
        } catch (Throwable e) {
            println('Build failed with an exception: ${e.cause?.message}')
            e.fillInStackTrace()
            throw e
        } finally {
        }
    }

    /**
     *
     * @param gradleContext
     * @param inputs
     * @param referencedInputs
     * @param outputProvider    OutputProvider管理输出路径
     * @param isIncremental
     * @throws IOException
     * @throws TransformException
     * @throws InterruptedException
     */
    void onTransform(
            Context gradleContext,
            Collection<TransformInput> inputs,
            Collection<TransformInput> referencedInputs,
            TransformOutputProvider outputProvider,
            boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        ////如果非增量，则清空旧的输出内容
        if (!incremental) {
            outputProvider.deleteAll()
        }

        printCopyRight()
        def startTime = printStartTips()
        printlnJarAndDir(inputs)


        //此处会遍历所有文件
        /**遍历输入文件*/
        inputs.each { TransformInput input ->
            /**
             * 遍历jar
             */
            input.jarInputs.each { JarInput jarInput ->

            }
            /**
             * 遍历目录
             */
            input.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                println("||-->开始遍历特定目录  ${dest.absolutePath}")
                File dir = directoryInput.file
                if (dir) {
                    HashMap<String, File> modifyMap = new HashMap<>()
                    //groovy把文件看做是一种资源，提供的方法都是通过ResourceGroovyMethods提供的。
                    //traverse遍历以某一扩展名结尾的文件
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                        File classFile ->
                            File modified = modifyClassFile(dir, classFile, gradleContext.getTemporaryDir())
                            if (modified != null) {
                                //key为相对路径
                                modifyMap.put(classFile.absolutePath.replace(dir.absolutePath, ""), modified)
                            }
                    }
                    FileUtils.copyDirectory(directoryInput.file, dest)
                    modifyMap.entrySet().each {
                        Map.Entry<String, File> en ->
                            File target = new File(dest.absolutePath + en.getKey())
//                            Logger.info(target.getAbsolutePath())
                            if (target.exists()) {
                                target.delete()
                            }
                            FileUtils.copyFile(en.getValue(), target)
                            en.getValue().delete()
                    }
                }
            }
        }

        printEndTips(startTime)
    }

    /**
     * 目录文件中修改对应字节码
     */
    private static File modifyClassFile(File dir, File classFile, File tempDir) {
        File modified = null
        FileOutputStream outputStream = null
        try {
            String className = AutoTextUtil.path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
            println("File:className:" + className)
            //判断class是否是要修改的class
            if (AutoMatchUtil.isShouldModifyClass(className)) {
                //获取class字节码数组
                byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
                //修改字节码
                byte[] modifiedClassBytes = AutoModify.modifyClasses(sourceClassBytes)
                //生成新的class文件
                if (modifiedClassBytes) {
                    modified = new File(tempDir, className.replace('.', '') + '.class')
                    if (modified.exists()) {
                        modified.delete()
                    }
                    modified.createNewFile()
                    outputStream = new FileOutputStream(modified)
                    outputStream.write(modifiedClassBytes)
                }
            } else {
                return classFile
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close()
                }
            } catch (Exception e) {
            }
        }
        return modified

    }





    /**
     * 打印提示信息
     */
    private void printCopyRight() {
        println()
        println '#######################################################################'
        println '##########                                                    '
        println '##########         欢迎使用  (' + VERSION + ')无埋点编译插件'
        println '##########           使用过程中碰到任何问题请联系数据中心          '
        println '##########                                                    '
        println '#######################################################################'
        println '##########                                                    '
        println '##########                      插件配置参数                    '
        println '##########                                                    '
        println '##########                                                    '
        println '##########                                                    '
        println '#######################################################################'
        println()
    }

    /**
     * 打印提示信息
     */
    private Long printStartTips() {
        //开始计算消耗的时间
        println("||=======================================================================================================")
        println("||                                                 开始计时                                               ")
        println("||=======================================================================================================")
        def startTime = System.currentTimeMillis()
        return startTime
    }

    /**
     * 打印提示信息
     */
    private void printEndTips(Long startTime) {
        //计算耗时
        def cost = (System.currentTimeMillis() - startTime) / 1000
        println("||=======================================================================================================")
        println("||                                       计时结束:费时${cost}秒                                           ")
        println("||=======================================================================================================")
    }

    /**
     * 包括两种数据:jar包和class目录，打印出来用于调试
     */
    private void printlnJarAndDir(Collection<TransformInput> inputs) {
        def classPaths = []
        String buildTypes
        String productFlavors
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                classPaths.add(directoryInput.file.absolutePath)
                buildTypes = directoryInput.file.name
                productFlavors = directoryInput.file.parentFile.name
                println("||项目class目录：${directoryInput.file.absolutePath}")
            }
            input.jarInputs.each { JarInput jarInput ->
                classPaths.add(jarInput.file.absolutePath)
                println("||项目jar包：${jarInput.file.absolutePath}")
            }
        }
    }
}
