package dev.moetz.swagger.builder.model

import dev.moetz.swagger.definition.SwaggerDefinition

sealed class Parameter {

    protected abstract val name: String
    protected abstract val parameterIn: String

    private var description: String? = null
    private var default: String? = null
    protected open var required: Boolean? = null
    private var schema: Schema? = null
    protected open var type: String? = null
    private var enum: List<String>? = null

    fun description(description: String) {
        this.description = description
    }

    fun default(default: String) {
        this.default = default
    }

    fun required(required: Boolean) {
        this.required = required
    }

    fun schema(schema: Schema) {
        this.schema = schema
    }

    fun type(type: String) {
        this.type = type
    }

    fun enum(vararg enum: String) {
        this.enum = enum.toList()
    }


    val definition: SwaggerDefinition.Path.ParameterDefinition
        get() = SwaggerDefinition.Path.ParameterDefinition(
            name = name,
            `in` = parameterIn,
            description = description,
            default = default,
            required = required,
            type = type,
            enum = enum,
            schema = schema?.definition
        )

}


class PathParameter @PublishedApi internal constructor(
    override val name: String,
    override var type: String?
) : Parameter() {

    override val parameterIn: String
        get() = "path"

    override var required: Boolean? = true

}


class QueryParameter @PublishedApi internal constructor(override val name: String) : Parameter() {

    override val parameterIn: String
        get() = "query"

}