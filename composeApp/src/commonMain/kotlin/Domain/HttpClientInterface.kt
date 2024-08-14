package Domain

import Model.NetworkResponse
import kotlinx.serialization.KSerializer

interface HttpClientInterface {
    suspend fun <T> get(url: String, headers: Map<String, String>, serializer: KSerializer<T>): NetworkResponse<T>
    suspend fun <T> post(url: String, headers: Map<String, String>, body: Any?, serializer: KSerializer<T>): NetworkResponse<T>
    suspend fun <T> put(url: String, headers: Map<String, String>, body: Any?, serializer: KSerializer<T>): NetworkResponse<T>
    suspend fun <T> patch(url: String, headers: Map<String, String>, body: Any?, serializer: KSerializer<T>): NetworkResponse<T>
}