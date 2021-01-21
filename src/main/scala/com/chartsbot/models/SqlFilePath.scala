package com.chartsbot.models

import java.sql.Timestamp

/**
  * Describe the row of the FilesPaths table
  *
  * @param chatId Int, the chatId of the stored thingy
  * @param fileClassification Type of file: could be a meme, or a flyer, or something else
  * @param fileType Image, audio, video, ...
  * @param fileName Name of the file as it's stored in the storage box
  * @param author Name of the author
  * @param timeCreation Epoch seconds of the file creation
  */
case class SqlFilePath(
    chatId: Int,
    chatTitle: String,
    fileClassification: String,
    fileType: String,
    fileName: String,
    author: String,
    timeCreation: Int
) {
  override def toString: String = {
    "chatId -> " + chatId + " - chatTitle " + chatTitle + " - fileClassification -> " +
      fileClassification + " - fileType -> " + fileType +
      " - fileName -> " + fileName + " - author -> " + author + " - timeCreation -> " + timeCreation
  }
}

