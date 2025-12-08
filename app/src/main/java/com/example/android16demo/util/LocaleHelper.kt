package com.example.android16demo.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.example.android16demo.data.sync.SyncPreferences
import java.util.Locale

/**
 * Utility class for managing app locale/language settings
 */
object LocaleHelper {
    
    /**
     * Apply the saved language preference to the context
     */
    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            SyncPreferences.LANGUAGE_CHINESE -> Locale.SIMPLIFIED_CHINESE
            SyncPreferences.LANGUAGE_ENGLISH -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
        
        return updateResources(context, locale)
    }
    
    /**
     * Get locale from language code
     */
    fun getLocale(languageCode: String): Locale {
        return when (languageCode) {
            SyncPreferences.LANGUAGE_CHINESE -> Locale.SIMPLIFIED_CHINESE
            SyncPreferences.LANGUAGE_ENGLISH -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
    }
    
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        return context.createConfigurationContext(configuration)
    }
}
