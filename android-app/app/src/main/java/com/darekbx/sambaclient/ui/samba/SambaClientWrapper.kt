package com.darekbx.sambaclient.ui.samba

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileAllInformation
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import java.io.OutputStream
import java.lang.IllegalStateException
import java.math.BigInteger
import java.security.MessageDigest

class SambaClientWrapper(private val smbClient: SMBClient) {

    companion object {
        const val PATH_DELIMITER = "\\"
    }

    private var _session: Session? = null
    private var _diskShare: DiskShare? = null

    fun authenticate(server: String, user: String? = null, password: String? = null) {
        val connection = smbClient.connect(server)
        val authenticationContext = createAuthContext(user, password)
        _session = connection.authenticate(authenticationContext)
    }

    fun connectToDiskShare(shareName: String) {
        _diskShare = _session?.connectShare(shareName) as DiskShare
    }

    fun close() {
        _session?.close()
        _diskShare?.close()
    }

    fun generateCredentialsMd5(): String {
        val authContext = _session?.authenticationContext
            ?: throw IllegalStateException("Authentication context is null")
        val salt = "${authContext.username}_${authContext.password}"
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(salt.toByteArray())).toString(16).padStart(32, '0')
    }

    fun list(directory: String): List<SambaFile> {
        val list = _diskShare?.list(directory)
        return list?.map { it.toSambaFile() }
            ?: throw IllegalStateException("Disk share error!")
    }

    fun fileInformation(path: String): SambaFile? {
        val fileInformation = _diskShare?.getFileInformation(path) ?: return null
        return fileInformation.toSambaFile()
    }

    fun fileDownload(path: String, outputStream: OutputStream) {
        _diskShare
            ?.openFile(
                path,
                setOf(AccessMask.GENERIC_READ),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null
            )
            ?.use { file ->
                file.read(outputStream)
            } ?: throw IllegalStateException("File is not accessible")
    }

    fun fileDelete(path: String) {
        _diskShare?.rm(path)
    }

    fun createDirectory(path: String, newDirectoryName: String) {
        val newDirPath =
            if (path.isEmpty()) newDirectoryName
            else path + PATH_DELIMITER + newDirectoryName
        _diskShare
            ?.openDirectory(
                newDirPath,
                setOf(AccessMask.FILE_LIST_DIRECTORY, AccessMask.FILE_ADD_SUBDIRECTORY),
                setOf(FileAttributes.FILE_ATTRIBUTE_DIRECTORY),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_CREATE,
                setOf(SMB2CreateOptions.FILE_DIRECTORY_FILE)
            )
            ?.close()
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

    private fun FileAllInformation.toSambaFile(): SambaFile {
        return SambaFile(
            nameInformation,
            basicInformation.creationTime.toEpochMillis(),
            basicInformation.changeTime.toEpochMillis(),
            standardInformation.endOfFile,
            basicInformation.fileAttributes
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