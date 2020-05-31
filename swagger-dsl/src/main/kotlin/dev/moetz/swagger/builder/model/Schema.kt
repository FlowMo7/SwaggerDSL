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

    /**
     * Validates the schema and throws a [ValidationException] if the validation failed. Will return if the validation succeeded.
     *
     * @throws ValidationException
     */
    @Throws(ValidationException::class)
    @PublishedApi
    internal abstract fun validate()

    class ValidationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
        override val message: String?
            get() = super.message + schemaStack.joinToString()

        @PublishedApi
        internal fun fillSchemaStack(type: String, name: String? = null): ValidationException {
            schemaStack.add(type + if (name != null) " $name" else null)
            return this
        }

        private val schemaStack: MutableList<String> = mutableListOf()
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

    @PublishedApi
    override fun validate() {
        properties.forEach { (propertyName, schema) ->
            try {
                schema.validate()
            } catch (validationException: ValidationException) {
                throw ValidationException(
                    "Validation error in ObjectSchema '$name', property $propertyName: ${validationException.message}",
                    validationException
                )
            }
        }
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

    @PublishedApi
    override fun validate() {
        if (itemsSchema == null) {
            throw ValidationException("No item schema defined for ArraySchema. Add an item-schema using 'items(itemsSchema: Schema)'.")
        }
        try {
            itemsSchema?.validate()
        } catch (validationException: ValidationException) {
            throw ValidationException(
                "Validation error in items-schema of ArraySchema '$name': ${validationException.message}",
                validationException
            )
        }
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

    //number & integer parameters
    private var minimum: Pair<Number, Boolean>? = null
    private var maximum: Pair<Number, Boolean>? = null
    private var multipleOf: Number? = null

    //string parameters
    private var pattern: String? = null
    private var minLength: Int? = null
    private var maxLength: Int? = null


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
     * Set the [minimum] value allowed for this field.
     *
     * **This is only valid for '`integer`' and '`number`' types.**
     *
     * @param minimum The minimum value for this field
     * @param exclusive Whether the given [minimum] value is inclusive or exclusive. Default is false, which means
     * inclusive the given value.
     */
    @SwaggerDsl
    fun minimum(minimum: Number, exclusive: Boolean = false) {
        this.minimum = minimum to exclusive
    }

    /**
     * Define the [maximum] value allowed for this field.
     *
     * **This is only valid for '`integer`' and '`number`' types.**
     *
     * @param maximum The maximum value for this field
     * @param exclusive Whether the given [maximum] value is inclusive or exclusive. Default is false, which means
     * inclusive the given value.
     */
    @SwaggerDsl
    fun maximum(maximum: Number, exclusive: Boolean = false) {
        this.maximum = maximum to exclusive
    }

    /**
     * Define that the field only accepts multiples of the given value.
     *
     * **This is only valid for '`integer`' and '`number`' types.**
     *
     * @param multipleOf The number the given field is allowed to be multiples of.
     */
    @SwaggerDsl
    fun multipleOf(multipleOf: Number) {
        this.multipleOf = multipleOf
    }


    /**
     * Define the minimum string length of this field.
     *
     * **This is only valid for fields with type '`string`'.**
     *
     * @param minLength The minimum length of the field (inclusive).
     */
    @SwaggerDsl
    fun minLength(minLength: Int) {
        this.minLength = minLength
    }

    /**
     * Define the maximum string length of this field.
     *
     * **This is only valid for fields with type '`string`'.**
     *
     * @param maxLength The maximum length of the field (inclusive).
     */
    @SwaggerDsl
    fun maxLength(maxLength: Int) {
        this.maxLength = maxLength
    }

    /**
     * Define a pattern for this field.
     *
     * **This is only valid for fields with type '`string`'.**
     *
     * @param pattern The regex-pattern this field need to honor.
     */
    @SwaggerDsl
    fun pattern(pattern: String) {
        this.pattern = pattern
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


    @PublishedApi
    override fun validate() {
        //https://swagger.io/docs/specification/data-models/data-types/ (+ array, + object)
        val validDataTypes = listOf("string", "number", "integer", "boolean")

        if (validDataTypes.contains(type).not()) {
            throw ValidationException("Type '$type' is not valid. Valid types are: ${validDataTypes.joinToString { "'$it'" }} (plus 'array' and 'object', but those have their own DSL method). See https://swagger.io/docs/specification/data-models/data-types/ for more details.")
        }

        when (type) {
            "number" -> {
                if ((format == null || format == "float" || format == "double").not()) {
                    throw ValidationException("Type 'number' only allows the format to be either not set, 'float' or 'double'. See https://swagger.io/docs/specification/data-models/data-types/ for more details.")
                }
            }
            "integer" -> {
                if ((format == null || format == "int32" || format == "int64").not()) {
                    throw ValidationException("Type 'integer' only allows the format to be either not set, 'int32' or 'int64'. See https://swagger.io/docs/specification/data-models/data-types/ for more details.")
                }
            }
        }

        if (type != "string" && pattern != null) {
            throw ValidationException("Only 'string' types are allowed to specify a pattern. There is one defined (with type $type). See https://swagger.io/docs/specification/data-models/data-types/ for more details.")
        }

        if (type != "string" && (minLength != null || maxLength != null)) {
            throw ValidationException("Only 'string' types are allowed to specify 'minLength' or 'maxLength'. There is one defined (with type $type). See https://swagger.io/docs/specification/data-models/data-types/ for more details.")
        }


        if (type != "number" && type != "integer") {
            if (multipleOf != null) {
                throw ValidationException("Only 'integer' and 'number' types are allowed to define 'multipleOf', which is defined (with type $type). See https://swagger.io/docs/specification/data-models/data-types/ for more details.")
            }

            if ((minimum != null || maximum != null)) {
                throw ValidationException("'minimum' or 'maximum' are only allowed on types 'number' and 'integer', one is set (with type $type). See https://swagger.io/docs/specification/data-models/data-types/ for more details.")
            }
        }
    }

    override val definition: SwaggerDefinition.Path.SchemaDefinition
        get() = SwaggerDefinition.Path.SchemaDefinition.TypeSchemaDefinition(
            type = type,
            description = description,
            required = required,
            example = example,
            format = format,
            enum = enum,
            minimum = minimum,
            maximum = maximum,
            multipleOf = multipleOf,
            minLength = minLength,
            maxLength = maxLength,
            pattern = pattern
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

    @PublishedApi
    override fun validate() {
        //no validation performed here
    }

    override val definition: SwaggerDefinition.Path.SchemaDefinition
        get() = SwaggerDefinition.Path.SchemaDefinition.ReferencedSchemaDefinition(
            name = name,
            type = type,
            description = "",
            required = null
        )

}