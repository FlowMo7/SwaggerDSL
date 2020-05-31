package dev.moetz.swagger

import dev.moetz.swagger.definition.SwaggerDefinition
import dev.moetz.swagger.generator.YamlFileGenerator
import java.nio.charset.Charset


/**
 * A handler which returns the swagger ui file contents as well as the swaggerDefinition as a yaml file respectively
 * when calling [get] with a relative url for the swagger path handling.
 *
 * The swagger UI files are pre-loaded in the binary as resources.
 *
 * @param swaggerDefinition The definition of the swagger-file to serve.
 * @param hideSwaggerValidator Whether the validator on the bottom should be hidden
 * @param hideSwaggerUrlField Whether the url field in the toolbar should be hidden.
 */
class SwaggerHandler(
    swaggerDefinition: SwaggerDefinition,
    private val swaggerYamlUrl: String = "./swagger.yml",
    private val hideSwaggerValidator: Boolean = false,
    private val hideSwaggerUrlField: Boolean = true
) {


    /**
     * The (lazily cached) [ByteArray] of the YAML file contents to serve.
     */
    private val swaggerYamlByteArray: ByteArray by lazy {
        YamlFileGenerator.generate(swaggerDefinition).toByteArray()
    }


    /**
     * Returns a [ByteArray] with the content of the file which should be served back for the given request defined by a relative [uri], and the respective mime-type for the served file.
     *
     * @param uri The relative uri to get the content for. This should be relative to the path the swagger should be
     * served on, e.g. if the swagger ui should be served at `https://some.example.org/v1/swagger/`, then the respective
     * uri passed for the pages should be starting after the `/swagger/`, so for the main page e.g. be blank
     * (or index.html).
     * @return A [Pair]<[String], [ByteArray]> which contains the content to serve back for the given GET request and the respective MimeType, or null if no content
     * was found to serve for the request and a 404 should be served respectively.
     */
    fun get(uri: String): Pair<String, ByteArray>? {
        return when (uri) {
            "swagger.yml" -> swaggerYamlByteArray
            "index.html" -> getIndexFile()
            else -> getUI(uri)
        }?.let { byteArray -> Pair(uri.getMimeType(), byteArray) }
    }

    private fun getIndexFile(): ByteArray? {
        return getUI("index.html")?.toString(Charset.defaultCharset())
            ?.replace(SWAGGER_URL_TO_REPLACE, swaggerYamlUrl)
            ?.let { html ->
                // add display:none css attribute to the form if the url in toolbar should not be editable / displayed.
                if (hideSwaggerUrlField) {
                    html.replace("</style>", " $SWAGGER_STYLE_CODE_TO_HIDE_URL_FIELD</style>")
                } else {
                    html
                }
            }
            ?.let { html ->
                if (hideSwaggerValidator) {
                    html.replace("layout:", "validatorUrl: null, layout:")
                } else {
                    html
                }
            }
            ?.toByteArray()
    }

    private fun getUI(uri: String): ByteArray? {
        return try {
            val classloader = SwaggerHandler::class.java.classLoader
            classloader.getResourceAsStream("swagger-ui/$uri")?.readBytes()
        } catch (throwable: Throwable) {
            null
        }
    }

    private fun String.getMimeType(): String {
        return when (this.substringAfterLast(".", "")) {
            "html" -> "text/html; charset=UTF-8"
            "css" -> "text/css"
            "png" -> "image/png"
            "js" -> "application/x-javascript"
            "yml" -> "application/x-yaml"
            else -> "application/octet-stream"
        }
    }


    private companion object {

        /**
         * The url in the swagger UI to replace with the actual URL of the swagger definition file.
         */
        private const val SWAGGER_URL_TO_REPLACE = "https://petstore.swagger.io/v2/swagger.json"

        private const val SWAGGER_STYLE_CODE_TO_HIDE_URL_FIELD = ".download-url-wrapper { display: none !important; }"
    }

}