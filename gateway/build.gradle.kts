java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.security:spring-security-config")
  	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.cloud:spring-cloud-starter-gateway:3.1.1")
	implementation("javax.xml.bind:jaxb-api:2.3.1") // Java 9/10 deprecates, 11 removes javax.xml.bind.DatatypeConverter
	implementation("com.auth0:java-jwt:${Versions.AUTH0_JAVA_JWT}")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude("org.junit.vintage", "junit-vintage-engine")
	}
	testImplementation("com.squareup.okhttp3:mockwebserver:4.0.0")
	testImplementation("com.squareup.okhttp3:okhttp:4.0.0")
}

// use JUnit 5 platform
tasks.withType<Test> {
	useJUnitPlatform()
}

jib {
	val publish: Publish by project.extra

	from {
		// base image
		image = "gcr.io/distroless/java:11"
	}
	to {
		image = publish.repository()
		auth {
			username = publish.username
			password = publish.password
		}
	}
	container {
		creationTime = publish.created
		labels = publish.ociAnnotations()
		ports = listOf("8080", "8443")
	}
}