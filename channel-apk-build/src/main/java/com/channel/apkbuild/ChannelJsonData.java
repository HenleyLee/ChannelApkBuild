package com.channel.apkbuild;

import java.util.List;

/**
 * 渠道JSON数据
 */
public class ChannelJsonData {

    public List<FlavorItem> groups;
    public List<FlavorItem> flavors;

    public FlavorItem getGroup(FlavorItem flavor) {
        if (flavor == null || flavor.isGroup) {
            throw new IllegalArgumentException("getGroup with illegal FlavorItem");
        }
        if (groups == null) {
            return null;
        }
        for (FlavorItem group : groups) {
            if (group.group.equals(flavor.group)) {
                return group;
            }
        }
        return null;
    }

}
