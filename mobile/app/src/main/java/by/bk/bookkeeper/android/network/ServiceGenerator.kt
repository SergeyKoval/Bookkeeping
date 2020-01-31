package by.bk.bookkeeper.android.network

import android.annotation.SuppressLint
import by.bk.bookkeeper.android.BuildConfig
import by.bk.bookkeeper.android.network.auth.TokenAuthenticator
import by.bk.bookkeeper.android.network.auth.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateException
import javax.net.ssl.*

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

object ServiceGenerator {

    fun <T> createService(clazz: Class<T>, baseUrl: String): T {
        val tokenOkHttpClient: OkHttpClient = OkHttpClient.Builder()
                .authenticator(TokenAuthenticator())
                .addInterceptor(TokenInterceptor())
                .addInterceptor(createLoggingInterceptor())
                .build()
        return createDefaultRetrofitBuilder(baseUrl)
                .client(tokenOkHttpClient)
                .build()
                .create(clazz)
    }

    private fun createDefaultRetrofitBuilder(baseUrl: String) = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())

    private fun createLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)

    /**
     * OkHttp Client that trust self-signed certificates
     */
    private fun createUnsafeOkHttpClientBuilder(): OkHttpClient.Builder {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })
            val sslContext = SSLContext.getInstance("SSL").apply {
                init(null, trustAllCerts, java.security.SecureRandom())
            }
            val sslSocketFactory = sslContext.socketFactory
            return OkHttpClient.Builder().apply {
                addInterceptor(createLoggingInterceptor())
                sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                hostnameVerifier(object : HostnameVerifier {
                    override fun verify(hostname: String?, p1: SSLSession?): Boolean {
                        hostname?.run {
                            if (endsWith("deplake.tk")) {
                                return true
                            }
                        }
                        return false
                    }
                })
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}