# Kotlin Swagger DSL

[ ![Download](https://api.bintray.com/packages/flowmo7/maven/swagger-dsl/images/download.svg) ](https://bintray.com/flowmo7/maven/swagger-dsl/_latestVersion)


This library provides a Kotlin DSL to create Swagger definitions and provides a provider for the respective Swagger UI HTML / JS / CSS files.


## Setup

```gradle
allprojects {
    repositories {
        ...
        maven {
            url  "https://dl.bintray.com/flowmo7/maven" 
        }
    }
}

dependencies {
    implementation 'dev.moetz:swagger-dsl:0.2.2'
}
```


## Kotlin DSL Usage


Create a new swagger definition with the given DSL:

```kotlin
val swagger: SwaggerDefinition by lazy {
    SwaggerBuilder.generate {

        info {
            title("My API")
            version("1.0.1")
            description("An example API definition for the Kotlin Swagger DSL")
            host("swagger.example.org")
            basePath("/v1/")
            schemes("https")
        }

        tag("status", "Informational status calls about the API")
        tag("authentication", "Calls regarding authentication and authorization")
        tag("kittens", "Retrieve and put kittens")

        path("/status", "get") {
            summary("Endpoint to check the API status")
            description("Returns 200 on a healthy API with some additional information.")
            operationId("status")
            tags("status")
            produces("text/html")
            response(200) {
                description("API is up & running")
            }
            response(500) {
                description("Error in API")
            }
        }
        
        path("/kittens/{year}", "get") {
            summary("Get the kittens for a given year.")
            description("Returns a list of all kittens for a given year (optionally only kittens with the given breed type, if the query-parameter breed is set).")
            operationId("kittens/year")
            tags("kittens")
            produces("application/json")

            pathParameter("year", "string") {
                description("The year to get the kittens of")
                default("2019")
            }

            queryParameter("breed") {
                description(
                    "The breeds to get the holidays of.\n" +
                            "Possible breeds are:\n" +
                            "[${Breed.values().joinToString(separator = ", ") { it.name.toLowerCase() }}]"
                )
                default(states.first().name)
                required(false)

                type("string")
                enum(*Breed.values().map { it.name }.toTypedArray())
            }

            response(200) {
                description("A list of kitten images for the given year (and optionally breed).")
                schema(Schema.createArray {
                    items(Schema.createObject {
                        property("inserted", Schema.createType("string") {
                            description("The date when the kitten image was inserted")
                            format("YYYY-MM-DD")
                            example("2019-12-25")
                            required(true)
                        })

                        property("url", Schema.createType("string") {
                            description("The url of the kitten image.")
                            example("https://images.example.org/kitten1.png")
                            required(true)
                        })

                        property("breed", Schema.createType("string") {
                            description("The breed of the kitten.")
                            example(Breed.values().first().name)
                            required(true)
                            enum(*Breed.values().map { it.name }.toTypedArray())
                        })
                    })
                })
            }
            response(400) {
                description("Parameter year missing, or parameter 'breed' illegal.")
            }
        }
        
        
    }
}
```

To generate a YAML swagger definition, use the provided YamlFileGenerator:

```kotlin
val yamlFileContents = YamlFileGenerator.generate(swaggerDefinition)
```

## Provide Swagger-UI Usage

The library also provides a handler for serving the Swagger-UI and a given SwaggerDefinition.
However, the library does not provide any _real_ server, to keep your choice of server free.

Therefore, the `SwaggerHandler` only provides the `get(relativeUrl: String)` method, which returns the ByteArray for the given site to return.

An example using [Ktor.io](https://ktor.io/) could look like:
```kotlin
route("swagger") {
    get("{...}") {
        val rawUri = call.request.uri

        if (rawUri == "/swagger") {
            call.respondRedirect("/swagger/", true)
        } else {
            val uri = rawUri.substringAfter("swagger")
                .let { if (it.firstOrNull() == '/') it.drop(1) else it }
                .let { if (it.isBlank()) "index.html" else it }

            val swaggerContent = swaggerHandler.get(uri)
            if (swaggerContent != null) {
                val (mimeType, content) = swaggerContent
                call.respondBytes(ContentType.parse(mimeType)) { content }
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
```

# License

```
Copyright 2018 Florian Mötz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
