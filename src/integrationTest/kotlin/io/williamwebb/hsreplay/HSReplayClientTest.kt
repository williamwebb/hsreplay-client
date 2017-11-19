/**
 * Created by williamwebb on 6/5/17.
 */
package io.williamwebb.hsreplay

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.*
import org.junit.Test
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class HSReplayClientTest {

    val ISO8601DATEFORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    val DEBUG_API_KEY = System.getenv("HS_REPLAY_KEY_DEV") ?: throw Exception("Unable to get property")
    val http_client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor({System.out.println(it)}).setLevel(BODY))
            .build()

    @Test
    fun createTokenTest() {
        val client = HSReplayClient(http_client, DEBUG_API_KEY, true)
        val token = client.createAuthToken().blockingGet()

        assert(token.test_data == true)
        assert(token.key.isNotEmpty())
    }

    @Test
    fun uploadTest() {
        val power_log = this.javaClass.getResource("power.log").readText()
        assert(power_log.isNotEmpty())

        val uploadRequest = HSReplayClient.UploadRequest(ISO8601DATEFORMAT.format(Date()), "1", 13740)

        val client = HSReplayClient(http_client, DEBUG_API_KEY, true)
        val token = client.createAuthToken().blockingGet()
        println("Token: " + token.key)
        val replayUrl = client.upload(uploadRequest, power_log).blockingGet()

        println("Replay Url: " + replayUrl)
        assert(replayUrl.isNotEmpty())
    }

    @Test
    fun claimTest() {
        val power_log = this.javaClass.getResource("power.log").readText()
        assert(power_log.isNotEmpty())

        val uploadRequest = HSReplayClient.UploadRequest(ISO8601DATEFORMAT.format(Date()), "1", 13740)

        val client = HSReplayClient(http_client, DEBUG_API_KEY, true)
        client.createAuthToken().blockingGet()
        val replayUrl = client.upload(uploadRequest, power_log).blockingGet()

        assert(replayUrl.isNotEmpty())

        val cliamRequest = client.claimReplaysRequest().blockingGet()
        assert(cliamRequest.full_url.isNotEmpty())
    }
}