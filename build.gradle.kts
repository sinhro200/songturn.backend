import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.1"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"

    id("nu.studer.jooq") version "5.2"
    id("org.flywaydb.flyway") version "7.2.0"

    application
}

application {
    mainClass.set("com.sinhro.songturn.backend.BackendApplication")
}

group = "com.sinhro.songturn"
version = "1.0.3"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

val dbConfig = mapOf(
        "url" to (System.getenv("DATABASE_URL")),
        "schema" to (System.getenv("DATABASE_SCHEMA")),
        "user" to (System.getenv("DATABASE_USER")),
        "password" to (System.getenv("DATABASE_PASSWORD")),
        "driver" to (System.getenv("DATABASE_DRIVER"))
)

flyway {
    url = dbConfig["url"]
    user = dbConfig["user"]
    password = dbConfig["password"]
    schemas = arrayOf(dbConfig["schema"])
//    locations = arrayOf("classpath:db/migration")
}

jooq {

    version.set("3.14.4")  // default (can be omitted)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)  // default (can be omitted)
    configurations {
        create("main") {  // name of the jOOQ configuration
//            generateSchemaSourceOnCompilation.set(true)  // default (can be omitted)
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.DEBUG
                jdbc.apply {
                    driver = dbConfig["driver"]
                    url = dbConfig["url"]
                    user = dbConfig["user"]
                    password = dbConfig["password"]
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    database.apply {
                        isIncludeIndexes = false
                        inputSchema = dbConfig["schema"]
                        forcedTypes.addAll(arrayOf(
                                org.jooq.meta.jaxb.ForcedType()
                                        .withName("varchar")
                                        .withIncludeExpression(".*")
                                        .withIncludeTypes("JSONB?"),
                                org.jooq.meta.jaxb.ForcedType()
                                        .withName("varchar")
                                        .withIncludeExpression(".*")
                                        .withIncludeTypes("INET")
                        ).toList())
                        /*properties.add(org.jooq.meta.jaxb.Property()
                                .withKey("ssl")
                                .withValue("false"))
                        properties.add(org.jooq.meta.jaxb.Property()
                                .withKey("useSSL")
                                .withValue("false")
                        )*/
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
//						packageName = "nu.studer.sample"
//						directory = "build/generated-src/jooq/main"  // default (can be omitted)
                        packageName = "com.sinhro.songturn.backend"
                        directory = "build/generated-src/jooq/main"

                    }
                }
            }
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.3")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    runtimeOnly("org.postgresql:postgresql")
    jooqGenerator("org.postgresql:postgresql")

    //###
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
//    implementation("io.jsonwebtoken:jjwt:0.6.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
    // Uncomment the next line if you want to use RSASSA-PSS (PS256, PS384, PS512) algorithms:
    // runtimeOnly("org.bouncycastle:bcprov-jdk15on:1.60")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2") // or 'io.jsonwebtoken:jjwt-gson:0.11.2' for gson

    //###

    implementation("org.springframework.boot:spring-boot-starter-mail:1.2.0.RELEASE")

    implementation("com.squareup.okhttp3:okhttp:3.14.6")

//      ### SPRING TESTING
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("junit:junit:4.13")
    //testImplementation("org.springframework:spring-test:4.3.2.RELEASE")
    testImplementation("org.springframework:spring-test")
    testImplementation("com.jayway.jsonpath:json-path:2.2.0")
    testImplementation("org.hamcrest:hamcrest-library:1.3")
    testCompileOnly("org.mockito:mockito-core:2.1.0")

//    implementation("com.sinhro.songturn:rest:0.0.1")
    implementation(project(":rest"))

//    compile(project(":rest"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnit()

    maxHeapSize = "1G"
//    useJUnitPlatform()

}

tasks.register<Exec>("publish-heroku") {
    commandLine(
            "C:\\Windows\\System32\\wsl.exe",
                    "heroku container:push web -a songturn",
                    "&&",
                    "heroku container:release web -a songturn"
    )
}