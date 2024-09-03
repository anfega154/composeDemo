import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinKapt)
    id("realm-android")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("app.cash.sqldelight") version "2.0.1"
    kotlin("plugin.serialization") version "1.9.22"
    //id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

repositories {
    google()
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
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

            implementation("androidx.constraintlayout:constraintlayout:2.1.4")

            //cmms dependencies
            // Dependencias Firebase
            implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
            implementation("com.google.firebase:firebase-messaging")
            implementation("com.google.firebase:firebase-crashlytics")
            implementation("com.google.firebase:firebase-analytics")
            implementation("com.google.firebase:firebase-perf")

            // Realm
            implementation("io.realm.kotlin:library-base:1.0.0")
            implementation("io.realm:realm-android-library:10.18.0")

            // Otras dependencias de Android
            implementation("androidx.activity:activity:1.5.1")
            implementation("androidx.cardview:cardview:1.0.0")
            implementation("androidx.multidex:multidex:2.0.1")
            implementation("androidx.appcompat:appcompat:1.6.1")
            implementation("androidx.legacy:legacy-support-v4:1.0.0")
            implementation("androidx.recyclerview:recyclerview:1.3.1")
            implementation("com.google.android.material:material:1.9.0")
            implementation("com.getbase:floatingactionbutton:1.10.1")
            implementation("com.journeyapps:zxing-android-embedded:4.3.0") {
                isTransitive = false
            }
            implementation("com.google.zxing:core:3.3.0")

            // RxJava
            implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
            implementation("io.reactivex.rxjava2:rxjava:2.2.21")

            // Socket.IO Client
            implementation("com.github.nkzawa:socket.io-client:0.5.2")

            implementation("com.google.android.gms:play-services-location:21.0.1")
            implementation("com.github.vipulasri:timelineview:1.1.5")
            implementation("com.github.bumptech.glide:glide:3.8.0")
            implementation("com.squareup.okhttp3:okhttp:4.10.0")
            implementation("com.google.code.gson:gson:2.8.9")
            implementation("com.larswerkman:HoloColorPicker:1.5")
            implementation("androidx.legacy:legacy-support-v4:1.0.0")


        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)//basicApp
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
    sourceSets["main"].java.srcDirs("src/androidMain/kotlin", "src/androidMain/legacy/java", "src/androidMain/cmms/java", "src/androidMain/component/java", "src/androidMain/core/java")

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
    kapt {
        correctErrorTypes = false
    }
}