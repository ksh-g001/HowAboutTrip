import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id ("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.project.how"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.project.how"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildFeatures {
            dataBinding = true
            viewBinding = true
            buildConfig = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "API_SERVER", getApiKey("api_server"))
        resValue("string", "GOOGLE_AD_MOB_ID", getApiKey("google_ad_mob_id"))
        resValue("string", "GOOGLE_AD_MOB_UNIT_ID", getApiKey("google_ad_mob_unit_id"))
        resValue("string", "GOOGLE_AD_MOB_BANNER_TEST", getApiKey("google_ad_mob_banner_test"))
        resValue("string", "GOOGLE_MAP_API_KEY", getApiKey("google_map_api_key"))
        resValue("string", "GOOGLE_PLACE_API_KEY", getApiKey("google_place_api_key"))

        buildConfigField("String", "ERROR_IMAGE_URL", getApiKey("ERROR_IMAGE_URL"))
        buildConfigField("String", "GOOGLE_OAUTH_ID", getApiKey("google_oauth_id"))
        buildConfigField("String", "GOOGLE_MAP_API_KEY", getApiKey("google_map_api_key"))
        buildConfigField("String", "GOOGLE_PLACE_API_KEY", getApiKey("google_place_api_key"))
        buildConfigField("String", "GOOGLE_SERVER_ID", getApiKey("google_server_id"))
        buildConfigField("String", "API_SERVER", getApiKey("api_server"))
        buildConfigField("String", "TEMPORARY_IMAGE_URL", getApiKey("temporary_image_url"))
        buildConfigField("String", "ERROR_IMAGE_URL", getApiKey("ERROR_IMAGE_URL"))
        buildConfigField("String", "USER_AGENT", getApiKey("user_agent"))
        buildConfigField("String", "OCR_SERVER_URL", getApiKey("taggun_url"))
        buildConfigField("String", "OCR_API_KEY", getApiKey("taggun_api_key"))
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
}

fun getApiKey(propertyKey: String): String = gradleLocalProperties(rootDir, providers).getProperty(propertyKey)

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-ads:23.3.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("androidx.camera:camera-core:1.3.4")

    implementation("com.airbnb.android:lottie:6.4.0")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.5")
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:23.0.0")

    implementation("com.google.android.gms:play-services-auth:21.2.0")

    implementation("com.google.dagger:hilt-android:2.46")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.hilt:hilt-common:1.2.0")
    kapt("com.google.dagger:hilt-android-compiler:2.46")

    implementation("com.squareup.retrofit:retrofit:2.0.0-beta2")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    implementation("com.github.bumptech.glide:glide:5.0.0-rc01")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.libraries.places:places:3.5.0")

    implementation("androidx.room:room-ktx:2.7.0-alpha07")
    kapt("androidx.room:room-compiler:2.7.0-alpha07")

    implementation("androidx.work:work-runtime-ktx:2.9.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

kapt {
    correctErrorTypes = true
}
