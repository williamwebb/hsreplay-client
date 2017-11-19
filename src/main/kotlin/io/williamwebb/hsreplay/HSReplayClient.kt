/**
 * Created by williamwebb on 6/5/17.
 */
package io.williamwebb.hsreplay

import io.reactivex.Single
import io.reactivex.Single.fromCallable
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

class HSReplayClient(private val okHttpClient: OkHttpClient, private val apiKey: String, var test: Boolean = false) {

    data class TokenRequest(val test_data: Boolean)
    data class User(val id: Int, val battletag: String, val username: String) {
        companion object {
            @JvmField val UNKNOWN: User = User(-1, "", "")
        }
    }
    data class AuthenticationToken(val key: String, val test_data: Boolean, val user: User)
    data class UploadRequest(val match_start: String, val friendly_player: String, val build: Int)
    data class UploadAuthorization(val shortid: String, val url: String, val put_url: String)
    data class ClaimAuthorization(val created: String, val full_url: String, val url: String)

    private val API_URL = "https://api.hsreplay.net/v1/"
    private val UPLOAD_API_URL = "https://upload.hsreplay.net/api/v1/"

    interface API {
        @POST("tokens/{uuid}") fun getToken(@Path("uuid") uuid: String): Single<AuthenticationToken>
        @POST("tokens/") fun createToken(@Body tokenRequest: TokenRequest): Single<AuthenticationToken>
        @POST("claim_account/") fun claimReplaysRequest(): Single<ClaimAuthorization>
    }

    interface UPLOAD_API {
        @POST("replay/upload/request/") fun uploadRequest(@Body request: UploadRequest): Single<UploadAuthorization>
    }

    private val client: OkHttpClient = okHttpClient.newBuilder()
            .addInterceptor {
                val builder = it.request().newBuilder()

                if(token.isNotEmpty())
                    builder.header("Authorization","Token " + token)
                builder.header("X-Api-Key", apiKey)

                return@addInterceptor it.proceed(builder.build())
            }.build()

    private val apiBuilder = Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())

    private val apiService: API = apiBuilder
            .baseUrl(API_URL)
            .build().create(API::class.java)

    private val uploadService: UPLOAD_API = apiBuilder
            .baseUrl(UPLOAD_API_URL)
            .build().create(UPLOAD_API::class.java)

    var token: String = ""

    fun createAuthToken(): Single<AuthenticationToken> = apiService
            .createToken(TokenRequest(test))
            .doOnSuccess { token = it.key }

    fun getAuthToken(): Single<AuthenticationToken> = apiService.getToken(token)

    fun claimReplaysRequest(): Single<ClaimAuthorization> = apiService.claimReplaysRequest()

    fun upload(uploadRequest: UploadRequest, power_log: String): Single<String> {
        if(token.isEmpty()) return Single.error(IllegalStateException("No Auth Token, have you called set or called createAuthToken()?"))
        return uploadService.uploadRequest(uploadRequest).flatMap {
            uploadToS3(it, power_log)
        }
    }

    private fun uploadToS3(authorization: UploadAuthorization, body: String): Single<String> {
        return fromCallable {
            val requestBody = RequestBody.create(MediaType.parse("text/plain"), Gzipper.compress(body))

            val request = Request.Builder()
                    .put(requestBody)
                    .url(authorization.put_url)
                    .header("Content-Encoding", "gzip")
                    .build()

            val client = okHttpClient.newBuilder()
                    .addInterceptor(HttpLoggingInterceptor({System.out.println(it)}).setLevel(BODY))
                    .build()

            val response = client.newCall(request).execute()
            if(!response.isSuccessful) throw Exception("Log upload failed! " + response.toString())

            return@fromCallable authorization.url
        }
    }
}
