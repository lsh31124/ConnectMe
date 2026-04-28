plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":connect-me-domain"))
    implementation(project(":connect-me-common"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}