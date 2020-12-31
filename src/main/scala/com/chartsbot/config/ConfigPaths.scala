package com.chartsbot.config

object ConfigPaths {

  trait SqlPaths {
    final val SQLITE_CONNECTION_PATH = "sql.connection.path"
  }

  trait FtpPaths {
    final val FTP_CONNECTION_HOSTNAME = "ftp.connection.hostname"
    final val FTP_CONNECTION_PORT = "ftp.connection.port"
    final val FTP_CONNECTION_CREDENTIALS_USERNAME = "ftp.connection.credentials.username"
    final val FTP_CONNECTION_CREDENTIALS_PASSWORD = "ftp.connection.credentials.password"
    final val FTP_BASE_DIR = "ftp.configuration.base-dir"
  }

}
