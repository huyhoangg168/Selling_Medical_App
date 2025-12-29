import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

val localProps = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

val apiKey: String = localProps.getProperty("API_KEY") ?: ""
val apiUrl: String = localProps.getProperty("API_URL") ?: ""

android {
    namespace = "com.example.clientsellingmedicine"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("key_releases/medimate_key_apk.jks")
            storePassword = "123456"
            keyAlias = "medimate_v1"
            keyPassword = "123456"
        }
    }

    defaultConfig {
        applicationId = "com.example.clientsellingmedicine"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_KEY", "\"$apiKey\"")
            buildConfigField("String", "API_URL", "\"$apiUrl\"")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")

            buildConfigField("String", "API_KEY", "\"$apiKey\"")
            buildConfigField("String", "API_URL", "\"$apiUrl\"")
        }
    }
}

dependencies {
    implementation("com.google.firebase:firebase-auth")
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-recaptcha:17.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("androidx.cardview:cardview:1.0.0")
    // Retrofit and GSON
    implementation ("com.squareup.retrofit2:retrofit:2.3.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.3.0")
    //Jsoup
    implementation ("org.jsoup:jsoup:1.14.3")

    // Logging
    implementation ("com.squareup.okhttp3:logging-interceptor:3.9.0")
    // load image
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    //slider
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.2")
    //lombok
    implementation ("org.projectlombok:lombok:1.18.20")
    annotationProcessor ("org.projectlombok:lombok:1.18.20")

    implementation ("androidx.activity:activity-ktx:1.4.0")
    //webview
    implementation("androidx.webkit:webkit:1.8.0")

    //cloudinary
    implementation ("com.cloudinary:cloudinary-android:2.7.1")
    //Oauth google
    implementation ("com.google.android.gms:play-services-auth:20.6.0")
    //loading Animations
    implementation ("com.airbnb.android:lottie:5.2.0")
    //open browser for captcha verification
    implementation ("androidx.browser:browser:1.3.0")

    implementation ("com.google.android.gms:play-services-safetynet:18.0.1")
    
    // Mã hóa và bảo mật
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Vân tay và sinh trắc học
    implementation("androidx.biometric:biometric:1.1.0")
    
    // Recaptcha (đã có ở trên nhưng version mới hơn ở đây)
    // implementation("com.google.android.gms:play-services-recaptcha:17.1.0") // Đã khai báo ở trên
}