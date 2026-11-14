package com.zakshub.nexus.security

import android.content.Context
import android.content.SharedPreferences

/**
 * Secure local preference manager for optional external AI API keys (e.g., Google Gemini).
 * Kept entirely on-device, never committed to git, logged, or exported in plain backups.
 */
object SecureApiKeyStorage {

    private const val PREFS_NAME = "nexus_secure_keys"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveGeminiApiKey(context: Context, apiKey: String) {
        val trimmed = apiKey.trim()
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, trimmed).apply()
    }

    fun saveApiKey(context: Context, apiKey: String) = saveGeminiApiKey(context, apiKey)

    fun getGeminiApiKey(context: Context): String? {
        val key = getPrefs(context).getString(KEY_GEMINI_API_KEY, null)
        return if (key.isNullOrBlank()) null else key
    }

    fun clearGeminiApiKey(context: Context) {
        getPrefs(context).edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    fun clearApiKey(context: Context) = clearGeminiApiKey(context)

    fun hasApiKey(context: Context): Boolean {
        return getGeminiApiKey(context) != null
    }

    /**
     * Returns a safe masked representation of the API key for UI display (e.g. "AIzaSy...4x9B").
     */
    fun getMaskedApiKey(context: Context): String {
        val raw = getGeminiApiKey(context) ?: return "No API key configured (100% Offline Mode)"
        if (raw.length <= 8) return "••••••••"
        return "${raw.take(6)}••••••••${raw.takeLast(4)}"
    }
}
