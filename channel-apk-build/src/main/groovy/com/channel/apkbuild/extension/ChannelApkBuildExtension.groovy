package com.channel.apkbuild.extension

import com.channel.apkbuild.utils.Consts
import org.gradle.api.GradleException

/**
 * The configuration properties.
 *
 * @author liyunlong
 * @date 2018/4/10 15:28
 */
public class ChannelApkBuildExtension {

    /**
     * Whether the channel bundle task is enabled
     */
    boolean channelEnable
    /**
     * Whether to write channel information
     */
    boolean writeChannel
    /**
     * Flavor resource file directory
     */
    File flavorDir
    /**
     * Channel configuration JSON file directory
     */
    File configJson
    /**
     * Release channel package store directory
     */
    File releaseChannelDir

    ChannelApkBuildExtension() {
    }

    void checkParameter() {
        if (configJson == null) {
            throw new GradleException(Consts.PREFIX_OF_LOGGER + "config json is null, you must set the correct config json value!")
        } else if (!configJson.exists()) {
            throw new GradleException(Consts.PREFIX_OF_LOGGER + "config json ${configJson} is not exist, you must set the correct config json value!")
        } else if (!configJson.isFile()) {
            throw new GradleException(Consts.PREFIX_OF_LOGGER + "config json ${configJson} is not a file, you must set the correct config json value!")
        }

        if (flavorDir == null) {
            throw new GradleException(Consts.PREFIX_OF_LOGGER + "flavor directory is null, you must set the correct flavor directory value!")
        } else if (!flavorDir.exists()) {
            throw new GradleException(Consts.PREFIX_OF_LOGGER + "flavor directory ${flavorDir} is not exist, you must set the correct flavor directory value!")
        } else if (!flavorDir.isDirectory()) {
            throw new GradleException(Consts.PREFIX_OF_LOGGER + "flavor directory ${flavorDir} is not a directory, you must set the correct flavor directory value!")
        }
    }


    @Override
    String toString() {
        return "ChannelApkBuildExtension{" +
                "channelEnable=" + channelEnable +
                ", writeChannel=" + writeChannel +
                ", flavorDir=" + flavorDir +
                ", configJson=" + configJson +
                ", releaseChannelDir=" + releaseChannelDir +
                '}'
    }

}
