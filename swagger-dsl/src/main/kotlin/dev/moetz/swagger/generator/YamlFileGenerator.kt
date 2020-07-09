package dev.moetz.swagger.generator

import dev.moetz.swagger.definition.SwaggerDefinition
import dev.moetz.swagger.generator.YamlFileGenerator.generate


/**
 * A generator which converts a [SwaggerDefinition] into a YAML-file.
 * See [generate].
 */
object YamlFileGenerator {

    /**
     * The padding for the yaml file indentation.
     */
    private const val PADDING = "  "


    /**
     * Converts the given [definition] into a YAML file definition.
     *
     * @param definition The [SwaggerDefinition] to generate the YAML file of.
     * @return the contents of the YAML file as a string
     */
    fun generate(definition: SwaggerDefinition): String {
        val stringBuilder = StringBuilder("swagger: '${definition.swaggerFileVersion}'\n")

        definition.info?.getYamlLines()?.forEach {
            stringBuilder.append("$it\n")
        }

        definition.tags.getYamlLines().forEach {
            stringBuilder.append("$it\n")
        }

        if (definition.paths.isNotEmpty()) {
            stringBuilder.append("paths:\n")
            definition.paths
                    .groupBy { it.path }
                    .map { (_, pathsInGroup) -> pathsInGroup.toYamlLines() }
                    .forEach {
                        it.forEach {
                            stringBuilder.append("$PADDING$it\n")
                        }
                    }
        }

        if (definition.definitions.isNotEmpty()) {
            stringBuilder.append("definitions:\n")
            stringBuilder.append(
                    definition.definitions
                            .map { (name, referencedDefinition) -> name to referencedDefinition.toYamlLines() }
                            .joinToString(separator = "\n") { (name, yamlLines) ->
                                yamlLines.joinToString(prefix = "$PADDING$name:\n", separator = "\n") { "$PADDING$PADDING$it" }
                            }
            )
        }

        return stringBuilder.toString()
    }


    /**
     * Escapes the quotes (`"`) in the given string for a YAML string. (turns `"` into `\"`)
     * @return the given receiver (string) with escaped quotes
     */
    private fun String.escapeQuotes(): String = this.replace("\"", "\\\"")

    /**
     * @return a list of lines for the YAML definition which represents the info elements of the swagger specification.
     */
    private fun SwaggerDefinition.Info.getYamlLines(): List<String> {
        val list = mutableListOf("info:")

        description?.also { description ->
            list.addAll(descriptionToList(description).map { "$PADDING$it" })
        }
        version?.also { version ->
            list.add("${PADDING}version: '$version'")
        }
        title?.also { title ->
            list.add("${PADDING}title: ${title.escapeForYamlAndWrapInQuotes()}")
        }
        host?.also { host ->
            list.add("host: ${host.escapeForYamlAndWrapInQuotes()}")
        }
        basePath?.also { basePath ->
            list.add("basePath: ${basePath.escapeForYamlAndWrapInQuotes()}")
        }

        list.add("schemes:")
        list.addAll(schemes.map { "$PADDING- $it" })

        return list.toList()
    }

    private fun List<SwaggerDefinition.Tag>.getYamlLines(): List<String> {
        return if (this.isNotEmpty()) {
            val list = mutableListOf("tags:")
            list.addAll(
                    this.flatMap { tag ->
                        listOf(
                                "$PADDING- name: ${tag.name}"
                        ) + descriptionToList(tag.description).map { "$PADDING$PADDING$it" }
                    }
            )
            list.toList()
        } else {
            emptyList()
        }
    }

    private fun List<SwaggerDefinition.Path>.toYamlLines(): List<String> {
        val list = mutableListOf(this.first().path.escapeForYamlAndWrapInQuotes() + ":")

        this.forEach { pathDefinition ->
            list.add("$PADDING${pathDefinition.method}:")

            if (pathDefinition.tags.isNotEmpty()) {
                list.add("$PADDING${PADDING}tags:")
                pathDefinition.tags.forEach { tag ->
                    list.add("$PADDING$PADDING$PADDING- $tag")
                }
            }
            pathDefinition.summary?.also { summary ->
                list.add("$PADDING${PADDING}summary: ${summary.escapeForYamlAndWrapInQuotes()}")
            }
            pathDefinition.description?.also { description ->
                list.addAll(descriptionToList(description).map { "$PADDING$PADDING$it" })
            }
            pathDefinition.operationId?.also { operationId ->
                list.add("$PADDING${PADDING}operationId: ${operationId.escapeForYamlAndWrapInQuotes()}")
            }

            pathDefinition.deprecated?.also { deprecated ->
                list.add("$PADDING${PADDING}deprecated: $deprecated")
            }

            if (pathDefinition.produces.isNotEmpty()) {
                list.add("$PADDING${PADDING}produces:")
                list.addAll(pathDefinition.produces.map { produces ->
                    "$PADDING$PADDING$PADDING- $produces"
                })
            }

            if (pathDefinition.consumes.isNotEmpty()) {
                list.add("$PADDING${PADDING}consumes:")
                list.addAll(pathDefinition.consumes.map { consumes ->
                    "$PADDING$PADDING$PADDING- $consumes"
                })
            }

            if (pathDefinition.parameters.isNotEmpty()) {
                list.add("$PADDING${PADDING}parameters:")
                list.addAll(pathDefinition.parameters.flatMap { parameter ->
                    parameter.toYamlLines().map { "$PADDING$PADDING$PADDING$it" }
                })
            }

            if (pathDefinition.responses.isNotEmpty()) {
                list.add("$PADDING${PADDING}responses:")
                pathDefinition.responses.forEach { response ->
                    list.addAll(response.toYamlLines().map { "$PADDING$PADDING$PADDING$it" })
                }
            }
        }

        return list.toList()
    }

    private fun SwaggerDefinition.Path.ParameterDefinition.toYamlLines(): List<String> {
        val list = mutableListOf(
                "- name: $name",
                "${PADDING}in: ${`in`}"
        )

        description?.also { description ->
            list.addAll(descriptionToList(description).map { "$PADDING$it" })
        }
//        default?.also { default ->
//            list.add("${PADDING}default: ${default.escapeForYamlAndWrapInQuotes()}")
//        }
        required?.also { required ->
            list.add("${PADDING}required: $required")
        }
        type?.also { type ->
            list.add("${PADDING}type: $type")
        }
        enum?.also { enum ->
            list.add(
                    "${PADDING}enum: " + enum.joinToString(
                            prefix = "[",
                            postfix = "]",
                            separator = ","
                    ) { it.escapeForYamlAndWrapInQuotes() })
        }
        schema?.also { schema ->
            list.add("${PADDING}schema:")
            list.addAll(
                    schema.toYamlLines()
                            .map { "${PADDING}${PADDING}$it" }
            )
        }

        format?.also {
            list.add("${PADDING}collectionFormat: $format")
            list.addAll(
                requireNotNull(arrayItemsSchema) { "arrayItemsSchema is n" }.toYamlLines()
                    .map { "${PADDING}$it" }
            )
        }

        return list.toList()
    }

    private fun SwaggerDefinition.Path.SchemaDefinition.toYamlLines(): List<String> {
        return when (this) {
            is SwaggerDefinition.Path.SchemaDefinition.ObjectSchemaDefinition -> {
                val list = mutableListOf<String>()
                list.add("type: $type")

                description?.also { description ->
                    list.addAll(descriptionToList(description))
                }

                val requiredProperties = properties.filter { (_, schema) -> schema.required == true }
                if (requiredProperties.isNotEmpty()) {
                    list.add("required:")
                    requiredProperties.forEach { (name, _) ->
                        list.add("$PADDING- $name")
                    }
                }

                if (properties.isNotEmpty()) {
                    list.add("properties:")
                    properties.forEach { (name, schema) ->
                        list.add("$PADDING$name:")
                        list.addAll(schema.toYamlLines().map { "$PADDING$PADDING$it" })
                    }
                }

                list.toList()
            }
            is SwaggerDefinition.Path.SchemaDefinition.ArraySchemaDefinition -> {
                val list = mutableListOf<String>()
                list.add("type: $type")

                description?.also { description ->
                    list.addAll(descriptionToList(description))
                }

                itemsSchema?.also { item ->
                    list.add("items:")
                    list.addAll(item.toYamlLines().map { "$PADDING$it" })
                }

                return list.toList()
            }
            is SwaggerDefinition.Path.SchemaDefinition.TypeSchemaDefinition -> {
                val list = mutableListOf<String>()
                list.add("type: $type")

                description?.also { description ->
                    list.addAll(descriptionToList(description))
                }
                example?.also { example ->
                    list.addAll(exampleToList(example))
                }
                format?.also { format ->
                    list.add("format: ${format.escapeForYamlAndWrapInQuotes()}")
                }
                enum?.also { enum ->
                    list.add("enum:")
                    list.addAll(enum.map { enumValue ->
                        "$PADDING- $enumValue"
                    })
                }
                minimum?.also { (minimum, exclusive) ->
                    list.add("minimum: $minimum")
                    list.add("exclusiveMinimum: $exclusive")
                }
                maximum?.also { (maximum, exclusive) ->
                    list.add("maximum: $maximum")
                    list.add("exclusiveMaximum: $exclusive")
                }
                multipleOf?.also { multipleOf ->
                    list.add("multipleOf: $multipleOf")
                }

                minLength?.also { minLength ->
                    list.add("minLength: $minLength")
                }
                maxLength?.also { maxLength ->
                    list.add("maxLength: $maxLength")
                }
                pattern?.also { pattern ->
                    list.add("pattern: $pattern")
                }

                return list.toList()
            }
            is SwaggerDefinition.Path.SchemaDefinition.ReferencedSchemaDefinition -> {
                return listOf("\$ref: '#/definitions/$name'")
            }
        }
    }

    private fun SwaggerDefinition.Path.ResponseDefinition.toYamlLines(): List<String> {
        val list = mutableListOf("$status:")
        description?.also { description ->
            list.addAll(descriptionToList(description).map { "$PADDING$it" })
        }
        schema?.also { schema ->
            list.add("${PADDING}schema:")
            list.addAll(schema.toYamlLines().map { "$PADDING$PADDING$it" })
        }

        return list.toList()
    }

    private fun descriptionToList(description: String): List<String> {
        return if (description.contains('\n')) {
            listOf("description: |") + description.split('\n').map { "${PADDING}$it" }
        } else {
            listOf("description: ${description.escapeForYamlAndWrapInQuotes()}")
        }
    }

    private fun exampleToList(example: String): List<String> {
        return if (example.contains('\n')) {
            listOf("example: |") + example.split('\n').map { "${PADDING}$it" }
        } else {
            listOf("example: ${example.escapeForYamlAndWrapInQuotes()}")
        }
    }


    private fun String.escapeForYamlAndWrapInQuotes(): String = "\"${this.replace("\"", "\\\"")}\""

}
