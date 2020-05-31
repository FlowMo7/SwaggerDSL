package dev.moetz.swagger

import dev.moetz.swagger.builder.SwaggerBuilder
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.Before
import org.junit.Test

class SwaggerHandlerTest {

    private lateinit var handler: SwaggerHandler

    @Before
    fun setUp() {
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

        handler = SwaggerHandler(
            swaggerDefinition = definition
        )
    }

    @Test
    fun `packaged swagger-ui contains index file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("index.html")) {
            "did respond null for 'index.html'"
        }

        contentType shouldBeEqualTo "text/html; charset=UTF-8"
        byteArray.toString(Charsets.UTF_8).shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains favicon-16 file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("favicon-16x16.png")) {
            "did respond null for 'favicon-16x16.png'"
        }

        contentType shouldBeEqualTo "image/png"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains favicon-32 file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("favicon-32x32.png")) {
            "did respond null for 'favicon-32x32.png'"
        }

        contentType shouldBeEqualTo "image/png"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains oauth2-redirect file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("oauth2-redirect.html")) {
            "did respond null for 'oauth2-redirect.html'"
        }

        contentType shouldBeEqualTo "text/html; charset=UTF-8"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains swagger-ui-css file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("swagger-ui.css")) {
            "did respond null for 'swagger-ui.css'"
        }

        contentType shouldBeEqualTo "text/css"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains swagger-ui-css-map file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("swagger-ui.css.map")) {
            "did respond null for 'swagger-ui.css.map'"
        }

        contentType shouldBeEqualTo "application/octet-stream"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains swagger-ui-js file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("swagger-ui.js")) {
            "did respond null for 'swagger-ui.js'"
        }

        contentType shouldBeEqualTo "application/x-javascript"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains swagger-ui-js-map file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("swagger-ui.js.map")) {
            "did respond null for 'swagger-ui.js.map'"
        }

        contentType shouldBeEqualTo "application/octet-stream"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains swagger-ui-bundle-js file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("swagger-ui-bundle.js")) {
            "did respond null for 'swagger-ui-bundle.js'"
        }

        contentType shouldBeEqualTo "application/x-javascript"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains swagger-ui-bundle-js-map file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("swagger-ui-bundle.js.map")) {
            "did respond null for 'swagger-ui-bundle.js.map'"
        }

        contentType shouldBeEqualTo "application/octet-stream"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains swagger-ui-standalone-preset-js file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("swagger-ui-standalone-preset.js")) {
            "did respond null for 'swagger-ui-standalone-preset.js'"
        }

        contentType shouldBeEqualTo "application/x-javascript"
        byteArray.shouldNotBeEmpty()
    }

    @Test
    fun `packaged swagger-ui contains swagger-ui-standalone-preset-js-map file`() {
        val (contentType, byteArray) = requireNotNull(handler.get("swagger-ui-standalone-preset.js.map")) {
            "did respond null for 'swagger-ui-standalone-preset.js.map'"
        }

        contentType shouldBeEqualTo "application/octet-stream"
        byteArray.shouldNotBeEmpty()
    }

}