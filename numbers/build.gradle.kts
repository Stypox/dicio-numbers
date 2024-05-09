plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("java-library")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.nanojson)
    testImplementation(libs.junit)
}
