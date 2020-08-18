package com.channel.apkbuild.utils

import com.android.SdkConstants
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceDirectorySet
import com.android.build.gradle.api.AndroidSourceSet
import com.android.builder.internal.ClassFieldImpl
import com.android.builder.model.ClassField
import com.android.builder.model.ProductFlavor
import com.android.builder.model.SigningConfig
import com.channel.apkbuild.ChannelJsonData
import com.channel.apkbuild.FlavorConfig
import com.channel.apkbuild.FlavorItem
import com.google.gson.Gson

/**
 * 应用配置信息辅助类
 *
 * @author liyunlong
 * @date 2018/4/18 11:26
 */
final class ApplyConfigHelper {

    /**
     * 读取渠道配置JSON信息
     *
     * @param jsonFile 渠道配置JSON文件
     * @return 渠道JSON数据
     */
    static ChannelJsonData readConfig(File jsonFile) {
        Gson gson = new Gson()
        return gson.fromJson(new InputStreamReader(new FileInputStream(jsonFile), "UTF-8"), ChannelJsonData.class)
    }

    /**
     * 读取渠道配置信息
     *
     * @param channelJsonData 渠道JSON数据
     * @param flavorDir flavor资源目录
     * @return 渠道配置信息Map
     */
    static Map<String, FlavorConfig> getFlavorConfigs(ChannelJsonData channelJsonData, File flavorDir) {
        HashMap<String, FlavorConfig> flavorConfigMap = new HashMap<>()
        for (FlavorItem flavorItem : channelJsonData.flavors) {
            FlavorItem mergedFlavorItem = getMergedFlavorItem(channelJsonData, flavorItem)
            FlavorConfig flavorConfig = getFlaverConfig(mergedFlavorItem, flavorDir)
            flavorConfigMap.put(flavorItem.flavorName, flavorConfig)
        }
        return flavorConfigMap
    }

    /**
     * 合并Group和FlavorItem
     *
     * @param channelJsonData 渠道JSON数据
     * @param flavorItem 渠道项信息
     * @return 合并Group和FlavorItem后的渠道项信息
     */
    static FlavorItem getMergedFlavorItem(ChannelJsonData channelJsonData, FlavorItem flavorItem) {
        FlavorItem group = channelJsonData.getGroup(flavorItem)
        if (group == null) {
            group = channelJsonData.groups == null && channelJsonData.groups.size() > 0 ? channelJsonData.groups.get(0) : null
        }
        FlavorItem mergedFlavorItem = flavorItem
        if (group != null) {
            mergedFlavorItem = FlavorItem.mergeGroup(group, flavorItem)
        }
        return mergedFlavorItem
    }

    /**
     * 将FlavorItem转换为FlaverConfig
     *
     * @param flavorItem 渠道项信息
     * @param flavorDir flavor资源目录
     * @return 渠道配置信息
     */
    static FlavorConfig getFlaverConfig(FlavorItem flavorItem, File flavorDir) {
        FlavorConfig flavorConfig = new FlavorConfig()

        // basic config info
        flavorConfig.appName = flavorItem.appName
        flavorConfig.applicationId = flavorItem.appId
        flavorConfig.versionCode = flavorItem.versionCode
        flavorConfig.versionName = flavorItem.versionName
        flavorConfig.signingName = flavorItem.signingName

        // String buildConfig
        for (Map.Entry<String, String> entry : flavorItem.buildConfigValues.entrySet()) {
            String key = entry.getKey()
            String value = entry.getValue()
            if (value == null || value.length() == 0) {
                Logger.info("config String buildConfig has null value with key: ${key}")
                continue
            }
            ClassField classField = new ClassFieldImpl("String", key, "\"" + value + "\"")
            flavorConfig.buildConfigFields.put(key, classField)
        }

        // boolean buildConfig
        for (Map.Entry<String, Boolean> entry : flavorItem.buildConfigBoolValues.entrySet()) {
            String key = entry.getKey()
            Boolean value = entry.getValue()
            if (value == null || value.toString().length() == 0) {
                Logger.info("config boolean buildConfig has null value with key: ${key}")
                continue
            }
            ClassField classField = new ClassFieldImpl("boolean", key, String.valueOf(value))
            flavorConfig.buildConfigFields.put(key, classField)
        }

        // resFields
        for (Map.Entry<String, String> entry : flavorItem.resValues.entrySet()) {
            String key = entry.getKey()
            String value = entry.getValue()
            if (value == null || value.length() == 0) {
                Logger.info("config resValue has null value with key: ${key}")
                continue
            }
            ClassField classField = new ClassFieldImpl("string", key, value)
            flavorConfig.resFields.put(key, classField)
        }

        // manifestFields
        if (flavorItem.manifestValues != null) {
            flavorConfig.manifestFields.putAll(flavorItem.manifestValues)
        }

        // channels
        if (flavorItem.channels != null) {
            flavorConfig.channels.addAll(flavorItem.channels)
        }

        // android source dir
        if (flavorDir != null && flavorDir.exists() && flavorDir.isDirectory()) {
            if (flavorItem.srcDir != null && !flavorItem.srcDir.isEmpty()) {
                for (String dirName : flavorItem.srcDir) {
                    File sourceDir = new File(flavorDir, dirName)
                    if (sourceDir.exists() && sourceDir.isDirectory()) {
                        flavorConfig.flavorDirs.add(sourceDir)
                    }
                }
            } else {
                Logger.info("${flavorItem.flavorName} flavorItem srcDir is empty: ${flavorItem.srcDir}.")
            }
        } else {
            Logger.info("skip apply res and java！${flavorDir} does not exist or is not a directory.")
        }
        return flavorConfig
    }

    /**
     * 设置资源文件
     *
     * @param android
     * @param flavorConfigMap 渠道配置信息
     */
    static void applyAndroidSource(BaseExtension android, Map<String, FlavorConfig> flavorConfigMap) {
        int size = 0
        for (AndroidSourceSet sourceSet : android.sourceSets) {
            AndroidSourceDirectorySet srcSet = sourceSet.res
            String flavorName = srcSet.name.replace(" resources", "").trim()
            FlavorConfig flavorConfig = flavorConfigMap.get(flavorName)
            if (flavorConfig == null || flavorConfig.flavorDirs == null || flavorConfig.flavorDirs.isEmpty()) {
                continue
            }
            Logger.info("start deploy flavor ${flavorName} android sourceSet dirs...")
            for (File dir : flavorConfig.flavorDirs) {
                sourceSet.res.srcDir(new File("${dir.path}/${SdkConstants.FD_RES}"))
                sourceSet.java.srcDir(new File("${dir.path}/${SdkConstants.FD_JAVA}"))
                sourceSet.aidl.srcDir(new File("${dir.path}/${SdkConstants.FD_AIDL}"))
                sourceSet.assets.srcDir(new File("${dir.path}/${SdkConstants.FD_ASSETS}"))
                sourceSet.manifest.srcFile("${dir.path}/${SdkConstants.FN_ANDROID_MANIFEST_XML}")
                sourceSet.resources.srcDir(new File("${dir.path}/${SdkConstants.FD_JAVA_RES}"))
                sourceSet.renderscript.srcDir(new File("${dir.path}/${SdkConstants.FD_RENDERSCRIPT}"))
                sourceSet.jni.srcDir(new File("${dir.path}/${SdkConstants.FD_JNI}"))
                sourceSet.jniLibs.srcDir(new File("${dir.path}/jniLibs"))
                sourceSet.shaders.srcDir(new File("${dir.path}/shaders"))
            }
            Logger.info("deploy flavor ${flavorName} android sourceSet->${flavorConfig.flavorDirs}")
            size++
            Logger.newLine()
        }
        Logger.info("deploy android source sourceSets successfully! A total of ${size} android sourceSets!\n")
    }

    /**
     * 设置ProductFlavor
     *
     * @param android
     * @param flavorConfigMap 渠道配置信息
     */
    static void applyProductFlavor(BaseExtension android, Map<String, FlavorConfig> flavorConfigMap) {
        // 读取签名配置信息
        HashMap<String, SigningConfig> signingMap = getSigningConfigs(android)

        for (ProductFlavor productFlavor : android.productFlavors) {
            FlavorConfig flavorConfig = flavorConfigMap.get(productFlavor.name)
            if (flavorConfig == null) {
                Logger.error("product flavor ${productFlavor.name} config not found!")
                continue
            }
            Logger.info("start deploy product flavor ${productFlavor.name} config...")
            Logger.info("product flaver ${productFlavor.name} flavor config is ${flavorConfig}")
            productFlavor.versionCode = flavorConfig.versionCode
            productFlavor.versionName = flavorConfig.versionName
            productFlavor.applicationId = flavorConfig.applicationId
            productFlavor.resValues.putAll(flavorConfig.resFields)
            productFlavor.manifestPlaceholders.putAll(flavorConfig.manifestFields)
            productFlavor.buildConfigFields.putAll(flavorConfig.buildConfigFields)

            // 设置签名
            if (flavorConfig.signingName != null && !signingMap.isEmpty()) {
                SigningConfig signingConfig = signingMap.get(flavorConfig.signingName)
                if (signingConfig != null) {
                    productFlavor.signingConfig = signingConfig
                    Logger.info("product flavor ${productFlavor.name} signing config is ${signingConfig}")
                }
            }
            Logger.newLine()
        }
        Logger.info("deploy product flavor config successfully! A total of ${android.productFlavors.size()} product flavors!\n")
    }

    /**
     * 读取签名配置信息
     *
     * @param project
     */
    private static HashMap<String, SigningConfig> getSigningConfigs(BaseExtension android) {
        HashMap<String, SigningConfig> signingMap = new HashMap<>()
        for (SigningConfig signingConfig : android.signingConfigs) {
            signingMap.put(signingConfig.name, signingConfig)
        }
        Logger.info("deploy signing config successfully! A total of ${android.signingConfigs.size()} signing configs!\n")
        return signingMap
    }

}