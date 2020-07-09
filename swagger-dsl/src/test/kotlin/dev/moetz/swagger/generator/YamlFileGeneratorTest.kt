package dev.moetz.swagger.generator

import dev.moetz.swagger.builder.SwaggerBuilder
import dev.moetz.swagger.builder.model.Schema
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test

class YamlFileGeneratorTest {

    @Test
    fun testRecursiveSchemaDefinition() {
        val definition = SwaggerBuilder.generate {
            info {
                title("Test API")
                version("1.2.3")
                description("Some API description")
                host("swagger.example.org")
                basePath("/swaggertest/")
                schemes("https")
            }

            tag("someTag", "Some information about the tag")

            path("/somePath", "get") {
                operationId("somePath")
                tags("someTag")


                response(200) {
                    schema(createObjectSchema(name = "SomeObject") {
                        property("name", createTypeSchema(type = "string") {
                            format("double")
                            description("Some description")
                        })

                        property("recursiveObject", referenceRecursively(this))
                    })
                }
            }
        }

        val generatedYaml = YamlFileGenerator.generate(definition)

        generatedYaml shouldBeEqualTo """
swagger: '2.0'
info:
  description: "Some API description"
  version: '1.2.3'
  title: "Test API"
host: "swagger.example.org"
basePath: "/swaggertest/"
schemes:
  - https
tags:
  - name: someTag
    description: "Some information about the tag"
paths:
  "/somePath":
    get:
      tags:
        - someTag
      operationId: "somePath"
      responses:
        200:
          schema:
            ${"\$"}ref: '#/definitions/SomeObject'
definitions:
  SomeObject:
    type: object
    properties:
      name:
        type: string
        description: "Some description"
        format: "double"
      recursiveObject:
        ${"\$"}ref: '#/definitions/SomeObject'
""".trimMargin()
    }

    @Test
    fun testStringType() {
        val definition = SwaggerBuilder.generate {
            info {
                title("Test API")
                version("1.2.3")
                description("Some API description")
                host("swagger.example.org")
                basePath("/swaggertest/")
                schemes("https")
            }

            tag("someTag", "Some information about the tag")

            path("/somePath", "get") {
                operationId("somePath")
                tags("someTag")


                response(200) {
                    schema(createObjectSchema(name = "SomeObject") {
                        property("name", createTypeSchema(type = "string") {
                            description("Some description")
                            minLength(5)
                            maxLength(10)
                        })
                    })
                }
            }
        }

        val generatedYaml = YamlFileGenerator.generate(definition)

        generatedYaml shouldBeEqualTo """
swagger: '2.0'
info:
  description: "Some API description"
  version: '1.2.3'
  title: "Test API"
host: "swagger.example.org"
basePath: "/swaggertest/"
schemes:
  - https
tags:
  - name: someTag
    description: "Some information about the tag"
paths:
  "/somePath":
    get:
      tags:
        - someTag
      operationId: "somePath"
      responses:
        200:
          schema:
            ${"\$"}ref: '#/definitions/SomeObject'
definitions:
  SomeObject:
    type: object
    properties:
      name:
        type: string
        description: "Some description"
        minLength: 5
        maxLength: 10
""".trimMargin()
    }

    @Test
    fun testPatternStringType() {
        val definition = SwaggerBuilder.generate {
            info {
                title("Test API")
                version("1.2.3")
                description("Some API description")
                host("swagger.example.org")
                basePath("/swaggertest/")
                schemes("https")
            }

            tag("someTag", "Some information about the tag")

            path("/somePath", "get") {
                operationId("somePath")
                tags("someTag")


                response(200) {
                    schema(createObjectSchema(name = "SomeObject") {
                        property("name", createTypeSchema(type = "string") {
                            description("Some description")
                            pattern("[1234]\\asdf\$")
                        })
                    })
                }
            }
        }

        val generatedYaml = YamlFileGenerator.generate(definition)

        generatedYaml shouldBeEqualTo """
swagger: '2.0'
info:
  description: "Some API description"
  version: '1.2.3'
  title: "Test API"
host: "swagger.example.org"
basePath: "/swaggertest/"
schemes:
  - https
tags:
  - name: someTag
    description: "Some information about the tag"
paths:
  "/somePath":
    get:
      tags:
        - someTag
      operationId: "somePath"
      responses:
        200:
          schema:
            ${"\$"}ref: '#/definitions/SomeObject'
definitions:
  SomeObject:
    type: object
    properties:
      name:
        type: string
        description: "Some description"
        pattern: [1234]\asdf${'$'}
""".trimMargin()
    }

    @Test
    fun numberTypeSchemaShouldNotBeAbleToDefineLength() {
        {
            SwaggerBuilder.generate {
                info {
                    title("Test API")
                    version("1.2.3")
                    description("Some API description")
                    host("swagger.example.org")
                    basePath("/swaggertest/")
                    schemes("https")
                }

                tag("someTag", "Some information about the tag")

                path("/somePath", "get") {
                    operationId("somePath")
                    tags("someTag")


                    response(200) {
                        schema(createObjectSchema(name = "SomeObject") {
                            property("name", createTypeSchema(type = "number") {
                                description("Some description")
                                minLength(5)
                                maxLength(10)
                            })
                        })
                    }
                }
            }
        } shouldThrow Schema.ValidationException::class
    }

    @Test
    fun numberTypeDefineMinAndMax() {
        val definition = SwaggerBuilder.generate {
            info {
                title("Test API")
                version("1.2.3")
                description("Some API description")
                host("swagger.example.org")
                basePath("/swaggertest/")
                schemes("https")
            }

            tag("someTag", "Some information about the tag")

            path("/somePath", "get") {
                operationId("somePath")
                tags("someTag")


                response(200) {
                    schema(createObjectSchema(name = "SomeObject") {
                        property("name", createTypeSchema(type = "number") {
                            description("Some description")
                            minimum(5, true)
                            maximum(10, false)
                        })
                    })
                }
            }
        }

        val generatedYaml = YamlFileGenerator.generate(definition)

        generatedYaml shouldBeEqualTo """
swagger: '2.0'
info:
  description: "Some API description"
  version: '1.2.3'
  title: "Test API"
host: "swagger.example.org"
basePath: "/swaggertest/"
schemes:
  - https
tags:
  - name: someTag
    description: "Some information about the tag"
paths:
  "/somePath":
    get:
      tags:
        - someTag
      operationId: "somePath"
      responses:
        200:
          schema:
            ${"\$"}ref: '#/definitions/SomeObject'
definitions:
  SomeObject:
    type: object
    properties:
      name:
        type: number
        description: "Some description"
        minimum: 5
        exclusiveMinimum: true
        maximum: 10
        exclusiveMaximum: false
""".trimMargin()
    }

    @Test
    fun numberTypeDefineMultipleOf() {
        val definition = SwaggerBuilder.generate {
            info {
                title("Test API")
                version("1.2.3")
                description("Some API description")
                host("swagger.example.org")
                basePath("/swaggertest/")
                schemes("https")
            }

            tag("someTag", "Some information about the tag")

            path("/somePath", "get") {
                operationId("somePath")
                tags("someTag")


                response(200) {
                    schema(createObjectSchema(name = "SomeObject") {
                        property("name", createTypeSchema(type = "number") {
                            description("Some description")
                            multipleOf(2)
                        })
                    })
                }
            }
        }

        val generatedYaml = YamlFileGenerator.generate(definition)

        generatedYaml shouldBeEqualTo """
swagger: '2.0'
info:
  description: "Some API description"
  version: '1.2.3'
  title: "Test API"
host: "swagger.example.org"
basePath: "/swaggertest/"
schemes:
  - https
tags:
  - name: someTag
    description: "Some information about the tag"
paths:
  "/somePath":
    get:
      tags:
        - someTag
      operationId: "somePath"
      responses:
        200:
          schema:
            ${"\$"}ref: '#/definitions/SomeObject'
definitions:
  SomeObject:
    type: object
    properties:
      name:
        type: number
        description: "Some description"
        multipleOf: 2
""".trimMargin()
    }

    @Test
    fun numberTypeDefineMinMaxAndMultipleOf() {
        val definition = SwaggerBuilder.generate {
            info {
                title("Test API")
                version("1.2.3")
                description("Some API description")
                host("swagger.example.org")
                basePath("/swaggertest/")
                schemes("https")
            }

            tag("someTag", "Some information about the tag")

            path("/somePath", "get") {
                operationId("somePath")
                tags("someTag")


                response(200) {
                    schema(createObjectSchema(name = "SomeObject") {
                        property("name", createTypeSchema(type = "number") {
                            description("Some description")
                            multipleOf(2)
                            minimum(5, true)
                            maximum(10, false)
                        })
                    })
                }
            }
        }

        val generatedYaml = YamlFileGenerator.generate(definition)

        generatedYaml shouldBeEqualTo """
swagger: '2.0'
info:
  description: "Some API description"
  version: '1.2.3'
  title: "Test API"
host: "swagger.example.org"
basePath: "/swaggertest/"
schemes:
  - https
tags:
  - name: someTag
    description: "Some information about the tag"
paths:
  "/somePath":
    get:
      tags:
        - someTag
      operationId: "somePath"
      responses:
        200:
          schema:
            ${"\$"}ref: '#/definitions/SomeObject'
definitions:
  SomeObject:
    type: object
    properties:
      name:
        type: number
        description: "Some description"
        minimum: 5
        exclusiveMinimum: true
        maximum: 10
        exclusiveMaximum: false
        multipleOf: 2
""".trimMargin()
    }

    @Test
    fun testArrayType() {
        val definition = SwaggerBuilder.generate {
            info {
                title("Test API")
                version("1.2.3")
                description("Some API description")
                host("swagger.example.org")
                basePath("/swaggertest/")
                schemes("https")
            }

            tag("someTag", "Some information about the tag")

            path("/somePath", "get") {
                operationId("somePath")
                tags("someTag")

                queryParameter("test") {
                    description("array query parameter")
                    array("csv", createArraySchema {
                        items(createTypeSchema("string") {
                            description("type of array")
                        })
                    })
                }

                response(200) {
                    description("success")
                }
            }
        }

        val generatedYaml = YamlFileGenerator.generate(definition)

        generatedYaml.trimMargin() shouldBeEqualTo """
swagger: '2.0'
info:
  description: "Some API description"
  version: '1.2.3'
  title: "Test API"
host: "swagger.example.org"
basePath: "/swaggertest/"
schemes:
  - https
tags:
  - name: someTag
    description: "Some information about the tag"
paths:
  "/somePath":
    get:
      tags:
        - someTag
      operationId: "somePath"
      parameters:
        - name: test
          in: query
          description: "array query parameter"
          collectionFormat: csv
          type: array
          items:
            type: string
            description: "type of array"
      responses:
        200:
          description: "success"
 """.trimMargin()
    }

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


                response(201) {
                    description("Hektometer point found, returning its coordinates")
                    schema(createObjectSchema(name = "HektometerResponse") {
                        description("A hektometer point with its coordinates")
                        required(true)

                        property("km", createTypeSchema(type = "number") {
                            format("double")
                            description("The km of this hektometer point")
                            example("7.6")
                            required(true)
                        })

                        property("lat", createTypeSchema(type = "number") {
                            format("double")
                            description("The latitude of this hektometer point")
                            example("48.57564545816002")
                            required(true)
                        })

                        property("lng", createTypeSchema(type = "number") {
                            format("double")
                            description("The longitude of this hektometer point")
                            example("16.701478681218024")
                            required(true)
                        })
                    })
                }
                response(500) {
                    description("Error in API")
                }
            }
        }

        val generatedYaml = YamlFileGenerator.generate(definition)

        generatedYaml shouldBeEqualTo """
swagger: '2.0'
info:
  description: |
    Some API description
    Which is multiline
    Even 3 lines here
  version: '1.2.3'
  title: "Test API"
host: "swagger.example.org"
basePath: "/swaggertest/"
schemes:
  - https
tags:
  - name: someTag
    description: |
      Some information about the tag
      Even multiline!
paths:
  "/status":
    get:
      tags:
        - status
      summary: "Endpoint to check the API status"
      description: "Returns 200 on a healthy API with some additional information."
      operationId: "status"
      produces:
        - text/html
      responses:
        200:
          description: "API is up & running"
        201:
          description: "Hektometer point found, returning its coordinates"
          schema:
            ${"\$"}ref: '#/definitions/HektometerResponse'
        500:
          description: "Error in API"
definitions:
  HektometerResponse:
    type: object
    description: "A hektometer point with its coordinates"
    required:
      - km
      - lat
      - lng
    properties:
      km:
        type: number
        description: "The km of this hektometer point"
        example: "7.6"
        format: "double"
      lat:
        type: number
        description: "The latitude of this hektometer point"
        example: "48.57564545816002"
        format: "double"
      lng:
        type: number
        description: "The longitude of this hektometer point"
        example: "16.701478681218024"
        format: "double"
""".trimMargin()
    }

}