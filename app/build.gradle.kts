plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Local signing.properties is created by scripts/generate-signing-key.sh.
// CI can provide the same values through OPENCODE_* environment variables.
val signingProperties = java.util.Properties().apply {
    val signingFile = rootProject.file("signing.properties")
    if (signingFile.exists()) signingFile.inputStream().use { load(it) }
}
fun signingValue(propertyName: String, environmentName: String): String? =
    System.getenv(environmentName)?.takeIf { it.isNotBlank() }
        ?: signingProperties.getProperty(propertyName)?.takeIf { it.isNotBlank() }

val signingStorePath = signingValue("storeFile", "OPENCODE_KEYSTORE_PATH")
val signingStorePassword = signingValue("storePassword", "OPENCODE_KEYSTORE_PASSWORD")
val signingKeyAlias = signingValue("keyAlias", "OPENCODE_KEY_ALIAS")
val signingKeyPassword = signingValue("keyPassword", "OPENCODE_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    signingStorePath, signingStorePassword, signingKeyAlias, signingKeyPassword
).all { !it.isNullOrBlank() } && signingStorePath?.let { rootProject.file(it).exists() } == true

android {
    namespace = "com.opencode.ai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.opencode.ai"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("appRelease") {
                storeFile = rootProject.file(requireNotNull(signingStorePath))
                storePassword = requireNotNull(signingStorePassword)
                keyAlias = requireNotNull(signingKeyAlias)
                keyPassword = requireNotNull(signingKeyPassword)
            }
        }
    }

    buildTypes {
        release {
            // Release builds use the generated JKS when configured. Without it,
            // Gradle can still produce an unsigned release artifact for CI checks.
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("appRelease")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            // Use the app JKS for debug too when configured; otherwise Gradle's
            // standard debug key keeps developer/CI builds working out of box.
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("appRelease")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
