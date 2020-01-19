package com.channel.apkbuild.maker

/**
 * 渠道包生成工具
 *
 * @author liyunlong
 * @date 2018/9/7 10:20
 */
public final class ChannelApkMaker {

    final static ChannelApkMakerV1 getChannelApkMakerV1() {
        return new ChannelApkMakerV1()
    }

    final static ChannelApkMakerV2 getChannelApkMakerV2() {
        return new ChannelApkMakerV2()
    }

}
