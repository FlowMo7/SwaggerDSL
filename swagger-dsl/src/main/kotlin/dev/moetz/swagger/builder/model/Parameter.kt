package dev.moetz.swagger.builder.model

import dev.moetz.swagger.builder.SwaggerDsl
import dev.moetz.swagger.definition.SwaggerDefinition

sealed class Parameter {

    protected abstract val name: String
    protected abstract val parameterIn: String

    private var description: String? = null
    protected open var required: Boolean? = null
    private var schema: Schema? = null
    protected open var type: String? = null
    private var enum: List<String>? = null
    private var format: String? = null
    private var arrayItemsSchema: Schema? = null

    @SwaggerDsl
    fun description(description: String) {
        this.description = description
    }

    @SwaggerDsl
    fun required(required: Boolean) {
        this.required = required
    }

    @SwaggerDsl
    fun schema(schema: Schema) {
        this.schema = schema
        when (schema) {
            is ArraySchema -> type("array")
            is TypeSchema -> schema.type.takeIf { it != "object" }?.let { type(it) }
            is ReferencedSchema -> schema.type.takeIf { it != "object" }?.let { type(it) }
        }
    }

    @SwaggerDsl
    fun type(type: String) {
        if (this !is FormDataParameter) {
            val allowedTypes = listOf("array", "boolean", "integer", "number", "object", "string")
            if (allowedTypes.contains(type).not()) {
                throw IllegalArgumentException("Type $type not allowed. Must be one of the allowed values: ${allowedTypes.joinToString()}")
            }
        }
        this.type = type
    }

    @SwaggerDsl
    fun enum(vararg enum: String) {
        this.enum = enum.toList()
    }


    /**
     * Set the format and item type of array parameter.
     * **See Also:** [Documentation](https://swagger.io/docs/specification/2-0/describing-parameters/#array)
     *
     * @param format The format of the parameter.
     * @param itemsSchema The [Schema] of the items in this array
     */
    @SwaggerDsl
    fun array(format: String, itemsSchema: Schema) {
        this.type = null
        this.format = format
        this.arrayItemsSchema = itemsSchema
        val allowedTypes = listOf("csv", "ssv", "tsv", "pipes", "multi")
        if (allowedTypes.contains(format).not()) {
            throw IllegalArgumentException("Format $format not allowed. Must be one of the allowed values: ${allowedTypes.joinToString()}")
        }
        if (format == "multi" && (this !is QueryParameter || this !is FormDataParameter)) {
            throw IllegalArgumentException("Type $format only allowed on query or form data parameters")
        }
    }


    val definition: SwaggerDefinition.Path.ParameterDefinition
        get() {
            if (this !is BodyParameter && arrayItemsSchema == null && type == null) {
                throw IllegalArgumentException("Parameters must have a type declared (all but the body parameter). ${this.name} is missing a type.")
            }
            return SwaggerDefinition.Path.ParameterDefinition(
                name = name,
                `in` = parameterIn,
                description = description,
                required = required,
                type = type,
                enum = enum,
                schema = schema?.definition,
                format = format,
                arrayItemsSchema = arrayItemsSchema?.definition
            )
        }
}


class PathParameter @PublishedApi internal constructor(
    override val name: String,
    override var type: String?
) : Parameter() {

    init {
        //validation is done in the DSL setter, so we invoke it here
        type?.let { type(it) }
    }

    override val parameterIn: String
        get() = "path"

    override var required: Boolean? = true

}


class QueryParameter @PublishedApi internal constructor(override val name: String) : Parameter() {
    override val parameterIn: String get() = "query"
}

class BodyParameter @PublishedApi internal constructor(override val name: String) : Parameter() {
    override val parameterIn: String get() = "body"
}

class FormDataParameter @PublishedApi internal constructor(override val name: String) : Parameter() {
    override val parameterIn: String get() = "formData"
}

class HeaderParameter @PublishedApi internal constructor(override val name: String) : Parameter() {
    override val parameterIn: String get() = "header"
}