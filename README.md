# ChannelApkBuild —— Android V1 and V2 Signature Channel Package Plugin

**`ChannelApkBuild`** 是一种快速多渠道打包工具，同时支持基于 `V1` 签名和 `V2` 签名进行多渠道打包。插件本身会自动检测 `Apk` 使用的签名类别，并选择合适的多渠道打包方式，对使用者来说完全透明。欢迎使用！

目前 `Gradle Plugin 2.2` 以上默认开启 `V2` 签名，所以如果想关闭 `V2` 签名，可将下面的 `v2SigningEnabled` 设置为 `false`。
```gradle
signingConfigs {
    release {
        ...
        v1SigningEnabled true
        v2SigningEnabled false
    }

    debug {
        ...
        v1SigningEnabled true
        v2SigningEnabled false
    }
}
```

## 接入流程 ##
#### 添加对 ChannelApkBuild Plugin 的依赖 ####
在根工程的 `build.gradle` 中，添加对打包插件的依赖：
```gradle
dependencies {
    classpath 'com.android.tools.build:gradle:4.0.0'
    classpath 'com.channel.apkbuild:channel-apk-build:4.0.0'
}
```

#### 引用 ChannelApkBuild Plugin ####
在主 `App` 工程的 `build.gradle` 中，添加对插件的引用：
```gradle
apply plugin: 'com.channel.apkbuild'
```

#### 添加对 Walle 类库的依赖 ####
在主 `App` 工程的 `build.gradle` 中，添加读取渠道信息的类库依赖：
```gradle
dependencies {
    api 'com.meituan.android.walle:library:1.1.7'
}
```

## 配置插件 ##
```gradle
def date = new Date().format("yyyy-MM-dd")
def configDir = "../config/"
def extraPath = file("${project.rootDir}/apk")

ext {
    // 是否启用渠道包任务
    channelEnable = true
    // 渠道配置json文件位置
    configJson = file("${configDir}/channel.json")
    // flavor资源文件位置
    flavorDir = file("${configDir}/flavor/")
}

def getChannelEnableValue() {
    return ext.channelEnable
}

def getChannelConfigJsonValue() {
    return ext.configJson
}

def getChannelFlavorDirValue() {
    return ext.flavorDir
}

// 打包插件
apply plugin: 'com.channel.apkbuild'

// 渠道打包配置
channelApkBuild {
    // 是否启用渠道包任务
    channelEnable = getChannelEnableValue()
    // 渠道配置json文件位置
    configJson = getChannelConfigJsonValue()
    // flavor资源文件位置
    flavorDir = getChannelFlavorDirValue()
    // release渠道包放置位置，默认放在 build/outputs/release/ 文件夹中
    releaseChannelDir = file("${extraPath}/${date}/")
}
```

