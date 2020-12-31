package com.chartsbot.services

import java.net.InetAddress

import akka.stream.alpakka.ftp.{ FtpCredentials, SftpSettings }
import com.chartsbot.config.ConfigPaths.FtpPaths
import com.typesafe.config.Config
import javax.inject.{ Inject, Singleton }

@Singleton
class SftpConnector @Inject() (config: Config) extends FtpPaths {

  val ftpsCredentials: FtpCredentials = FtpCredentials.create(
    username = config.getString(FTP_CONNECTION_CREDENTIALS_USERNAME),
    password = config.getString(FTP_CONNECTION_CREDENTIALS_PASSWORD)
  )

  val sftpSettings: SftpSettings = SftpSettings
    .create(InetAddress.getByName(config.getString(FTP_CONNECTION_HOSTNAME)))
    .withPort(config.getInt(FTP_CONNECTION_PORT))
    .withCredentials(ftpsCredentials)
    .withStrictHostKeyChecking(false)

}
