plugins {
  kotlin("jvm") version "1.8.10"
}

val version: String by project
val maven_group: String by project
val archives_base_name: String by project

group = maven_group

repositories {
  mavenCentral()
  maven("https://m2.dv8tion.net/releases")
}

dependencies {
  implementation("net.dv8tion:JDA:5.0.0-beta.3") {
    exclude(module = "opus-java")
  }
  implementation("club.minnced:discord-webhooks:0.8.0")
  implementation("com.google.code.gson:gson:2.10.1")
  implementation("org.apache.logging.log4j:log4j-core:2.20.0")
  implementation("org.apache.logging.log4j:log4j-api:2.20.0")
}

val targetJavaVersion = 17

tasks {
  processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
      expand("version" to project.version)
    }
  }

  compileKotlin {
    kotlinOptions.jvmTarget = "17"
  }

  withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
      options.release.set(targetJavaVersion)
    }
  }

  jar {
    manifest {
      attributes("Main-Class" to "ca.sammdot.ploverbot.PloverBotKt")
    }

    from("LICENSE") {
      rename { "${it}_${archives_base_name}" }
    }

    from({
      configurations.runtimeClasspath.get().map { zipTree(it) }
    }) {
      duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    archiveBaseName.set(archives_base_name)
  }
}

java {
  val javaVersion = JavaVersion.toVersion(targetJavaVersion)
  if (JavaVersion.current() < javaVersion) {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
  }
}