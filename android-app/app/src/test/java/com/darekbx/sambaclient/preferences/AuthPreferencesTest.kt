package com.darekbx.sambaclient.preferences

import android.content.Context
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AuthPreferencesTest {

    private val preferences =
        RuntimeEnvironment.application.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
    private val authPreferences = AuthPreferences(preferences)

    @After
    fun cleanUp() {
        preferences.edit().clear().apply()
    }

    @Test
    fun `Values were persisted`() {
        // Given
        authPreferences.persist("Address", "User", "Password")

        // When
        val credentials = authPreferences.read()

        // Then
        assertTrue(credentials.arePersisted)
        assertEquals("Address", credentials.address)
        assertEquals("User", credentials.user)
        assertEquals("Password", credentials.password)
    }

    @Test
    fun `Values were cleared`() {
        // Given
        authPreferences.persist("Address", "User", "Password")
        val credentials = authPreferences.read()
        assertTrue(credentials.arePersisted)

        // When
        authPreferences.clearAddressAndUser()

        // Then
        val clearedCredentials = authPreferences.read()
        assertFalse(clearedCredentials.arePersisted)
        assertNull(clearedCredentials.address)
        assertNull(clearedCredentials.user)
        assertNull(clearedCredentials.password)
    }
}
