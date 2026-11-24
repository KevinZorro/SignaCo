import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Rutas de navegacion type-safe (@Serializable en core/navegacion/Rutas.kt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.firebase.appdistribution)
    alias(libs.plugins.google.gms.google.services)
    // Cobertura de las pruebas unitarias (RNF-19). Viene con Gradle: no es una biblioteca.
    jacoco
}

android {
    namespace = "com.ufps.signaco"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ufps.signaco"
        minSdk = 26
        targetSdk = 37
        // CI pasa -PciVersionCode=<n.º de ejecución> para que cada APK distribuido sea instalable
        // encima del anterior; en local queda el valor fijo.
        versionCode = (findProperty("ciVersionCode") as String?)?.toIntOrNull() ?: 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            optimization {
                enable = false
            }
            firebaseAppDistribution {
                artifactType = "APK"
                testersFile = "$projectDir/testers.txt"
                releaseNotes = "Nueva versión de prueba para SignaCo."
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        // Necesario para que RegistroDeDepuracion sepa si es una build de depuración.
        buildConfig = true
    }
    androidResources {
        // El .tflite y el .task se cargan con MappedByteBuffer directamente desde el APK.
        // Si Gradle los comprime, el mapeo falla.
        noCompress += listOf("tflite", "task")
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    sourceSets {
        // MigrationTestHelper lee los esquemas exportados como assets de las pruebas.
        getByName("androidTest").assets.directories.add("$projectDir/schemas")
    }
}

// Room escribe aqui los esquemas de la base. Sirven para revisar migraciones en code review
// y para las pruebas de migracion cuando el esquema cambie.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Compose / UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navegacion
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Asincronia
    implementation(libs.kotlinx.coroutines.android)

    // Inyeccion de dependencias
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Persistencia
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Cámara, visión e inferencia
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mediapipe.tasks.vision)
    implementation(libs.tensorflow.lite)

    // Pruebas unitarias (capa domain)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Pruebas instrumentadas
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// --- Cobertura de domain (RNF-19: objetivo 40 %) ---
//
// Se mide a mano y no en cada build:
//   ./gradlew coberturaDomain -Pcobertura
// El reporte queda en app/build/reports/jacoco/coberturaDomain/html/index.html.
//
// Sin -Pcobertura el agente de JaCoCo no se engancha a las pruebas, así que el CI y el
// build de siempre quedan exactamente igual. No toca el APK, el modelo ni los assets.
val medirCobertura = providers.gradleProperty("cobertura").isPresent

jacoco {
    toolVersion = "0.8.14"
}

tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isEnabled = medirCobertura
    }
}

tasks.register<JacocoReport>("coberturaDomain") {
    group = "verification"
    description = "Cobertura de las pruebas unitarias sobre los paquetes domain."
    dependsOn("testDebugUnitTest")

    val build = layout.buildDirectory
    // Solo las clases de domain. Se descartan las fábricas que generan Hilt y Dagger: no son
    // código del equipo y bajarían el porcentaje sin decir nada.
    classDirectories.setFrom(
        fileTree(build) {
            include(
                "intermediates/built_in_kotlinc/debug/**/domain/**/*.class",
                "tmp/kotlin-classes/debug/**/domain/**/*.class",
            )
            exclude("**/*_Factory*", "**/*_MembersInjector*", "**/Hilt_*", "**/*_HiltModules*")
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(build) { include("jacoco/testDebugUnitTest.exec") })

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(true)
    }
}
