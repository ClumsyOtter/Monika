pluginManagement {
    repositories {
        // 阿里云 Gradle 插件仓库镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
        // 阿里云 Google 镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // 阿里云 Maven 中央仓库镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云 Google 镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }
        // 阿里云 Maven 中央仓库镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        // 腾讯云镜像
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
        // 华为云镜像
        maven {
            url = uri("https://repo.huaweicloud.com/repository/maven/")
        }
        // JitPack（用于 GitHub 项目）
        maven {
            url = uri("https://jitpack.io")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "Monika"
include(":app")
 