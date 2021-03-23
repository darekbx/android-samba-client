package com.darekbx.sambaclient.ui.samba

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class PathMovementTest {

    private val tested = PathMovement()

    @AfterEach
    fun cleanUp() {
        tested.clear()
    }

    @Test
    fun `Walk inside and outside`() {
        tested.obtainPath("dir") `should be equal to` "dir"
        tested.obtainPath("subdir") `should be equal to` "dir/subdir"
        tested.obtainPath("subdir") `should be equal to` "dir/subdir/subdir"
        tested.obtainPath(".") `should be equal to` "dir/subdir"
        tested.obtainPath(".") `should be equal to` "dir"
        tested.obtainPath(".") `should be equal to` ""
    }

    @Test
    fun `Walk inside and then to root`() {
        tested.obtainPath("dir") `should be equal to` "dir"
        tested.obtainPath("subdir") `should be equal to` "dir/subdir"
        tested.obtainPath("..") `should be equal to` ""
    }
}
