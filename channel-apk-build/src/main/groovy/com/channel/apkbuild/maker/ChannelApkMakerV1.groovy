package com.channel.apkbuild.maker

import com.android.apksig.ApkVerifier
import com.android.apksig.internal.apk.ApkSigningBlockUtils
import com.channel.apkbuild.PackerNg
import com.channel.apkbuild.utils.Consts
import org.gradle.api.GradleException

/**
 * Generate Channel Apks(Android Signature V1 Scheme)
 */
final class ChannelApkMakerV1 extends BaseChannelApkMaker {

    @Override
    int getVersion() {
        return ApkSigningBlockUtils.VERSION_JAR_SIGNATURE_SCHEME
    }

    @Override
    void verify(ApkVerifier.Result result) {
        if (!result.verifiedUsingV1Scheme) {
            throw new GradleException(Consts.PREFIX_OF_LOGGER + "${apkFile} has no v1 signature in APK JAR File!")
        }
    }

    @Override
    String readChannel(File apkFile) {
        return PackerNg.Helper.readMarket(apkFile)
    }

    @Override
    void writeChannel(File apkFile, String channel) {
        PackerNg.Helper.writeMarket(apkFile, channel)
    }

}
