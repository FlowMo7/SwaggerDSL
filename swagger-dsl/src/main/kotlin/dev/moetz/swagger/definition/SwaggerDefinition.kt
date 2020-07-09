package dev.moetz.swagger.definition

import dev.moetz.swagger.builder.model.ArraySchema


data class SwaggerDefinition(
    val swaggerFileVersion: String,
    val info: Info?,
    val tags: List<Tag>,
    val paths: List<Path>,
    val definitions: Map<String, Path.SchemaDefinition>
) {
    data class Info(
        val description: String?,
        val version: String?,
        val title: String?,
        val host: String?,
        val basePath: String?,
        val schemes: List<String>
    )

    data class Tag(
        val name: String,
        val description: String
    )

    data class Path(
        val path: String,
        val method: String,
        val tags: List<String>,
        val summary: String?,
        val description: String?,
        val produces: List<String>,
        val consumes: List<String>,
        val operationId: String?,
        val parameters: List<ParameterDefinition>,
        val responses: List<ResponseDefinition>,
        val deprecated: Boolean?
    ) {

        data class ParameterDefinition(
            val name: String,
            val `in`: String,
            val description: String?,
            val required: Boolean?,
            val type: String?,
            val enum: List<String>?,
            val schema: SchemaDefinition?,
            val format: String?,
            val arrayItemsSchema: SchemaDefinition?
        )

        data class ResponseDefinition(
            val status: Int,
            val description: String?,
            val schema: SchemaDefinition?
        )

        sealed class SchemaDefinition {
            abstract val type: String
            abstract val description: String?
            abstract val required: Boolean?

            data class ObjectSchemaDefinition(
                override val type: String,
                override val description: String?,
                override val required: Boolean?,
                val properties: List<Pair<String, SchemaDefinition>>
            ) : SchemaDefinition()


            data class ArraySchemaDefinition(
                override val type: String,
                override val description: String?,
                override val required: Boolean?,
                val itemsSchema: SchemaDefinition?
            ) : SchemaDefinition()


            data class TypeSchemaDefinition(
                override val type: String,
                override val description: String?,
                override val required: Boolean?,
                val example: String?,
                val format: String?,
                val enum: List<String>?,
                val minimum: Pair<Number, Boolean>?,
                val maximum: Pair<Number, Boolean>?,
                val multipleOf: Number?,
                val minLength: Int?,
                val maxLength: Int?,
                val pattern: String?
            ) : SchemaDefinition()

            data class ReferencedSchemaDefinition(
                val name: String?,
                override val type: String,
                override val description: String?,
                override val required: Boolean?
            ) : SchemaDefinition()
        }
    }
}