import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
	repositories {
		mavenCentral()
		// for local testing:
		// mavenLocal()
		maven("https://jitpack.io")
	}
	dependencies {
		classpath("com.vanniktech:gradle-maven-publish-plugin:0.11.1")

		// self-using the plugin
		classpath("com.github.rvp-diconium:androidcCodeStylePlugin:1.0.0")

		// to test from ./gradlew installArchives
		// first comment out the usage from the .jar file above
		// and uncomment the line below
		// classpath("com.diconium.android.codestyle:codestyle:0.1.0-SNAPSHOT")
	}
}

apply {
	plugin("com.vanniktech.maven.publish")
	plugin("com.diconium.android.codestyle")
}

plugins {
	`kotlin-dsl`
	jacoco
	kotlin("jvm") version "1.3.72"
}

tasks.jacocoTestReport {
	reports {
		xml.isEnabled = false
		csv.isEnabled = true
		html.isEnabled = true
	}
}

extensions.configure<com.diconium.android.codestyle.CodeStyleConfig>("codeStyle") {
	debug = true
	downloadDir = ".idea/modules/codestyle"
}

repositories {
	mavenCentral()
	jcenter()
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("commons-codec:commons-codec:1.14")
	testImplementation("junit:junit:4.13")
}

tasks.create("cleanBuildTestJacoco") {
	group = "build"
	dependsOn("clean")
	finalizedBy(
		"assemble", "test", "build", "jacocoTestReport"
	)
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
	jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
	jvmTarget = "1.8"
}