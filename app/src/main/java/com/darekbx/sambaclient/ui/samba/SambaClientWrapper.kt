package com.darekbx.sambaclient.ui.samba

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import java.lang.IllegalStateException

class SambaClientWrapper(private val smbClient: SMBClient) {

    private var _session: Session? = null
    private var _diskShare: DiskShare? = null

    fun authenticate(server: String, user: String? = null, password: String? = null) {
        val connection = smbClient.connect(server)
        var authenticationContext = createAuthContext(user, password)
        _session = connection.authenticate(authenticationContext)
    }

    fun connectToDiskShare(shareName: String) {
        _diskShare = _session?.connectShare(shareName) as DiskShare
    }

    fun close() {
        _session?.close()
        _diskShare?.close()
    }

    fun list(directory: String): List<SambaFile> {
        val list = _diskShare?.list(directory)
        return list?.map { it.toSambaFile() }
            ?: throw IllegalStateException("Disk share error!")
    }

    private fun FileIdBothDirectoryInformation.toSambaFile(): SambaFile {
        return SambaFile(
            fileName,
            creationTime.toEpochMillis(),
            changeTime.toEpochMillis(),
            endOfFile,
            fileAttributes
        )
    }

    private fun createAuthContext(user: String?, password: String?): AuthenticationContext {
        return if (user != null && password != null) {
            AuthenticationContext(user, password.toCharArray(), null)
        } else {
            AuthenticationContext.anonymous()
        }
    }
}
