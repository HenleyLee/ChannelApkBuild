package com.channel.apkbuild.maker

import com.android.annotations.Nullable
import com.android.apksig.ApkVerifier
import com.android.apksig.internal.util.AndroidSdkVersion
import com.channel.apkbuild.ChannelConfig
import com.channel.apkbuild.FileUtil
import com.channel.apkbuild.FlavorConfig
import com.channel.apkbuild.utils.Consts
import com.channel.apkbuild.utils.Logger
import com.google.gson.Gson
import org.gradle.api.GradleException

/**
 * Generate Channel Apks
 *
 * @author liyunlong
 * @date 2018/9/7 9:27
 */
abstract class BaseChannelApkMaker {

    private static final String APK_SUFFIX = ".apk"
    private static final String NAME_SEPARATOR = "-"
    private static final Integer MIN_VERSION = AndroidSdkVersion.GINGERBREAD
    private static final Integer MAX_VERSION = Integer.MAX_VALUE

    final void checkSignature(File apkFile) {
        ApkVerifier apkVerifier = new ApkVerifier.Builder(apkFile)
                .setMinCheckedPlatformVersion(MIN_VERSION)
                .setMaxCheckedPlatformVersion(MAX_VERSION)
                .build()
        ApkVerifier.Result result = apkVerifier.verify()
        if (result == null) {
            throw new GradleException(Consts.PREFIX_OF_LOGGER + "${apkFile} signature verification result is empty!")
        }
        verify(result)
    }

    /**
     * 为包写入渠道信息
     *
     * @param flavorConfig 渠道配置信息
     * @param baseApk 基础APK文件
     */
    final void writeChannelMessage(FlavorConfig flavorConfig, File baseApk) {
        if (baseApk == null || !baseApk.exists() || !baseApk.isFile()) {
            Logger.error("write channel message but base apk not found!")
            return
        }

        if (flavorConfig.channels == null || flavorConfig.channels.size() == 0) {
            Logger.error("write channel message failed, no channels!")
            return
        }

        Gson gson = new Gson()
        ChannelConfig channelConfig = flavorConfig.channels.get(0)

        String channelInfoJson = gson.toJson(channelConfig)
        writeChannel(baseApk, channelInfoJson)

        if (channelInfoJson != readChannel(baseApk)) {
            Logger.info("channel message validation failed for APK file! -> ${baseApk}")
        } else {
            Logger.info("write channel message ${channelInfoJson} for apk -> ${baseApk}")
        }
    }

    /**
     * 生成渠道包
     *
     * @param flavorConfig 渠道配置信息
     * @param baseApk
     * @param releaseDir
     */
    final void generateChannelApks(FlavorConfig flavorConfig, File baseApk, @Nullable File releaseDir) {
        if (baseApk == null || !baseApk.exists() || !baseApk.isFile()) {
            Logger.error("generate channel apks but base apk not found!")
            return
        }

        if (flavorConfig.channels == null || flavorConfig.channels.size() == 0) {
            Logger.error("generate channel apks failed, no channels found!")
            return
        }

        if (releaseDir == null) {
            releaseDir = new File(baseApk.getParentFile().getParentFile().getParentFile().getParentFile(), "/release")
        }

        File channelDir = new File(releaseDir, flavorConfig.appName)
        if (!channelDir.exists()) {
            channelDir.mkdirs()
        }

        int lastDotIndex = baseApk.getName().lastIndexOf(APK_SUFFIX)
        String baseName = baseApk.getName().substring(0, lastDotIndex)
        Gson gson = new Gson()

        for (ChannelConfig channelConfig : flavorConfig.channels) {
            int source = channelConfig.source
            String channel = channelConfig.channel
            if (channel != null && !channel.contains(String.valueOf(source))) {
                channel = source + NAME_SEPARATOR + channel
            }
            String channelApkName = baseName + NAME_SEPARATOR + channel + APK_SUFFIX
            File channelApkFile = new File(channelDir, channelApkName)
            FileUtil.copyFile(baseApk, channelApkFile)

            String channelInfoJson = gson.toJson(channelConfig)
            writeChannel(channelApkFile, channelInfoJson)

            if (channelInfoJson != readChannel(channelApkFile)) {
                Logger.info("channel message validation failed for APK file! -> ${channelApkFile}")
            } else {
                Logger.info("write channel message ${channelInfoJson} for apk -> ${channelApkFile}")
            }
        }
        Logger.info("generate ${flavorConfig.appName} ${flavorConfig.channels.size()} channel apks success in dir ${channelDir}!")
    }

    /**
     * 获取 Signature Scheme Version
     */
    abstract int getVersion()

    /**
     * 验证APK签名
     *
     * @param result 验证APK签名的结果
     */
    abstract void verify(ApkVerifier.Result result)

    /**
     * 读取APK渠道信息
     *
     * @param apkFile APK文件
     */
    abstract String readChannel(final File apkFile)

    /**
     * 写入APK渠道信息
     *
     * @param apkFile APK文件
     * @param channel 渠道信息
     */
    abstract void writeChannel(final File apkFile, final String channel)

}
