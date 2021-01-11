import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.1"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"

    id("nu.studer.jooq") version "5.2"
    id("org.flywaydb.flyway") version "7.2.0"
}

group = "com.sinhro.songturn"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

val jooqDb = mapOf(
        "url" to (System.getenv("jdbc.url") ?: "jdbc:postgresql://localhost:5432/songturn"),
        "schema" to (System.getenv("jdbc.schema") ?: "public"),
        "user" to (System.getenv("jdbc.user") ?: "sinhro"),
        "password" to (System.getenv("jdbc.password") ?: "1234"),
        "driver" to (System.getenv("jdbc.driver") ?: "org.postgresql.Driver")
)

flyway {
    url = jooqDb["url"]
    user = jooqDb["user"]
    password = jooqDb["password"]
    schemas = arrayOf(jooqDb["schema"])
//    locations = arrayOf("classpath:db/migration")
}

jooq {
    version.set("3.14.4")  // default (can be omitted)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)  // default (can be omitted)
    configurations {
        create("main") {  // name of the jOOQ configuration
//            generateSchemaSourceOnCompilation.set(true)  // default (can be omitted)
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = jooqDb["driver"]
                    url = jooqDb["url"]
                    user = jooqDb["user"]
                    password = jooqDb["password"]

                    properties.add(
                            org.jooq.meta.jaxb.Property()
                                    .withKey("ssl")
                                    .withValue("false")//false
                    )
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    database.apply {
                        inputSchema = jooqDb["schema"]
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

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation(module("com.sinhro.songturn:rest"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
//    useJUnit()
    useJUnitPlatform()

}
