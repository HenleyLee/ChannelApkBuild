package com.channel.apkbuild;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 渠道项信息
 */
public class FlavorItem {

    public boolean isGroup;
    public String group;
    public String appName;
    public String appId;
    public Integer versionCode;
    public String versionName;
    public String flavorName;
    public String signingName;
    public Set<String> srcDir;
    public List<ChannelConfig> channels;
    public Map<String, String> manifestValues;
    public Map<String, String> buildConfigValues;
    public Map<String, Boolean> buildConfigBoolValues;
    public Map<String, String> resValues;

    public FlavorItem() {

    }

    public FlavorItem(FlavorItem flavorItem) {
        flavorItem.initData();

        this.group = flavorItem.group;

        this.appId = flavorItem.appId;
        this.appName = flavorItem.appName;
        this.versionCode = flavorItem.versionCode;
        this.versionName = flavorItem.versionName;
        this.flavorName = flavorItem.flavorName;
        this.signingName = flavorItem.signingName;
        this.srcDir = new LinkedHashSet<>(flavorItem.srcDir);
        if (flavorItem.channels != null) {
            this.channels = new ArrayList<>(flavorItem.channels);
        }
        this.manifestValues = new LinkedHashMap<>(flavorItem.manifestValues);
        this.buildConfigValues = new LinkedHashMap<>(flavorItem.buildConfigValues);
        this.resValues = new LinkedHashMap<>(flavorItem.resValues);
        this.buildConfigBoolValues = new LinkedHashMap<>(flavorItem.buildConfigBoolValues);
    }

    private static boolean strEmpty(String str) {
        return str == null || str.length() == 0;
    }

    private static boolean intEmpty(Integer value) {
        return value == null || value == 0;
    }

    private synchronized FlavorItem initData() {
        String accountType = appId;
        String authorities = appId + ".sync";

        if (srcDir == null) {
            srcDir = new TreeSet<>();
        }

        if (buildConfigValues == null) {
            buildConfigValues = new LinkedHashMap<>();
        }
        if (buildConfigBoolValues == null) {
            buildConfigBoolValues = new LinkedHashMap<>();
        }

        if (manifestValues == null) {
            manifestValues = new LinkedHashMap<>();
        }
        manifestValues.put("APPLICATION_ID", appId);
        manifestValues.put("SYNC_DATA_AUTHORITIES", authorities);
        if (manifestValues.containsKey("APP_HOST")) {
            buildConfigValues.put("APP_HOST", manifestValues.get("APP_HOST"));
        }
        if (manifestValues.containsKey("APP_SCHEME")) {
            buildConfigValues.put("APP_SCHEME", manifestValues.get("APP_SCHEME"));
        }

        if (resValues == null) {
            resValues = new LinkedHashMap<>();
        }
        resValues.put("app_name", appName);
        resValues.put("provider_authority", authorities);
        resValues.put("account_type", accountType);
        resValues.put("account_safe_desc", appName + "不会同步您的隐私信息");
        if (resValues.containsKey("facebook_app_id")) {
            resValues.put("fb_login_protocol_scheme", "fb" + resValues.get("facebook_app_id"));
        }
        return this;
    }

    /**
     * 用flavor的数据覆盖group数据
     *
     * @return 新生成的覆盖后的数据
     */
    public static FlavorItem mergeGroup(FlavorItem group, FlavorItem flavor) {
        if (group == null || flavor == null || !group.isGroup || flavor.isGroup) {
            throw new IllegalArgumentException("FlavorItem mergeGroup 参数异常！\n->group=" + group + "; \nflavor=" + flavor);
        }

        FlavorItem flavorItem = new FlavorItem(group);

        if (flavor.srcDir != null && !flavor.srcDir.isEmpty()) {
            flavorItem.srcDir.addAll(flavor.srcDir);
        }

        if (!strEmpty(flavor.appId)) {
            flavorItem.appId = flavor.appId;
        }

        if (!strEmpty(flavor.appId)) {
            flavorItem.appId = flavor.appId;
        }

        if (!strEmpty(flavor.appName)) {
            flavorItem.appName = flavor.appName;
        }

        if (!intEmpty(flavor.versionCode)) {
            flavorItem.versionCode = flavor.versionCode;
        }

        if (!strEmpty(flavor.versionName)) {
            flavorItem.versionName = flavor.versionName;
        }

        if (!strEmpty(flavor.signingName)) {
            flavorItem.signingName = flavor.signingName;
        }

        if (flavorItem.channels == null) {
            flavorItem.channels = new ArrayList<>();
        }
        if (flavor.channels != null && !flavor.channels.isEmpty()) {
            flavorItem.channels.addAll(flavor.channels);
        }

        if (flavor.manifestValues != null && !flavor.manifestValues.isEmpty()) {
            flavorItem.manifestValues.putAll(flavor.manifestValues);
        }
        if (flavor.buildConfigValues != null && !flavor.buildConfigValues.isEmpty()) {
            flavorItem.buildConfigValues.putAll(flavor.buildConfigValues);
        }
        if (flavor.buildConfigBoolValues != null && !flavor.buildConfigBoolValues.isEmpty()) {
            flavorItem.buildConfigBoolValues.putAll(flavor.buildConfigBoolValues);
        }
        if (flavor.resValues != null && !flavor.resValues.isEmpty()) {
            flavorItem.resValues.putAll(flavor.resValues);
        }

        flavorItem.initData();

        return flavorItem;
    }

}
