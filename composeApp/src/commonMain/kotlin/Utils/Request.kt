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
import io.ktor.http.HttpStatusCode

private const val BASE_URL = "http://10.0.2.2:8002/api"
//private const val BASE_URL = "http://192.168.0.78/laraveldemo/public/api"
//private const val BASE_URL = "http://127.0.0.1:8002/api"

class Request (private val httpClient: HttpClient): HttpClientInterface
{
    override suspend fun <T> get(url: String, headers: Map<String, String>): NetworkResponse<T> {
        return executeRequest {
            httpClient.get("$BASE_URL$url") {
                headers.forEach { (key, value) -> header(key, value) }
            }
        }
    }

    override suspend fun <T> post(url: String, headers: Map<String, String>, body: Any?): NetworkResponse<T> {
        return executeRequest {
            httpClient.post("$BASE_URL$url") {
                headers.forEach { (key, value) -> header(key, value) }
                setBody(body)
            }
        }
    }

    override suspend fun <T> put(url: String, headers: Map<String, String>, body: Any?): NetworkResponse<T> {
        return executeRequest {
            httpClient.put("$BASE_URL$url") {
                headers.forEach { (key, value) -> header(key, value) }
                setBody(body)
            }
        }
    }

    override suspend fun <T> patch(url: String, headers: Map<String, String>, body: Any?): NetworkResponse<T> {
        return executeRequest {
            httpClient.patch("$BASE_URL$url") {
                headers.forEach { (key, value) -> header(key, value) }
                setBody(body)
            }
        }
    }

    private suspend fun <T> executeRequest(block: suspend () -> HttpResponse): NetworkResponse<T> {
        return try {
            val response = block()
            if (response.status == HttpStatusCode.OK) {
                val networkResponse = response.body<NetworkResponse<T>>()
                if (networkResponse.success) {
                    networkResponse
                } else {
                    NetworkResponse(
                        success = false,
                        message = "Error: ${networkResponse.message}",
                        body = networkResponse.body
                    )
                }
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
