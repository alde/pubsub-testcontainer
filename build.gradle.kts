plugins {
    kotlin("jvm") version "1.3.31"

    `build-scan`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}
val sonatypeUsername = System.getenv("SONATYPE_USERNAME")
val sonatypePassword = System.getenv("SONATYPE_PASSWORD")

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom {
                name.set("PubSub Testcontainer")
                description.set("Testcontainer with Google PubSub emulator")
                url.set("https://github.com/alde/pubsub-testcontainer")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("alde")
                        name.set("Rickard Dybeck")
                        email.set("r.dybeck@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/alde/pubsub-testcontainer.git")
                    developerConnection.set("scm:git:ssh://github.com/alde/pubsub-testcontainer.git")
                    url.set("https://github.com/alde/pubsub-testcontainer")
                }
            }
            groupId = "nu.alde"
            artifactId = "pubsub-testcontainer"
            version = "0.0.5"

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
    repositories {
        maven {
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}


buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlways()
}