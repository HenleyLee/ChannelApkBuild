package com.channel.apkbuild

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.PackageAndroidArtifact
import com.channel.apkbuild.extension.ChannelApkBuildExtension
import com.channel.apkbuild.maker.BaseChannelApkMaker
import com.channel.apkbuild.maker.ChannelApkMaker
import com.channel.apkbuild.utils.ApplyConfigHelper
import com.channel.apkbuild.utils.Logger
import com.channel.apkbuild.utils.SigningConfigHelper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the plugin's tasks and generate the channel package.
 *
 * @author liyunlong
 * @date 2018/5/12 15:36
 */
public class ChannelApkBuildPlugin implements Plugin<Project> {

    public static final String PLUGIN_EXTENSION_NAME = "channelApkBuild"

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException("channelApkBuild: 'com.android.application' plugin required！")
        }

        project.extensions.create(PLUGIN_EXTENSION_NAME, ChannelApkBuildExtension)

        def ext = project.extensions.getByName("ext")
        boolean channelEnable = ext.get("channelEnable")
        if (!channelEnable) {
            Logger.error("channel apk build task is disabled!")
            return
        }
        File configJson = ext.get("configJson")
        if (configJson == null) {
            Logger.error("config json is null, you must set the correct config json value!")
            return
        }
        if (!configJson.exists()) {
            Logger.error("config json ${configJson} is not exist, you must set the correct config json value!")
            return
        }
        if (!configJson.isFile()) {
            Logger.error("config json ${configJson} is not a file, you must set the correct config json value!")
            return
        }
        ChannelJsonData channelJsonData = ApplyConfigHelper.readConfig(configJson)
        if (channelJsonData == null) {
            Logger.error("read channel config file ${configJson} failed! The channelJsonData is null.")
            return
        }
        Logger.info("read channel config file ${configJson} successfully! A total of ${channelJsonData.groups.size()} groups and ${channelJsonData.flavors.size()} flavors.\n")

        // 读取渠道配置信息
        File flavorDir = ext.get("flavorDir")
        if (flavorDir == null) {
            Logger.error("flavor directory is null, you must set the correct flavor directory value!")
            return
        }
        if (!flavorDir.exists()) {
            Logger.error("flavor directory ${flavorDir} is not exist, you must set the correct flavor directory value!")
            return
        }
        if (!flavorDir.isDirectory()) {
            Logger.error("flavor directory ${flavorDir} is not a directory, you must set the correct flavor directory value!")
            return
        }
        Map<String, FlavorConfig> flavorConfigMap = ApplyConfigHelper.getFlavorConfigs(channelJsonData, flavorDir)

        AppExtension android = project.extensions.android

        // 设置资源文件夹
        ApplyConfigHelper.applyAndroidSource(android, flavorConfigMap)

        // 设置其它属性修改
        ApplyConfigHelper.applyProductFlavor(android, flavorConfigMap)

        project.afterEvaluate {
            ChannelApkBuildExtension configuration = project.channelApkBuild
            if (configuration == null) {
                Logger.error("The channelApkBuild configuration is null!")
                return
            }

            if (!configuration.channelEnable) {
                Logger.error("channel apk build task is disabled!")
                return
            }

            // 检查配置参数
            configuration.checkParameter()

            // 输出配置信息
            Logger.info("read channelApkBuild configuration ${configuration}\n")

            android.applicationVariants.all { ApkVariant variant ->

                variant.outputs.all { BaseVariantOutput output ->

                    String flavorName = variant.flavorName
                    FlavorConfig flavorConfig = flavorConfigMap.get(flavorName)
                    String debugSuffix = variant.buildType.debuggable ? "-debug" : ""
                    String appNamePrefix = "${flavorConfig.appName}-${variant.versionName}-${variant.versionCode}"
                    outputFileName = "${appNamePrefix}${debugSuffix}.apk"

                    variant.assembleProvider.get().doLast {
                        PackageAndroidArtifact packageAndroidArtifact = variant.packageApplicationProvider.get()
                        File outputDirectory = packageAndroidArtifact.outputDirectory.asFile.get()
                        File apkFile = new File(outputDirectory, apkData.outputFileName)
                        BaseChannelApkMaker channelApkMaker
                        if (SigningConfigHelper.isV2SignatureSchemeEnabled(variant)) {
                            channelApkMaker = ChannelApkMaker.getChannelApkMakerV2()
                        } else {
                            channelApkMaker = ChannelApkMaker.getChannelApkMakerV1()
                        }
                        Logger.info("generate channel apks with Android Signature V${channelApkMaker.version} Scheme.")
                        channelApkMaker.checkSignature(apkFile)
                        channelApkMaker.setWriteChannel(configuration.writeChannel)
                        channelApkMaker.setReleaseDir(configuration.releaseChannelDir)
                        Logger.info("start generating channel apks through ${apkFile}...\n")
                        if (variant.buildType.debuggable) {
                            channelApkMaker.writeChannelMessage(flavorConfig, apkFile)
                        } else {
                            channelApkMaker.generateChannelApks(flavorConfig, apkFile)
                        }
                    }
                }
            }

        }
    }

}