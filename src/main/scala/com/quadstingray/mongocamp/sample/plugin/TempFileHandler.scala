package com.quadstingray.mongocamp.sample.plugin
import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.plugin.FilePlugin

import scala.util.Random

class TempFileHandler extends FilePlugin with LazyLogging {
  override val name: String = "tempfile"

  override def getFile(bucket: String, fileId: String): File = {
    logger.warn("bucket getFile return random TemporaryFile")
    val file = File.newTemporaryFile()
    file.appendText(bucket + "\r\n")
    file.appendText(fileId + "\r\n")
    file.appendText(Random.alphanumeric.take(256).mkString)
    file
  }

  override def deleteFile(bucket: String, fileId: String): Boolean = {
    logger.warn("bucket deleteFile not implemented")
    true
  }

  override def putFile(bucket: String, fileId: String, file: File): Boolean = {
    logger.warn("bucket putFile not implemented")
    true
  }

  override def size(bucket: String): Double = {
    logger.warn("bucket size return random double")
    Random.nextDouble()
  }

  override def delete(bucket: String): Unit = {
    logger.warn("bucket delete not implemented")
  }

  override def clear(bucket: String): Boolean = {
    logger.warn("bucket clear not implemented")
    true
  }
}
