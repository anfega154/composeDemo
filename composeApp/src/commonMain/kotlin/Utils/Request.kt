package Utils

import Domain.HttpClientInterface
import Model.NetworkResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import io.ktor.http.ContentType
import io.ktor.http.contentType

//private const val BASE_URL = "http://10.0.2.2:8000/api"
//private const val BASE_URL = "http://192.168.0.78/laraveldemo/public/api"
private const val BASE_URL = "http://192.168.4.27/laraveldemo/public/api"
//private const val BASE_URL = "http://127.0.0.1:8000/api"

class Request (private val httpClient: HttpClient): HttpClientInterface
{
    override suspend fun <T> get(url: String, headers: Map<String, String>, serializer: KSerializer<T>): NetworkResponse<T> {
    return executeRequest({
        httpClient.get("$BASE_URL$url") {
            headers.forEach { (key, value) -> header(key, value) }
        }
    }, serializer)
}

override suspend fun <T> post(url: String, headers: Map<String, String>, body: Any?, serializer: KSerializer<T>): NetworkResponse<T> {
    return executeRequest({
        httpClient.post("$BASE_URL$url") {
            headers.forEach { (key, value) -> header(key, value) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }, serializer)
}

override suspend fun <T> put(url: String, headers: Map<String, String>, body: Any?, serializer: KSerializer<T>): NetworkResponse<T> {
    return executeRequest({
        httpClient.put("$BASE_URL$url") {
            headers.forEach { (key, value) -> header(key, value) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }, serializer)
}

override suspend fun <T> patch(url: String, headers: Map<String, String>, body: Any?, serializer: KSerializer<T>): NetworkResponse<T> {
    return executeRequest({
        httpClient.patch("$BASE_URL$url") {
            headers.forEach { (key, value) -> header(key, value) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }, serializer)
}
private suspend fun <T> executeRequest(block: suspend () -> HttpResponse, serializer: KSerializer<T>): NetworkResponse<T> {
    return try {
        val response = block()
        if (response.status == HttpStatusCode.OK) {
            val networkResponse = Json.decodeFromString(NetworkResponse.serializer(serializer), response.bodyAsText())
            NetworkResponse(
                success = true,
                message = networkResponse.message ?: "Success",
                body = networkResponse.body
            )
        } else {
            NetworkResponse(
                success = false,
                message = "Error: ${response.status.value} - ${response.status.description}",
                body = null as T
            )
        }
    } catch (e: Exception) {
        NetworkResponse(
            success = false,
            message = e.message ?: "Unknown error",
            body = null as T
        )
    }
}

}
