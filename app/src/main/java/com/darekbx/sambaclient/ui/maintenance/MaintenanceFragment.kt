package com.darekbx.sambaclient.ui.maintenance

import androidx.fragment.app.Fragment
import com.darekbx.sambaclient.R

/**
 * RPi maintentance server:
 * - created with http server
 * - communication through json
 * - authorization: md5({logged_user})
 *
 * - free space
 * - last backup date
 * - make backup (select usb drive for a backup, detect usb drives)
 * - find big files?
 * - chart by file sizes and file types?
 */
class MaintenanceFragment: Fragment(R.layout.fragment_maintenance) {
}
