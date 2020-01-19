package com.channel.apkbuild.maker

import com.android.apksig.ApkVerifier
import com.android.apksig.internal.apk.ApkSigningBlockUtils
import com.channel.apkbuild.utils.Consts
import com.meituan.android.walle.ChannelInfo
import com.meituan.android.walle.ChannelReader
import com.meituan.android.walle.ChannelWriter
import org.gradle.api.GradleException

/**
 * Generate Channel Apks(Android Signature V2 Scheme)
 */
final class ChannelApkMakerV2 extends BaseChannelApkMaker {

    @Override
    int getVersion() {
        return ApkSigningBlockUtils.VERSION_APK_SIGNATURE_SCHEME_V2
    }

    @Override
    void verify(ApkVerifier.Result result) {
        if (!result.verifiedUsingV2Scheme) {
            throw new GradleException(Consts.PREFIX_OF_LOGGER + "${apkFile} has no v2 signature in APK Signing Block!")
        }
    }

    @Override
    String readChannel(File apkFile) {
        final ChannelInfo channelInfo = ChannelReader.get(apkFile)
        if (channelInfo == null) {
            return null
        }
        return channelInfo.getChannel()
    }

    @Override
    void writeChannel(File apkFile, String channel) {
        ChannelWriter.put(apkFile, channel, null)
    }

}