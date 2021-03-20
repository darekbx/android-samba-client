package com.darekbx.sambaclient.preferences

import android.content.SharedPreferences

class AuthPreferences(private val preferences: SharedPreferences) {

    private val remberedAddressKey by lazy { "rememberedAddress_key" }
    private val remberedUserKey by lazy { "rememberedUser_key" }
    private val remberedShareNameKey by lazy { "rememberedShareName_key" }

    data class Credentials(val address: String?, val user: String?) {
        val arePersisted = address != null && user != null
    }

    fun persist(shareName: String?) {
        save {
            putString(remberedShareNameKey, shareName)
        }
    }

    fun readShareName() = preferences.getString(remberedShareNameKey, "")

    fun persist(address: String?, user: String?) {
        save {
            putString(remberedAddressKey, address)
            putString(remberedUserKey, user)
        }
    }

    fun read(): Credentials {
        return Credentials(
            preferences.getString(remberedAddressKey, null),
            preferences.getString(remberedUserKey, null)
        )
    }

    fun clearAddressAndUser() {
        persist(null, null)
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
