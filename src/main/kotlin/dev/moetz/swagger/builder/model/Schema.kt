package dev.moetz.swagger.builder.model

import dev.moetz.swagger.builder.SwaggerDsl
import dev.moetz.swagger.definition.SwaggerDefinition


sealed class Schema {

    internal abstract val type: String
    abstract val definition: SwaggerDefinition.Path.SchemaDefinition
    abstract val name: String?

    protected var required: Boolean? = null
        private set

    @SwaggerDsl
    fun required(required: Boolean = true) {
        this.required = required
    }

}

class ObjectSchema
@PublishedApi internal constructor(
    override val name: String?
) : Schema() {

    override val type: String
        get() = "object"


    private val properties: MutableList<Pair<String, Schema>> = mutableListOf()

    private var description: String? = null

    @SwaggerDsl
    fun description(description: String) {
        this.description = description
    }

    @SwaggerDsl
    fun property(name: String, schema: Schema) {
        if (properties.any { it.first == name }) {
            throw IllegalArgumentException("Property with name '$name' already set")
        }
        properties.add(Pair(name, schema))
    }


    override val definition: SwaggerDefinition.Path.SchemaDefinition
        get() = SwaggerDefinition.Path.SchemaDefinition.ObjectSchemaDefinition(
            type = type,
            description = description,
            required = required,
            properties = properties.map { (name, schema) -> Pair(name, schema.definition) }
        )

}

class ArraySchema
@PublishedApi internal constructor(
    override val name: String?
) : Schema() {

    override val type: String
        get() = "array"


    private var description: String? = null

    @SwaggerDsl
    fun description(description: String) {
        this.description = description
    }


    private var itemsSchema: Schema? = null

    @SwaggerDsl
    fun items(itemsSchema: Schema) {
        this.itemsSchema = itemsSchema
    }


    override val definition: SwaggerDefinition.Path.SchemaDefinition
        get() = SwaggerDefinition.Path.SchemaDefinition.ArraySchemaDefinition(
            type = type,
            description = description,
            required = required,
            itemsSchema = itemsSchema?.definition
        )

}

class TypeSchema
@PublishedApi internal constructor(
    override val type: String,
    override val name: String?
) : Schema() {

    private var description: String? = null
    private var example: String? = null
    private var format: String? = null
    private var enum: List<String>? = null


    @SwaggerDsl
    fun description(description: String) {
        this.description = description
    }

    @SwaggerDsl
    fun example(example: String) {
        this.example = example
    }

    @SwaggerDsl
    fun format(format: String) {
        this.format = format
    }

    @SwaggerDsl
    fun enum(vararg enum: String) {
        this.enum = enum.toList()
    }


    override val definition: SwaggerDefinition.Path.SchemaDefinition
        get() = SwaggerDefinition.Path.SchemaDefinition.TypeSchemaDefinition(
            type = type,
            description = description,
            required = required,
            example = example,
            format = format,
            enum = enum
        )

}


class ReferencedSchema
@PublishedApi internal constructor(
    override val name: String?,
    override val type: String
) : Schema() {


    @PublishedApi
    internal constructor(schema: Schema) : this(
        name = schema.name ?: throw IllegalArgumentException("name needs to be set in referenced schemas"),
        type = schema.type
    )

    override val definition: SwaggerDefinition.Path.SchemaDefinition
        get() = SwaggerDefinition.Path.SchemaDefinition.ReferencedSchemaDefinition(
            name = name,
            type = type,
            description = "",
            required = null
        )

}