import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.compose.ExperimentalComposeLibrary


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("app.cash.sqldelight") version "2.0.1"
    kotlin("plugin.serialization") version "1.9.22"

}

repositories {
    google()
    mavenCentral()
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.demo")
        }
    }
}

kotlin {

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)

            dependencies {
                implementation("androidx.compose.ui:ui-test-junit4-android:1.5.4")
                debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation ("com.google.accompanist:accompanist-systemuicontroller:0.31.3-beta")

            implementation(project.dependencies.platform("io.insert-koin:koin-bom:3.5.1"))
            implementation("io.insert-koin:koin-core")
            implementation("io.insert-koin:koin-android")

            implementation("app.cash.sqldelight:android-driver:2.0.1")

            //Ktor
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)

        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)

            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")


            //Navigation PreCompose
            api("moe.tlaster:precompose:1.5.10")
            //Viewmodel
            api("moe.tlaster:precompose-viewmodel:1.5.10")

            //Koin
            implementation(project.dependencies.platform("io.insert-koin:koin-bom:3.5.1"))
            implementation("io.insert-koin:koin-core")
            implementation("io.insert-koin:koin-compose")
            api("moe.tlaster:precompose-koin:1.5.10")

            //ktor
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.content.negotiation)

            //serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0") // Ensure the version matches your Kotlin version

        }
        iosMain.dependencies {
            implementation("app.cash.sqldelight:native-driver:2.0.1")
            implementation("co.touchlab:stately-common:2.0.5")

            //Ktor
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.mantum.demo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.mantum.demo"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

