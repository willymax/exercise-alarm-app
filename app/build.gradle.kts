import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
val apikeyPropertiesFile = Properties().apply {
    load(rootProject.file("apikey.properties").inputStream())
}
android {
    namespace = "com.willymax.exercisealarm"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.willymax.exercisealarm"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // add manifestPlaceholders for spotify client id and secret
        manifestPlaceholders["redirectSchemeName"] = "http"
        manifestPlaceholders["redirectHostName"] = "com.willymax.exercisealarm"

        // read from apikey.properties file
        buildConfigField("String", "SPOTIFY_CLIENT_SECRET",
            apikeyPropertiesFile.getProperty("SPOTIFY_CLIENT_SECRET")
        )
        buildConfigField("String", "SPOTIFY_CLIENT_ID",
            apikeyPropertiesFile.getProperty("SPOTIFY_CLIENT_ID")
        )
        buildConfigField("String", "SPOTIFY_REDIRECT_URI",
            apikeyPropertiesFile.getProperty("SPOTIFY_REDIRECT_URI")
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(files("libs/spotify-auth-release-2.1.0.aar"))
    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // third party dependencies -------------------------------------------------------------------
    implementation("com.wdullaer:materialdatetimepicker:4.2.3")         // date & time picker
    implementation("com.balysv:material-ripple:1.0.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.8.6")
}