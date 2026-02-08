package com.github.cgang.syncfiles.constants

import java.util.concurrent.TimeUnit

object Config {
    // Default server URL (can be changed by user in settings)
    const val DEFAULT_SERVER_URL = "https://your-server.com:8080"

    // Network
    const val CONNECT_TIMEOUT = 30L  // seconds
    const val READ_TIMEOUT = 60L     // seconds
    const val WRITE_TIMEOUT = 60L    // seconds

    // Sync
    const val CHUNK_SIZE = 1024 * 1024  // 1MB
    const val MAX_CONCURRENT_DOWNLOADS = 3
    const val MAX_CONCURRENT_UPLOADS = 2
    const val SYNC_INTERVAL_HOURS = 1L
    const val LARGE_FILE_THRESHOLD = 10 * 1024 * 1024  // 10MB

    // Storage
    const val MAX_CACHE_SIZE = 100 * 1024 * 1024L  // 100MB

    // Pagination
    const val DEFAULT_PAGE_SIZE = 100
    const val MAX_PAGE_SIZE = 1000

    // Authentication
    const val REALM = "file-hub"
    const val SESSION_COOKIE_NAME = "filehub_session"
    const val SESSION_EXPIRATION_HOURS = 24L

    // Preferences
    const val PREFERENCES_NAME = "syncfiles_prefs"
    const val KEY_SERVER_URL = "server_url"
    const val KEY_USERNAME = "username"
    const val KEY_SESSION = "session_data"

    const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    const val KEY_ALIAS_CREDENTIALS = "syncfiles_credentials"
    const val KEY_ENCRYPTION = "AES/CBC/PKCS7Padding"
}
