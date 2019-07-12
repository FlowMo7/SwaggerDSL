package dev.moetz.swagger.generator

import dev.moetz.swagger.builder.SwaggerBuilder
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class YamlFileGeneratorTest {

    @Test
    fun simpleYamlDefinitionTest() {
        val definition = SwaggerBuilder.generate {
            info {
                title("Test API")
                version("1.2.3")
                description(
                    "Some API description\n" +
                            "Which is multiline\n" +
                            "Even 3 lines here"
                )
                host("swagger.example.org")
                basePath("/swaggertest/")
                schemes("https")
            }

            tag(
                "someTag", "Some information about the tag\n" +
                        "Even multiline!"
            )

            path("/status", "get") {
                summary("Endpoint to check the API status")
                description("Returns 200 on a healthy API with some additional information.")
                operationId("status")
                tags("status")
                produces("text/html")
                response(200) {
                    description("API is up & running")
                }
                response(500) {
                    description("Error in API")
                }
            }
        }

        val generatedYaml = YamlFileGenerator.generate(definition)

        generatedYaml shouldBeEqualTo """
            |swagger: '2.0'
            |info:
            |  description: |
            |    Some API description
            |    Which is multiline
            |    Even 3 lines here
            |  version: '1.2.3'
            |  title: "Test API"
            |host: "swagger.example.org"
            |basePath: "/swaggertest/"
            |schemes:
            |  - https
            |tags:
            |  - name: someTag
            |    description: |
            |      Some information about the tag
            |      Even multiline!
            |paths:
            |  "/status":
            |    get:
            |      tags:
            |      - status
            |      summary: "Endpoint to check the API status"
            |      description: "Returns 200 on a healthy API with some additional information."
            |      operationId: "status"
            |      produces:
            |        - text/html
            |      responses:
            |        200:
            |          description: "API is up & running"
            |        500:
            |          description: "Error in API"
            |""".trimMargin()
    }

}