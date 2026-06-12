import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.kiosk.donation"
    compileSdk = 35

    val versionPropsFile = File(project.rootDir, "version.properties")
    val versionProps = Properties()
    if (versionPropsFile.exists()) {
        versionProps.load(versionPropsFile.inputStream())
    }
    
    val currentVersionCode = versionProps.getProperty("VERSION_CODE", "1").toInt()
    val currentVersionName = versionProps.getProperty("VERSION_NAME", "1.0.0")

    defaultConfig {
        applicationId = "com.kiosk.donation"
        minSdk = 26
        targetSdk = 35
        versionCode = currentVersionCode
        versionName = currentVersionName
    }

    // Increment version for the NEXT build
    project.gradle.buildFinished {
        if (versionPropsFile.exists()) {
            val nextVersionCode = currentVersionCode + 1
            val parts = currentVersionName.split(".")
            val nextVersionName = if (parts.size >= 3) {
                try {
                    val patch = parts[2].toInt() + 1
                    "${parts[0]}.${parts[1]}.$patch"
                } catch (e: Exception) {
                    "$currentVersionName.1"
                }
            } else {
                "$currentVersionName.1"
            }
            versionProps.setProperty("VERSION_CODE", nextVersionCode.toString())
            versionProps.setProperty("VERSION_NAME", nextVersionName.toString())
            versionPropsFile.outputStream().use { 
                versionProps.store(it, null)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val baseName = "KarimaDonations"
            val version = variant.versionName
            val type = variant.buildType.name
            val fileName = "${baseName}-v${version}-${type}.apk"
            output.outputFileName = fileName

            // Copy to web-admin folder after build
            variant.assembleProvider.configure {
                doLast {
                    val apkFile = output.outputFile
                    val destinationDir = File(project.rootDir, "web-admin")
                    if (apkFile.exists() && destinationDir.exists()) {
                        // Copy with version name
                        copy {
                            from(apkFile)
                            into(destinationDir)
                        }
                        // Also copy as "latest" for the web portal link
                        copy {
                            from(apkFile)
                            into(destinationDir)
                            rename { "KarimaDonations-latest.apk" }
                        }

                        // Generate version.json for the web portal
                        val versionFile = File(destinationDir, "version.json")
                        versionFile.writeText("{\"version\": \"$version\"}")
                        
                        println("Successfully updated APKs and version.json in web-admin folder")
                    }
                }
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Activity
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")

    // SumUp Android SDK
    implementation("com.sumup:merchant-sdk:6.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // DataStore for admin PIN / settings
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Room database for local product cache
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Firebase BOM — manages all Firebase library versions
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Coil — loads product images from file paths
    implementation("io.coil-kt:coil-compose:2.6.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
