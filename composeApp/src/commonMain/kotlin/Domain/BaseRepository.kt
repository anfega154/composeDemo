package Domain

import Utils.Mantum
import com.demo.Database
import io.ktor.client.HttpClient

open class BaseRepository (
    private val database : Database,
    private val mantum : Mantum,
    private val httpClient : HttpClient
){
    fun getDatabase() = database
    fun getMantum() = mantum
    fun getHttpClient() = httpClient
}