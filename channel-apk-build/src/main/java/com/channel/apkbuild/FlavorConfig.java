package com.channel.apkbuild;

import com.android.builder.model.ClassField;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取后的渠道配置信息
 */
public class FlavorConfig {

    public String appName;
    public Integer versionCode;
    public String versionName;
    public String applicationId;
    public String signingName;

    public List<File> resSrcDirs = new ArrayList<>(2);
    public List<File> javaSrcDirs = new ArrayList<>(2);

    public Map<String, ClassField> resFields = new HashMap<>();
    public Map<String, ClassField> manifestFields = new HashMap<>();
    public Map<String, ClassField> buildConfigFields = new HashMap<>();

    public List<ChannelConfig> channels = new ArrayList<>();

    public String getAppName() {
        return appName;
    }

    public List<ChannelConfig> getChannels() {
        return channels;
    }

    @Override
    public String toString() {
        return "FlavorConfig{" +
                "appName='" + appName + '\'' +
                ", versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", signingName='" + signingName + '\'' +
                ", resSrcDirs=" + resSrcDirs +
                ", javaSrcDirs=" + javaSrcDirs +
                '}';
    }

}
