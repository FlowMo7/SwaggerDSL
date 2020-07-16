package dev.moetz.swagger.builder.model

import dev.moetz.swagger.definition.SwaggerDefinition
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test


class ParameterTest {

    @Test
    fun `PathParameter with type string`() {
        val parameter = PathParameter("pathParameterName", "string")
        parameter.definition shouldBeEqualTo SwaggerDefinition.Path.ParameterDefinition(
            name = "pathParameterName",
            `in` = "path",
            description = null,
            required = true,
            type = "string",
            enum = null,
            schema = null,
            format = null,
            arrayItemsSchema = null
        )
    }

    @Test
    fun `PathParameter with type integer`() {
        val parameter = PathParameter("pathParameterName", "integer")
        parameter.definition shouldBeEqualTo SwaggerDefinition.Path.ParameterDefinition(
            name = "pathParameterName",
            `in` = "path",
            description = null,
            required = true,
            type = "integer",
            enum = null,
            schema = null,
            format = null,
            arrayItemsSchema = null
        )
    }

    @Test
    fun `PathParameter with type not set`() {
        val parameter = PathParameter("pathParameterName", null)
        val sut = { parameter.definition }
        sut.shouldThrow(IllegalArgumentException::class)
    }

    @Test
    fun `PathParameter with invalid type`() {
        val sut = { PathParameter("pathParameterName", "someInvalidType") }
        sut.shouldThrow(IllegalArgumentException::class)
    }


    @Test
    fun `QueryParameter with type string`() {
        val parameter = QueryParameter("queryParameterName")
        parameter.type("string")
        parameter.definition shouldBeEqualTo SwaggerDefinition.Path.ParameterDefinition(
            name = "queryParameterName",
            `in` = "query",
            description = null,
            required = null,
            type = "string",
            enum = null,
            schema = null,
            format = null,
            arrayItemsSchema = null
        )
    }

    @Test
    fun `QueryParameter with type integer`() {
        val parameter = QueryParameter("queryParameterName")
        parameter.type("integer")
        parameter.definition shouldBeEqualTo SwaggerDefinition.Path.ParameterDefinition(
            name = "queryParameterName",
            `in` = "query",
            description = null,
            required = null,
            type = "integer",
            enum = null,
            schema = null,
            format = null,
            arrayItemsSchema = null
        )
    }

    @Test
    fun `QueryParameter with type not set`() {
        val parameter = QueryParameter("queryParameterName")
        val sut = { parameter.definition }
        sut.shouldThrow(IllegalArgumentException::class)
    }

    @Test
    fun `QueryParameter with invalid type`() {
        val parameter = QueryParameter("queryParameterName")
        val sut = { parameter.type("someInvalidType") }
        sut.shouldThrow(IllegalArgumentException::class)
    }


    @Test
    fun `BodyParameter with type string`() {
        val parameter = BodyParameter("body")
        parameter.type("string")
        parameter.definition shouldBeEqualTo SwaggerDefinition.Path.ParameterDefinition(
            name = "body",
            `in` = "body",
            description = null,
            required = null,
            type = "string",
            enum = null,
            schema = null,
            format = null,
            arrayItemsSchema = null
        )
    }

    @Test
    fun `BodyParameter with type integer`() {
        val parameter = BodyParameter("body")
        parameter.type("integer")
        parameter.definition shouldBeEqualTo SwaggerDefinition.Path.ParameterDefinition(
            name = "body",
            `in` = "body",
            description = null,
            required = null,
            type = "integer",
            enum = null,
            schema = null,
            format = null,
            arrayItemsSchema = null
        )
    }

    @Test
    fun `BodyParameter with type not set`() {
        val parameter = BodyParameter("body")
        parameter.definition shouldBeEqualTo SwaggerDefinition.Path.ParameterDefinition(
            name = "body",
            `in` = "body",
            description = null,
            required = null,
            type = null,
            enum = null,
            schema = null,
            format = null,
            arrayItemsSchema = null
        )
    }

    @Test
    fun `BodyParameter with invalid type`() {
        val parameter = BodyParameter("body")
        val sut = { parameter.type("someInvalidType") }
        sut.shouldThrow(IllegalArgumentException::class)
    }

}