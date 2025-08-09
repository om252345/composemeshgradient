plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

group = "io.github.om252345"
version = "0.1.0"
android {
    namespace = "io.github.om252345.composemeshgradient"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.core.ktx)
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = project.group.toString()
            artifactId = "composemeshgradient"
            version = project.version.toString()

            pom {
                name.set("Compose Mesh Gradient")
                description.set("An Android Compose library for mesh gradients.")
                url.set("https://github.com/om252345/composemeshgradient")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("om252345")
                        name.set("Omkar Deshmukh")
                        email.set("omkard@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/om252345/composemeshgradient.git")
                    developerConnection.set("scm:git:ssh://github.com/om252345/composemeshgradient.git")
                    url.set("https://github.com/om252345/composemeshgradient")
                }
            }
        }
    }
    repositories {
        maven {
            name = "Local"
            url = uri(layout.buildDirectory.dir("repo"))
            // For remote (Maven Central), add credentials & correct url as needed
        }
    }
}