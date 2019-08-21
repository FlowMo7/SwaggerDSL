package dev.moetz.swagger.builder

import dev.moetz.swagger.builder.model.*
import dev.moetz.swagger.definition.SwaggerDefinition

class SwaggerBuilder
@PublishedApi internal constructor(
    private val swaggerFileVersion: String = "2.0"
) {

    @PublishedApi
    internal val referencedSchemas: MutableMap<String, Schema> = mutableMapOf()

    /**
     * Creates a new object schema (specified using the given [receiver] lambda), and adds it to the internal list of
     * schemas if a name is given and later referenced by just calling [getNamedSchema].
     *
     * @param name An optional name of the given schema. If set, this schema will be added to the internal list of
     * schemas and later referenced by just calling [getNamedSchema].
     * @param receiver The lambda to add specifications for this schema.
     *
     * @return The created [Schema]. If a [name] is given, this will be an instance of [ReferencedSchema],
     * otherwise this will be an instance of [ObjectSchema].
     */
    inline fun createObjectSchema(name: String? = null, receiver: ObjectSchema.() -> Unit): Schema =
        ObjectSchema(name = name).also(receiver).handleAndReturnReferencedIfNamed(this)

    /**
     * Creates a new array schema (specified using the given [receiver] lambda), and adds it to the internal list of
     * schemas if a name is given and later referenced by just calling [getNamedSchema].
     *
     * @param name An optional name of the given schema. If set, this schema will be added to the internal list of
     * schemas and later referenced by just calling [getNamedSchema].
     * @param receiver The lambda to add specifications for this schema.
     *
     * @return The created [Schema]. If a [name] is given, this will be an instance of [ArraySchema],
     * otherwise this will be an instance of [ObjectSchema].
     */
    inline fun createArraySchema(name: String? = null, receiver: ArraySchema.() -> Unit): Schema =
        ArraySchema(name = name).also(receiver).handleAndReturnReferencedIfNamed(this)

    /**
     * Creates a new type schema (specified using the given [receiver] lambda), and adds it to the internal list of
     * schemas if a name is given and later referenced by just calling [getNamedSchema].
     *
     * @param type The type of this type-schema.
     * @param name An optional name of the given schema. If set, this schema will be added to the internal list of
     * schemas and later referenced by just calling [getNamedSchema].
     * @param receiver The lambda to add specifications for this schema.
     *
     * @return The created [Schema]. If a [name] is given, this will be an instance of [TypeSchema],
     * otherwise this will be an instance of [ObjectSchema].
     */
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

    /**
     * @return a [ReferencedSchema] of any schema added before using [createObjectSchema], [createArraySchema] or
     * [createTypeSchema] with a name as parameter.
     */
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
    @SwaggerDsl
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
        @SwaggerDsl
        fun description(description: String) {
            this.description = description
        }

        /**
         * Defines a version for the given swagger definition.
         */
        @SwaggerDsl
        fun version(version: String) {
            this.version = version
        }

        /**
         * Adds a title for the swagger definition.
         */
        @SwaggerDsl
        fun title(title: String) {
            this.title = title
        }

        /**
         * Defines the host for the given swagger definition.
         */
        @SwaggerDsl
        fun host(host: String) {
            this.host = host
        }

        /**
         * Adds a base-path for the given swagger definition.
         */
        @SwaggerDsl
        fun basePath(basePath: String) {
            this.basePath = basePath
        }

        /**
         * Defines the schemes for the given swagger definition.
         */
        @SwaggerDsl
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
    @SwaggerDsl
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
    @SwaggerDsl
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
        private val consumes: MutableList<String> = mutableListOf()
        private var operationId: String? = null
        @PublishedApi
        internal val parameters: MutableList<Parameter> = mutableListOf()
        @PublishedApi
        internal val responses: MutableList<Response> = mutableListOf()
        private var deprecated: Boolean? = null


        @SwaggerDsl
        fun tags(vararg tag: String) {
            this.tags.addAll(tag.toList())
        }

        @SwaggerDsl
        fun summary(summary: String) {
            this.summary = summary
        }

        @SwaggerDsl
        fun description(description: String) {
            this.description = description
        }

        @SwaggerDsl
        fun produces(vararg produces: String) {
            this.produces.addAll(produces.toList())
        }

        @SwaggerDsl
        fun consumes(vararg consumes: String) {
            this.consumes.addAll(consumes.toList())
        }

        @SwaggerDsl
        fun operationId(operationId: String) {
            this.operationId = operationId
        }

        @SwaggerDsl
        inline fun response(status: Int, receiver: Response.() -> Unit) {
            this.responses.add(Response(status).also(receiver))
        }

        @SwaggerDsl
        inline fun pathParameter(name: String, type: String, receiver: PathParameter.() -> Unit) {
            this.parameters.add(PathParameter(name, type).also(receiver))
        }

        @SwaggerDsl
        inline fun queryParameter(name: String, receiver: QueryParameter.() -> Unit) {
            this.parameters.add(QueryParameter(name).also(receiver))
        }

        @SwaggerDsl
        inline fun bodyParameter(name: String, receiver: BodyParameter.() -> Unit) {
            this.parameters.add(BodyParameter(name).also(receiver))
        }

        @SwaggerDsl
        inline fun headerParameter(name: String, receiver: HeaderParameter.() -> Unit) {
            this.parameters.add(HeaderParameter(name).also(receiver))
        }

        @SwaggerDsl
        fun deprecated() {
            this.deprecated = true
        }


        class Response @PublishedApi internal constructor(private val status: Int) {

            private var description: String? = null

            @SwaggerDsl
            fun description(description: String) {
                this.description = description
            }

            private var schema: Schema? = null

            @SwaggerDsl
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
                consumes = consumes.toList(),
                operationId = operationId,
                parameters = parameters.map { parameter -> parameter.definition },
                responses = responses.map { response -> response.definition },
                deprecated = deprecated
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