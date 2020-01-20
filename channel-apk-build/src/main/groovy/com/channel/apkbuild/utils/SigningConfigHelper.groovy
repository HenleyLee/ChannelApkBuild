package com.channel.apkbuild.utils

import com.android.build.gradle.api.BaseVariant
import com.android.builder.model.SigningConfig

/**
 * 签名信息辅助工具
 *
 * @author liyunlong
 * @date 2018/9/6 11:53
 */
final class SigningConfigHelper {

    public static final String APP_SIGNATURE = "APP_SIGNATURE"
    public static final String V1_ENABLED = "v1SigningEnabled"
    public static final String V2_ENABLED = "v2SigningEnabled"

    static SigningConfig getSigningConfig(BaseVariant variant) {
        return variant.buildType.signingConfig == null ? variant.mergedFlavor.signingConfig : variant.buildType.signingConfig
    }

    static boolean isV2SignatureSchemeEnabled(BaseVariant variant) {
        return isV2SignatureSchemeEnabled(getSigningConfig(variant))
    }

    static boolean isV2SignatureSchemeEnabled(SigningConfig signingConfig) {
        if (signingConfig == null || !signingConfig.isSigningReady()) {
            return false
        }
        // check whether APK Signature Scheme v2 is enabled.
        if (signingConfig.hasProperty("v2SigningEnabled") && signingConfig.v2SigningEnabled) {
            return true
        }
        return false
    }

}
