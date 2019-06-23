package dev.moetz.swagger.definition


data class SwaggerDefinition(
    val swaggerFileVersion: String,
    val info: Info?,
    val tags: List<Tag>,
    val paths: List<Path>
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
        var summary: String?,
        val description: String?,
        val produces: List<String>,
        val operationId: String?,
        val parameters: List<ParameterDefinition>,
        val responses: List<ResponseDefinition>
    ) {

        data class ParameterDefinition(
            val name: String,
            val `in`: String,
            val description: String?,
            val default: String?,
            val required: Boolean?,
            val type: String?,
            val enum: List<String>?,
            val schema: SchemaDefinition?
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
                val enum: List<String>?
            ) : SchemaDefinition()
        }
    }
}