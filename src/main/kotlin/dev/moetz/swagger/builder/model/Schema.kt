package dev.moetz.swagger.builder.model

import dev.moetz.swagger.builder.SwaggerDsl
import dev.moetz.swagger.definition.SwaggerDefinition


/**
 * Sealed class for any schema in swagger, which are:
 * * [ObjectSchema] for objects
 * * [ArraySchema] for arrays
 * * [TypeSchema] for types
 * * [ReferencedSchema] for any of the above schemas as references
 */
sealed class Schema {

    internal abstract val type: String

    /**
     * The [SwaggerDefinition.Path.SchemaDefinition] representation of the given [Schema].
     */
    abstract val definition: SwaggerDefinition.Path.SchemaDefinition

    /**
     * The (optional) name of the schema.
     */
    abstract val name: String?

    protected var required: Boolean? = null
        private set

    /**
     * Set whether the schema is required.
     *
     * If this is not called, there will be no explicit mark whether the schema is required or not in the definition.
     *
     * @param required whether the schema should be set to required, or explicitly not required.
     */
    @SwaggerDsl
    fun required(required: Boolean = true) {
        this.required = required
    }


    protected var description: String? = null

    /**
     * Set a description for the [Schema].
     *
     * @param description The description of this schema.
     */
    @SwaggerDsl
    open fun description(description: String) {
        this.description = description
    }

}


/**
 * DSL class for an object to represent in the swagger.
 *
 * @param name An optional name of the object.
 */
class ObjectSchema
@PublishedApi internal constructor(
    override val name: String?
) : Schema() {

    override val type: String
        get() = "object"


    private val properties: MutableList<Pair<String, Schema>> = mutableListOf()

    /**
     * Add a property to the object schema.
     *
     * @param name The name of the property to add to the object.
     * @param schema The [Schema] definition of the property.
     */
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

/**
 * DSL class for an array to represent in the swagger.
 *
 * @param name An optional name of the object.
 */
class ArraySchema
@PublishedApi internal constructor(
    override val name: String?
) : Schema() {

    override val type: String
        get() = "array"


    private var itemsSchema: Schema? = null

    /**
     * Set the [Schema] of the items in this array.
     *
     * @param itemsSchema The [Schema] of the items in this array.
     */
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

    private var example: String? = null
    private var format: String? = null
    private var enum: List<String>? = null


    /**
     * Set the example value of this element.
     *
     * @param example The example value of this element.
     */
    @SwaggerDsl
    fun example(example: String) {
        this.example = example
    }

    /**
     * Set the format of this element.
     *
     * @param format The format to set.
     */
    @SwaggerDsl
    fun format(format: String) {
        this.format = format
    }

    /**
     * Set enum values for this element.
     *
     * @param enum The possible values of this element.
     */
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


/**
 * A schema definition as a reference to a real schema definition.
 *
 * This one only holds the name and type as reference to the original schema, without the definition itself.
 *
 * @param name The name of the schema, actually needs to be non-null here (will throw an [IllegalArgumentException] otherwise)
 * @param type The type ot the referenced schema.
 */
class ReferencedSchema
@PublishedApi internal constructor(
    override val name: String?,
    override val type: String
) : Schema() {

    override fun description(description: String) {
        throw IllegalStateException("Changing the description in a ReferencedSchema will not have any impact.")
    }


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