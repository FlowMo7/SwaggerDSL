package dev.moetz.swagger.builder

import dev.moetz.swagger.builder.model.*
import dev.moetz.swagger.definition.SwaggerDefinition

class SwaggerBuilder
@PublishedApi internal constructor(
    private val swaggerFileVersion: String = "2.0"
) {

    @PublishedApi
    internal val referencedSchemas: MutableMap<String, Schema> = mutableMapOf()

    inline fun createObjectSchema(name: String? = null, receiver: ObjectSchema.() -> Unit): Schema =
        ObjectSchema(name = name).also(receiver).handleAndReturnReferencedIfNamed(this)

    inline fun createArraySchema(name: String? = null, receiver: ArraySchema.() -> Unit): Schema =
        ArraySchema(name = name).also(receiver).handleAndReturnReferencedIfNamed(this)

    inline fun createTypeSchema(
        type: String,
        name: String? = null,
        receiver: TypeSchema.() -> Unit
    ): Schema =
        TypeSchema(type = type, name = name).also(receiver).handleAndReturnReferencedIfNamed(this)

    @PublishedApi
    internal fun Schema.handleAndReturnReferencedIfNamed(swaggerBuilder: SwaggerBuilder): Schema {
        val schemaName = this.name
        return if (schemaName != null) {
            if (swaggerBuilder.referencedSchemas.containsKey(schemaName)) {
                throw IllegalArgumentException("Schema with name '$name' already defined")
            }
            swaggerBuilder.referencedSchemas[schemaName] = this
            ReferencedSchema(this)
        } else {
            this
        }
    }

    fun getNamedSchema(name: String): Schema {
        val schema =
            referencedSchemas[name] ?: throw IllegalArgumentException("Could not find named schema '$name'")
        return ReferencedSchema(schema)
    }


    companion object {

        /**
         * Builder method to generate a new [SwaggerDefinition].
         */
        inline fun generate(
            receiver: SwaggerBuilder.() -> Unit
        ): SwaggerDefinition {
            val builder = SwaggerBuilder()
            receiver.invoke(builder)
            return builder.definition
        }

    }

    @PublishedApi
    internal var info: Info? = null

    /**
     * Add general Information to the swagger definition.
     *
     * See [SwaggerBuilder.Info] for more information.
     */
    inline fun info(receiver: Info.() -> Unit) {
        this.info = Info().also(receiver)
    }


    /**
     * Builder class for the general swagger infos.
     */
    class Info @PublishedApi internal constructor() {
        private var description: String? = null
        private var version: String? = null
        private var title: String? = null
        private var host: String? = null
        private var basePath: String? = null
        private var schemes: List<String>? = null

        /**
         * Adds a general description to the swagger information (may be multi-line).
         */
        fun description(description: String) {
            this.description = description
        }

        /**
         * Defines a version for the given swagger definition.
         */
        fun version(version: String) {
            this.version = version
        }

        /**
         * Adds a title for the swagger definition.
         */
        fun title(title: String) {
            this.title = title
        }

        /**
         * Defines the host for the given swagger definition.
         */
        fun host(host: String) {
            this.host = host
        }

        /**
         * Adds a base-path for the given swagger definition.
         */
        fun basePath(basePath: String) {
            this.basePath = basePath
        }

        /**
         * Defines the schemes for the given swagger definition.
         */
        fun schemes(vararg schemes: String) {
            this.schemes = schemes.toList()
        }


        /**
         * Exposes the immutable definition object for this [SwaggerBuilder.Info] instance.
         *
         * Generally you should not need to access this, this is exposed for swagger file generators like the `YamlFileGenerator`.
         */
        val definition: SwaggerDefinition.Info
            get() = SwaggerDefinition.Info(
                description = description,
                version = version,
                title = title,
                host = host,
                basePath = basePath,
                schemes = schemes.orEmpty()
            )

    }


    private val tags: MutableList<Tag> = mutableListOf()

    /**
     * Add a tag-definition to the swagger.
     *
     * @param name The name / identifier of the tag
     * @param description The description of this tag (may be multiline).
     * @throws IllegalArgumentException if the tag with the given name is already set.
     */
    fun tag(name: String, description: String) {
        if (tags.any { tag -> tag.name.equals(name, ignoreCase = true) }) {
            throw IllegalArgumentException("tag with name '$name' already set.")
        }
        tags.add(Tag(name, description))
    }

    private data class Tag constructor(
        internal val name: String,
        internal val description: String
    )


    @PublishedApi
    internal val paths: MutableList<Path> = mutableListOf()


    /**
     * Add a new path definition with the given [path] and [method].
     *
     * @param path The (relative) path for this path-definition.
     * @param method The method for this path-definition.
     * @throws IllegalArgumentException if a path-definition with the given path and method is already defined
     */
    inline fun path(path: String, method: String, receiver: Path.() -> Unit) {
        if (paths.any { it.path == path && it.method == method }) {
            throw IllegalArgumentException("Path-definition with path '$path' and method '$method' already set")
        }
        this.paths.add(Path(path, method).also(receiver))
    }

    class Path @PublishedApi internal constructor(
        @PublishedApi internal val path: String,
        @PublishedApi internal val method: String
    ) {

        private val tags: MutableList<String> = mutableListOf()
        private var summary: String? = null
        private var description: String? = null
        private val produces: MutableList<String> = mutableListOf()
        private var operationId: String? = null
        @PublishedApi
        internal val parameters: MutableList<Parameter> = mutableListOf()
        @PublishedApi
        internal val responses: MutableList<Response> = mutableListOf()


        fun tags(vararg tag: String) {
            this.tags.addAll(tag.toList())
        }

        fun summary(summary: String) {
            this.summary = summary
        }

        fun description(description: String) {
            this.description = description
        }

        fun produces(vararg produces: String) {
            this.produces.addAll(produces.toList())
        }

        fun operationId(operationId: String) {
            this.operationId = operationId
        }

        inline fun response(status: Int, receiver: Response.() -> Unit) {
            this.responses.add(Response(status).also(receiver))
        }

        inline fun pathParameter(name: String, type: String, receiver: PathParameter.() -> Unit) {
            this.parameters.add(PathParameter(name, type).also(receiver))
        }

        inline fun queryParameter(name: String, receiver: QueryParameter.() -> Unit) {
            this.parameters.add(QueryParameter(name).also(receiver))
        }


        class Response @PublishedApi internal constructor(private val status: Int) {

            private var description: String? = null

            fun description(description: String) {
                this.description = description
            }

            private var schema: Schema? = null

            fun schema(schema: Schema) {
                this.schema = schema
            }

            internal val definition: SwaggerDefinition.Path.ResponseDefinition
                get() = SwaggerDefinition.Path.ResponseDefinition(
                    status = status,
                    description = description,
                    schema = schema?.definition
                )
        }


        val definition: SwaggerDefinition.Path
            get() = SwaggerDefinition.Path(
                path = path,
                method = method,
                tags = tags.toList(),
                summary = summary,
                description = description,
                produces = produces.toList(),
                operationId = operationId,
                parameters = parameters.map { parameter -> parameter.definition },
                responses = responses.map { response -> response.definition }
            )

    }

    /**
     * Exposes the immutable definition object for this builder instance.
     *
     * Generally you should not need to access this, this is exposed for swagger file generators like the `YamlFileGenerator`.
     */
    val definition: SwaggerDefinition
        get() = SwaggerDefinition(
            swaggerFileVersion = swaggerFileVersion,
            info = info?.definition,
            tags = tags.map { tag -> SwaggerDefinition.Tag(tag.name, tag.description) },
            paths = paths.map { path -> path.definition },
            definitions = referencedSchemas.map { (name, schema) -> name to schema.definition }.toMap()
        )

}