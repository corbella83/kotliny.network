[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.kotliny.network/kotliny-network/badge.svg)](https://search.maven.org/artifact/com.kotliny.network/kotliny-network)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.22-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

Kotliny - Network Client
==========

Kotliny Network is a simple, powerful and lightweight Kotlin Multiplatform Network Client.

Get it with Gradle:

```groovy
implementation("com.kotliny.network:kotliny-network-client:1.1.0")
```

To create a basic client you will need a folder that will be used by the client to save temporary files.

```kotlin
val folder = folderOf("/some/temporary/folder")

val client = NetworkClient(folder)
```

Some options can also be provided when creating the `NetworkClient`:

```kotlin
val client = NetworkClient(folder) {
    setUserAgent("MyAgent") // Sets the User-Agent header to use by all requests
    setLoggerEnabled()  // Enables logging all requests and responses.
    setCacheEnabled()  // Enables cache according to the received headers ("Cache-Control").
    setCookiesEnabled()  // Enables cookies, using the corresponding headers ("Set-Cookie", "Cookie")
}
```

### Urls

To provide urls, you can use the `urlOf` methods that will parse the URL:

```kotlin
val url: HttpUrl = urlOf("http://www.domain.com/some/path?some=query")

val url: HttpUrl = urlOf("http", "www.api.domain.com", "some/path", listOf("some" to "query"))
```

These methods will throw an exception if something is wrong. But you can use the null-safe equivalent instead:

```kotlin
val url: HttpUrl? = urlOrNullOf("http://www.api.domain.com/some/path?some=query")

val url: HttpUrl? = urlOrNullOf("http", "www.api.domain.com", "some/path", listOf("some" to "query"))
```

Request
--------
The body of a request is represented by an `HttpContent` and can be one of the following types:

### HttpContent.Empty

This is the body that is used when the request doesn't need a body.

```kotlin
client.launch(HttpMethod.GET, url, HttpContent.Empty())

// To make a request with headers:
val emptyContent = HttpContent.Empty(headersOf("My-Token" to "123456"))
client.launch(HttpMethod.GET, url, emptyContent)
```

### HttpContent.Single

This is the body that is used when sending a single Content-Type.

Every Content-Type is represented by an `HttpContentData`.

For example, to send a json (application/json):

```kotlin
val jsonData = HttpContentData.Json("{ \"first\": \"one\", \"second\": \"two\" }")
client.launch(HttpMethod.POST, url, HttpContent.Single(jsonData))

// To make a request with headers:
val singleContent = HttpContent.Single(jsonData, headersOf("My-Token" to "123456"))
client.launch(HttpMethod.POST, url, singleContent)
```

And to send an image (image/jpeg):

```kotlin
val imageData = HttpContentData.Image("jpeg", File(...))
client.launch(HttpMethod.POST, url, HttpContent.Single(imageData))

// To make a request with headers:
val singleContent = HttpContent.Single(imageData, headersOf("My-Token" to "123456"))
client.launch(HttpMethod.POST, url, singleContent)
```

### HttpContent.Mix and HttpContent.Form

This is the body that is used when sending multiple Content-Types under the same request. The so called MULTIPART.
`Mix` is the default usage (multipart/mixed), while `Form` is when the contents are disposed by a form (multipart/form-data)

For example, to send a multipart form with a json (application/json) and an image (image/jpeg):

```kotlin
val jsonData = HttpContentData.Json("{ \"first\": \"one\", \"second\": \"two\" }")
val imageData = HttpContentData.Image("jpeg", File(...))

client.launch(
    HttpMethod.POST, url, HttpContent.Form(
        mapOf(
            "properties" to HttpContent.Single(jsonData, /*Some Optional Headers*/),
            "profile" to HttpContent.Single(imageData, /*Some Optional Headers*/),
        ),
    )
)
```

Response
--------
The response is represented as an `HttpResult<HttpContent, HttpContent>`. Where `HttpContent` is the same model as defined in the request. The result can be:

* `Success` -> If the response code is 2xx.
* `Error` -> If the response code is 4xx or 5xx
* `Failure` -> If some unexpected behavior has happened

The 3xx response codes are handled internally and should never arrive at this point.

```kotlin
val result: HttpResult<HttpContent, HttpContent> = client.launch(HttpMethod.GET, url, HttpContent.Empty())
```

An `HttpResult` can be mapped into anything else:

```kotlin
val result1: HttpResult<MyModel, HttpContent> = result.mapSuccess {
    // Transform from HttpContent response to MyModel
}

val result2: HttpResult<HttpContent, MyErrorModel> = result.mapError {
    // Transform from HttpContent response to MyErrorModel
}

val result3: HttpResult<HttpContent, HttpContent> = result.mapFailure {
    // Transform the exception into another exception
}
```

Or we can just fold the `HttpResult` into another type

```kotlin
val kotlinResult: Result<Pair<Int, String>> = result.fold(
    onSuccess = { Result.success(code to response.toString()) },
    onError = { Result.success(code to response.toString()) },
    onFailure = { Result.failure(exception) }
)
```

Also, if we are just interested in a particular result type, we can just get it:

```kotlin
val result1: MyModel? = result.successOrNull

val result2: MyErrorModel? = result.errorOrNull

val result3: Throwable? = result.failureOrNull
```

Api Caller
--------
In order to simplify the integration with an API, we can use the `ApiCaller` extension:

```groovy
implementation("com.kotliny.network:kotliny-network-api-caller:1.1.0")
```

To get an instance of an `ApiCaller` you will need the client responsible for making the requests, the base URL of the API service, and an `APISerializer` instance (JSON or XML) that will be used by default to serialize / deserialize objects.
Additionally, the library provides a default implementation of a JSON serializer.

```groovy
implementation("com.kotliny.network:kotliny-network-serializer-json:1.1.0") // APISerializer that uses the kotlinx.serialization library (multiplatform)
or
implementation("com.kotliny.network:kotliny-network-serializer-gson:1.1.0") // APISerializer that uses the gson library (only for jvm)
```

```kotlin
val apiCaller = ApiCaller(client, folder, urlOf("api.domain.com"), JsonApiSerializer())
```

Its usage is quite simple. The `ApiCaller` will automatically handle Content-Types and provide you with the expected result.
If the received Content-Type cannot be represented as the expected type, an `HttpResult.Failure` will be returned. 
For instance, if you expect a JSON but receive an "image/jpeg", an `HttpResult.Failure` will be returned.

```kotlin
// Expect a JSON model when success or error
val result: HttpResult<MyModel, MyErrorModel> = apiCaller.get<MyModel, MyErrorModel>("relative/path/json")

// Expect a file when success, and a JSON when error
val result: HttpResult<File, MyErrorModel> = apiCaller.get<File, MyErrorModel>("relative/path/image")

// Expect a string when success (might be raw json, hml, etc. any content capable of being represented as a string), and the unhandled content when error
val result: HttpResult<String, HttpContent> = apiCaller.get<MyModel, HttpContent>("relative/path/json")
```

You can also define your own rule to handle a particular response model. Imagine that the previous MyErrorModel is a sealed class and needs to be parsed different depending on the response code:

```kotlin
class ErrorContentHandler(serializer: ApiSerializer) : ContentHandler<MyErrorModel> {
    private val clientSerializable = SerializableContentHandler(fullType<MyErrorModel.Client>(), serializer)
    private val serverSerializable = SerializableContentHandler(fullType<MyErrorModel.Server>(), serializer)

    override val type: FullType<MyErrorModel> = fullType<MyErrorModel>()

    override fun convert(code: Int, content: HttpContent): Result<MyErrorModel> {
        return if (code in 400..499) {
            clientSerializable.convert(code, content)
        } else {
            serverSerializable.convert(code, content)
        }
    }
}

// Add it into the apiCaller instance that you have
apiCaller.addContentHandler(ErrorContentHandler(jsonSerializable))
```

### Headers and Queries

To make a request with custom queries and headers:

```kotlin
val apiCaller = apiCaller.get<MyModel, MyErrorModel>("relative/path/json") {
    // Set header
    setHeader("My-Auth", "TOKEN")

    // Set simple query
    setQuery("first", "one")

    // Set query list
    setQuery("letter", listOf("a", "b", "c"))
}
```

If there are some queries or headers that must be used along all the requests, set them to the apiCaller instance

```kotlin
// Set common header
apiCaller.setCommonHeader("My-Auth", "TOKEN")

// Set common lazy header.
apiCaller.setCommonQuery("My-Auth") { "TOKEN" }

// Set common query
apiCaller.setCommonQuery("first", "one")

// Set common lazy query.
apiCaller.setCommonQuery("first") { "one" }
```

Engines
--------
By default, a `NetworkClient` instance is using a java8 engine (for java) and URLSession engine (for ios) to perform the requests.

If using java8 and Android, PATCH is not officially supported and needs to have this Proguard rule in order for it to work.

```proguard
-keep class * implements java.net.HttpURLConnection { *; }
```

The library provides 2 more engines to be used in JVM, but you can always write your own implementation of an engine by extending `HttpEngine` if you need.

```groovy
// To use a HttpClient defined in java since JMV11 
implementation("com.kotliny.network:kotliny-network-engine-jvm11:1.1.0")

// To use an OkHttpClient
implementation("com.kotliny.network:kotliny-network-engine-okhttp:1.1.0")
```

```kotlin
val client = NetworkClient(folder) {
    setEngine(Java11HttpEngine())
}

val client = NetworkClient(folder) {
    setEngine(OkHttpEngine())
}
```

Testing
--------
The library also provides a simple utilities in order to simplify testing the network layer, without actually making the requests.

```groovy
implementation("com.kotliny.network:kotliny-network-engine-test:1.1.0")
```

There are mainly three types of engines that can be used for testing:

### EchoHttpEngine

This engine simply takes the request and returns it as the response. If you need to test how a certain response is handled by your app, simply make a request with that response, and you'll get it back.
By default, the response code is a 200, but you can use the extra header EchoHttpEngine.RESPONSE_CODE to define another one.

```kotlin
val client = NetworkClient(folder) {
    setEngine(EchoHttpEngine())
}

// Using the client directly
val content = HttpContent.Single(HttpContentData.Text("Hi There"))
val result1: HttpResult<HttpContent, HttpContent> = client.launch(HttpMethod.GET, url, content)

// Or using the ApiCaller
val result2: HttpResult<String, String> = apiCaller.get("path", HttpContentData.Text("Hi There"))
```

### MockHttpEngine

This engine mocks a certain response, given a certain request. When using this engine, every unmocked request will throw an exception.

```kotlin
val engine = MockHttpEngine()
val client = NetworkClient(folder) {
    setEngine(engine)
}

engine.setResponseFor("GET", "http://www.domain.com/path") {
    NetworkResponse(403, listOf(), "Hi there".source())
}

// Using the client directly
val result1: HttpResult<HttpContent, HttpContent> = client.launch(HttpMethod.GET, url, HttpContent.Empty())

// Or using the ApiCaller
val result2: HttpResult<String, String> = apiCaller.get("path", HttpContentData.Empty())
```

### LocalHttpEngine

This engine is a very simple implementation of a functional server. You can POST elements that latter can be retrieved by GET and can be removed by DELETE. To simplify things, this local engine is only working with `HttpContentData.Text`.

```kotlin
val client = NetworkClient(folder) {
    setEngine(LocalHttpEngine())
}

val id: Long = apiCaller.post("data", HttpContentData.Text("One"))

val result: String? = apiCaller.get<String>("data/$id").successOrNull // Result is "One"

apiCaller.delete("data/$id")

val result: String? = apiCaller.get<String>("data/$id").successOrNull // Result is null due to 404 Not Found
```

License
-------

    Copyright 2023 Pau Corbella

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    