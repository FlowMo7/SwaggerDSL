package dev.moetz.swagger.builder.model

import dev.moetz.swagger.definition.SwaggerDefinition


sealed class Schema {

    protected abstract val type: String
    abstract val definition: SwaggerDefinition.Path.SchemaDefinition

    companion object {

        inline fun createObject(receiver: ObjectSchema.() -> Unit): Schema = ObjectSchema().also(receiver)

        inline fun createArray(receiver: ArraySchema.() -> Unit): Schema = ArraySchema().also(receiver)

        inline fun createType(type: String, receiver: TypeSchema.() -> Unit): Schema = TypeSchema(type).also(receiver)

    }


    protected var required: Boolean? = null
        private set

    fun required(required: Boolean = true) {
        this.required = required
    }

}


class ObjectSchema @PublishedApi internal constructor() : Schema() {

    override val type: String
        get() = "object"


    private val properties: MutableList<Pair<String, Schema>> = mutableListOf()

    private var description: String? = null

    fun description(description: String) {
        this.description = description
    }

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

class ArraySchema @PublishedApi internal constructor() : Schema() {

    override val type: String
        get() = "array"


    private var description: String? = null

    fun description(description: String) {
        this.description = description
    }


    private var itemsSchema: Schema? = null

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

class TypeSchema @PublishedApi internal constructor(override val type: String) : Schema() {

    private var description: String? = null
    private var example: String? = null
    private var format: String? = null
    private var enum: List<String>? = null


    fun description(description: String) {
        this.description = description
    }

    fun example(example: String) {
        this.example = example
    }

    fun format(format: String) {
        this.format = format
    }

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