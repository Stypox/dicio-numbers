plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("java-library")
}

dependencies {
    implementation(libs.nanojson)
    testImplementation(libs.junit)
}
