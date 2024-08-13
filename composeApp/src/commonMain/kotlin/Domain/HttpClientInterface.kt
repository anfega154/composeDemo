package Domain

import Model.NetworkResponse

interface HttpClientInterface {
    suspend fun <T> get(url: String, headers: Map<String, String> = emptyMap()):NetworkResponse<T>
    suspend fun <T> post(url: String, headers: Map<String, String> = emptyMap(), body: Any? = null): NetworkResponse<T>
    suspend fun <T> put(url: String, headers: Map<String, String> = emptyMap(), body: Any? = null): NetworkResponse<T>
    suspend fun <T> patch(url: String, headers: Map<String, String> = emptyMap(), body: Any? = null): NetworkResponse<T>
}