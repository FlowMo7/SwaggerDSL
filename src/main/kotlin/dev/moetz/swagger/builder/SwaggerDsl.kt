package dev.moetz.swagger.builder

/**
 * Marker annotation for DSL methods.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
internal annotation class SwaggerDsl