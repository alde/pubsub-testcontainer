plugins {
    `build-scan`
    `maven-publish`

    kotlin("jvm") version "1.3.31"
}

repositories {
    jcenter()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib"))
    api("com.google.cloud:google-cloud-pubsub:1.48.0")
    api("org.testcontainers:testcontainers:1.11.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.awaitility:awaitility:3.1.6")
    testImplementation("org.assertj:assertj-core:3.11.1")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "nu.alde"
            artifactId = "pubsub-testcontainer"
            version = "0.0.4"

            from(components["java"])
        }
    }
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlways()
}