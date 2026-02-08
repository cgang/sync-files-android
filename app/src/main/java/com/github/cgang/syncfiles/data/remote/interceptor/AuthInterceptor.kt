package com.github.cgang.syncfiles.data.remote.interceptor

import android.content.Context
import android.content.SharedPreferences
import com.github.cgang.syncfiles.constants.Config
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Config.PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    @Volatile
    private var cachedCookie: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val requestBuilder = originalRequest.newBuilder()

        getCachedCookie()?.let { cookie ->
            requestBuilder.addHeader("Cookie", "${Config.SESSION_COOKIE_NAME}=$cookie")
        }

        val response = chain.proceed(requestBuilder.build())

        response.headers("Set-Cookie").firstOrNull {
            it.startsWith("${Config.SESSION_COOKIE_NAME}=")
        }?.let {
            cachedCookie = it.substringAfter("${Config.SESSION_COOKIE_NAME}=").substringBefore(";")
            prefs.edit().putString(Config.KEY_SESSION, cachedCookie).apply()
        }

        if (response.code == 401) {
            response.close()
            clearCookie()
            throw IOException("Unauthorized - Please login again")
        }

        return response
    }

    private fun getCachedCookie(): String? {
        if (cachedCookie == null) {
            cachedCookie = prefs.getString(Config.KEY_SESSION, null)
        }
        return cachedCookie
    }

    fun clearCookie() {
        cachedCookie = null
        prefs.edit().remove(Config.KEY_SESSION).apply()
    }
}
