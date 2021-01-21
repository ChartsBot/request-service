package com.chartsbot.config

object ConfigPaths {

  trait EcPaths {
    final val EC_NUMBER_THREADS = "ec.thread-number"
  }

  trait SqlPaths {
    final val MYSQL_CONNECTION_PATH = "sql.connection.path"
    final val MYSQL_CONNECTION_USERNAME = "sql.connection.username"
    final val MYSQL_CONNECTION_PASSWORD = "sql.connection.password"
  }

  trait FtpPaths {
    final val FTP_CONNECTION_HOSTNAME = "ftp.connection.hostname"
    final val FTP_CONNECTION_PORT = "ftp.connection.port"
    final val FTP_CONNECTION_CREDENTIALS_USERNAME = "ftp.connection.credentials.username"
    final val FTP_CONNECTION_CREDENTIALS_PASSWORD = "ftp.connection.credentials.password"
    final val FTP_BASE_DIR = "ftp.configuration.base-dir"
  }

  trait GrpcPaths {
    final val GRPC_SERVER_HOST = "akka.grpc.server.host"
    final val GRPC_SERVER_PORT = "akka.grpc.server.port"
    final val GRPC_SERVER_OVRD_AUTH = "akka.grpc.server.override-authority"
    final val GRPC_SERVER_KEY = "akka.grpc.server.key"
    final val GRPC_SERVER_PEM = "akka.grpc.server.pem"
  }

}
