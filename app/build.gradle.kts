plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("io.objectbox")
}

android {
    namespace = "io.shubham0204.smollmandroid"
    compileSdk = 35
    ndkVersion = "27.2.12479018"

    defaultConfig {
        applicationId = "io.shubham0204.smollmandroid"
        minSdk = 28
        targetSdk = 35
        versionCode = 7
        versionName = "7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore.jks")
            storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEYSTORE_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += listOf(
                // for langgraph4j
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                // for rag
                "META-INF/DEPENDENCIES",
                "META-INF/DEPENDENCIES.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "/META-INF/{AL2.0,LGPL2.1}"
            )
        }
    }
    buildFeatures {
        compose = true
    }

    applicationVariants.configureEach {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
    configurations {
        create("cleanedAnnotations")
        implementation {
            exclude(group = "org.jetbrains", module = "annotations")
        }
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.icons.extended)
    implementation(libs.androidx.compose.navigation)

    implementation(project(":smollm"))
    implementation(project(":hf-model-hub-api"))

    // Koin: dependency injection
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.ui.text.google.fonts)

    implementation("org.bsc.langgraph4j:langgraph4j-core:1.5.11")
    implementation("org.bsc.langgraph4j:langgraph4j-langchain4j:1.5.11")
    implementation("org.bsc.langgraph4j:langgraph4j-agent-executor:1.5.11")
    implementation("dev.langchain4j:langchain4j:1.0.0-rc1")
    implementation("dev.langchain4j:langchain4j-open-ai:1.0.0-rc1")
    implementation("dev.langchain4j:langchain4j-ollama:1.0.0-rc1")
    implementation(project(":langgraph4j-android-adapter"))
    implementation(project(":rag_android"))

//    // Koin: dependency injection
//    implementation(libs.koin.android)
//    implementation(libs.koin.annotations)
//    implementation(libs.koin.androidx.compose)
//    implementation(libs.androidx.ui.text.google.fonts)

    ksp(libs.koin.ksp.compiler)

    // compose-markdown: Markdown rendering in Compose
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:ext-latex:4.6.2")
    implementation("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation("io.noties.markwon:ext-tables:4.6.2")
    implementation("io.noties.markwon:ext-tasklist:4.6.2")
    implementation("io.noties.markwon:linkify:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")
    implementation("io.noties.markwon:syntax-highlight:4.6.2")
    implementation("io.noties:prism4j:2.0.0")
    annotationProcessor("io.noties:prism4j-bundler:2.0.0")

    // Jetpack Paging3: loading paged data for Compose
    val pagingVersion = "3.3.5"
    implementation("androidx.paging:paging-runtime:$pagingVersion")
    implementation("androidx.paging:paging-compose:$pagingVersion")

    // Android Room: Local persistence with SQLite
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
