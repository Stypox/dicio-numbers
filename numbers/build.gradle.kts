plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("java-library")
    id("maven-publish")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.nanojson)
    testImplementation(libs.junit)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.convention("sources");
    archiveClassifier.set("sources");
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Stypox/dicio-numbers")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = "org.dicio"
            artifactId = "numbers"
            version = System.getenv("COMMIT_SHA")
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
