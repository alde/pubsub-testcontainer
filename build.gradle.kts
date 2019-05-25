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

    implementation("com.google.cloud:google-cloud-pubsub") {
        version {
            strictly("[1.45, 2[")
            prefer("1.75.0")
        }
    }
    implementation("org.testcontainers:testcontainers:1.11.2")

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
            version = "0.0.5"

            from(components["java"])
        }
    }
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlways()
}