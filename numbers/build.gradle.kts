plugins {
    id("java-library")
}

dependencies {
    implementation(libs.nanojson)
    implementation(libs.annotations)
    testImplementation(libs.junit)
}
