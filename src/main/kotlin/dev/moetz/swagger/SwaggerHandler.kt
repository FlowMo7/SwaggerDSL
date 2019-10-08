package dev.moetz.swagger

import dev.moetz.swagger.SwaggerHandler.Companion.SWAGGER_UI_GITHUB_ROOT
import dev.moetz.swagger.definition.SwaggerDefinition
import dev.moetz.swagger.generator.YamlFileGenerator
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.activation.MimetypesFileTypeMap


/**
 * A handler which returns the swagger ui file contents as well as the swaggerDefinition as a yaml file respectively
 * when calling [get] with a relative url for the swagger path handling.
 *
 * This fetches the swagger ui files from the github repository at [SWAGGER_UI_GITHUB_ROOT] and caches them within the
 * given cacheDirectory (and max cacheSizeInByte) for some time.
 *
 * @param swaggerDefinition The definition of the swagger-file to serve.
 * @param okHttpClient the OkHttpClient to use to fetch the swagger-ui to serve. This [OkHttpClient] is altered as a new builder to apply caching logic.
 * @param cacheDirectory The directory to cache the swagger-ui in.
 * @param cacheSizeInByte The maximum size of the cache to use for the swagger-ui within the cacheDirectory.
 * @param hideSwaggerUrlField Whether the url field in the toolbar should be hidden.
 */
class SwaggerHandler(
        swaggerDefinition: SwaggerDefinition,
        private val swaggerYamlUrl: String = "./swagger.yml",
        okHttpClient: OkHttpClient,
        cacheDirectory: File = File("./cache"),
        cacheSizeInByte: Long = 1 * 1024 * 1024L /* default: 1 MB*/,
        private val hideSwaggerUrlField: Boolean = true
) {

    init {
        cacheDirectory.mkdirs()
    }

    /**
     * The (lazily cached) [ByteArray] of the YAML file contents to serve.
     */
    private val swaggerYamlByteArray: ByteArray by lazy {
        YamlFileGenerator.generate(swaggerDefinition).toByteArray()
    }

    /**
     * The [OkHttpClient] to use for fetching the swagger-ui.
     * This [OkHttpClient] contains a cache and applies caching-headers to the response to enable caching.
     */
    private val cachedOkHttpClient: OkHttpClient = okHttpClient.newBuilder()
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                response
                        .newBuilder()
                        .header("Cache-Control", "max-age=${TimeUnit.DAYS.toSeconds(6 * 30)}")
                        .build()
            }
            .cache(Cache(cacheDirectory, cacheSizeInByte))
            .build()


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
                ?.toByteArray()
    }

    private fun getUI(uri: String): ByteArray? {
        return cachedOkHttpClient
                .newCall(Request.Builder().get().url(SWAGGER_UI_GITHUB_ROOT + uri).build())
                .execute().body()?.bytes()
    }

    private fun String.getMimeType(): String {
        return when (this.substringAfterLast(".", "")) {
            "html" -> "text/html; charset=UTF-8"
            "css" -> "text/css"
            "png" -> "image/png"
            "js" -> "application/x-javascript"
            "yml" -> "application/x-yaml"
            else -> MimetypesFileTypeMap().getContentType(this)
        }
    }


    companion object {
        /**
         * The root URL for the swagger UI to fetch.
         */
        private const val SWAGGER_UI_GITHUB_ROOT =
                "https://raw.githubusercontent.com/swagger-api/swagger-ui/master/dist/"

        /**
         * The url in the swagger UI to replace with the actual URL of the swagger definition file.
         */
        private const val SWAGGER_URL_TO_REPLACE = "https://petstore.swagger.io/v2/swagger.json"

        private const val SWAGGER_STYLE_CODE_TO_HIDE_URL_FIELD = ".download-url-wrapper { display: none !important; }"
    }

}