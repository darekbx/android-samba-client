package com.darekbx.sambaclient.preferences

import android.content.SharedPreferences

class AuthPreferences(private val preferences: SharedPreferences) {

    private val remberedAddressKey by lazy { "rememberedAddress_key" }
    private val remberedUserKey by lazy { "rememberedUser_key" }
    private val remberedPasswordKey by lazy { "remberedPassword_key" }
    private val remberedShareNameKey by lazy { "rememberedShareName_key" }

    data class Credentials(val address: String?, val user: String?, val password: String?) {
        val arePersisted = address != null && user != null && password != null
    }

    fun persist(shareName: String?) {
        save {
            putString(remberedShareNameKey, shareName)
        }
    }

    fun readShareName() = preferences.getString(remberedShareNameKey, "")

    fun persist(address: String?, user: String?, password: String?) {
        save {
            putString(remberedAddressKey, address)
            putString(remberedUserKey, user)
            putString(remberedPasswordKey, password)
        }
    }

    fun read(): Credentials {
        return Credentials(
            preferences.getString(remberedAddressKey, null),
            preferences.getString(remberedUserKey, null),
            preferences.getString(remberedPasswordKey, null)
        )
    }

    fun clearAddressAndUser() {
        persist(null, null, null)
    }

    fun clearShareName() {
        persist(null)
    }

    private fun save(editions: SharedPreferences.Editor.() -> Unit) {
        preferences.edit()
            .apply { editions() }
            .apply()
    }
}
