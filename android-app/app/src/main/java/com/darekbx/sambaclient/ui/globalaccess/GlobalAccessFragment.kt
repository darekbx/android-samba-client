package com.darekbx.sambaclient.ui.globalaccess

/**
 * TODO
 * - Use Heroku to manage communication between TimeMachine and Android phone outside local network
 * - RemoteControl:
 *   - authentication: Basic token + login to receive and Bearer token
 *   - list files
 *   - upload file
 *   - download file
 *   - delete file
 * - RemoteControl is just a proxy between rpi and android device
 * - Communication:
 *   - phone is connecting to heroku to execute commands (CommandPattern) with quite long timeout, about 3x for Heroku command delay
 *   - RPi is listening for commands `while { 5s delay to check for commands? }`
 *
 */
class GlobalAccessFragment {
}
