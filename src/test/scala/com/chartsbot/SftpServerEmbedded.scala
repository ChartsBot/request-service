package com.chartsbot

import java.nio.file.Paths
import java.util.Collections

import com.typesafe.config.Config
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider

class SftpServerEmbedded() {

  private val sshd: SshServer = SshServer.setUpDefaultServer()

  def startServer(port: Int, uname: String, pwd: String): Unit = {

    sshd.setPort(port)

    sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
      override def authenticate(username: String, password: String, session: ServerSession): Boolean = {
        username == uname && password == pwd
      }
    })
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser")))
    val sftpFactory = new SftpSubsystemFactory.Builder().build()
    sshd.setSubsystemFactories(Collections.singletonList(sftpFactory))

    sshd.start()
  }

  def stopServer(): Unit = sshd.stop()

}
