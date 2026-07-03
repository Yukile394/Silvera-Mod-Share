package com.silvera.modshare.auth

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Basit, sunucusuz kimlik doğrulama.
 * - E-posta + şifre: SHA-256 + salt ile cihazda saklanır (SharedPreferences).
 * - Google: cihazda kayıtlı bir Google hesabı seçtirilip o hesabın e-postası ile giriş yapılır
 *   (tam OAuth için Firebase/Google Cloud projesi ve google-services.json gerekir; bu, ekstra
 *   sunucu kurulumu olmadan çalışan hafif bir alternatiftir).
 */
object AuthManager {

    private const val PREFS = "silvera_auth"
    private const val KEY_USERS = "users" // "email:hash:salt" -> satır satır
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_CURRENT_EMAIL = "current_email"
    private const val KEY_CURRENT_NAME = "current_name"
    private const val KEY_CURRENT_PROVIDER = "current_provider" // "local" | "google"
    private const val KEY_AVATAR_URI = "avatar_uri"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isLoggedIn(context: Context): Boolean = prefs(context).getBoolean(KEY_LOGGED_IN, false)

    fun currentEmail(context: Context): String? = prefs(context).getString(KEY_CURRENT_EMAIL, null)

    fun currentName(context: Context): String =
        prefs(context).getString(KEY_CURRENT_NAME, null) ?: currentEmail(context)?.substringBefore("@") ?: "Kullanıcı"

    fun currentProvider(context: Context): String = prefs(context).getString(KEY_CURRENT_PROVIDER, "local") ?: "local"

    fun avatarUri(context: Context): String? = prefs(context).getString(KEY_AVATAR_URI, null)

    fun setAvatarUri(context: Context, uri: String?) {
        prefs(context).edit().putString(KEY_AVATAR_URI, uri).apply()
    }

    fun setDisplayName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_CURRENT_NAME, name).apply()
    }

    fun register(context: Context, email: String, password: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) return Result.failure(IllegalArgumentException("Geçerli bir e-posta gir"))
        if (password.length < 4) return Result.failure(IllegalArgumentException("Şifre en az 4 karakter olmalı"))

        val p = prefs(context)
        val users = p.getStringSet(KEY_USERS, emptySet())!!.toMutableSet()
        if (users.any { it.startsWith("$cleanEmail:") }) {
            return Result.failure(IllegalStateException("Bu e-posta zaten kayıtlı"))
        }
        val salt = randomSalt()
        val hash = hash(password, salt)
        users.add("$cleanEmail:$hash:$salt")
        p.edit().putStringSet(KEY_USERS, users).apply()

        loginLocal(context, cleanEmail)
        return Result.success(Unit)
    }

    fun login(context: Context, email: String, password: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        val p = prefs(context)
        val users = p.getStringSet(KEY_USERS, emptySet())!!
        val record = users.firstOrNull { it.startsWith("$cleanEmail:") }
            ?: return Result.failure(IllegalStateException("Kayıtlı kullanıcı bulunamadı"))
        val parts = record.split(":")
        if (parts.size != 3) return Result.failure(IllegalStateException("Hesap bilgisi bozuk"))
        val (_, storedHash, salt) = parts
        if (hash(password, salt) != storedHash) return Result.failure(IllegalStateException("Şifre yanlış"))

        loginLocal(context, cleanEmail)
        return Result.success(Unit)
    }

    fun loginWithGoogleAccount(context: Context, accountEmail: String) {
        prefs(context).edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_CURRENT_EMAIL, accountEmail)
            .putString(KEY_CURRENT_NAME, accountEmail.substringBefore("@"))
            .putString(KEY_CURRENT_PROVIDER, "google")
            .apply()
    }

    private fun loginLocal(context: Context, email: String) {
        prefs(context).edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_CURRENT_EMAIL, email)
            .putString(KEY_CURRENT_PROVIDER, "local")
            .apply()
    }

    fun logout(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_CURRENT_EMAIL)
            .remove(KEY_CURRENT_PROVIDER)
            .apply()
    }

    private fun randomSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + password).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

