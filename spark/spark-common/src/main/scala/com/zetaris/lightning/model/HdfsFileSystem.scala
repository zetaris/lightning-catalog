/*
 *
 *  * Copyright 2023 ZETARIS Pty Ltd
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  * associated documentation files (the "Software"), to deal in the Software without restriction,
 *  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 *  * subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all copies
 *  * or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.zetaris.lightning.model

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SparkSession

import java.io._

object HdfsFileSystem {
  /**
   * break folder into parent and child
   * @param path
   * @return
   */
  def toFolderUrl(path: String): (String, String) = {
    val withProtocol = if (path.startsWith("/")) {
      s"file://$path"
    } else {
      path
    }

    val index = withProtocol.indexOf("://")
    if (index < 0) {
      throw new RuntimeException(s"wrong path : $withProtocol")
    }

    val parentIndex = withProtocol.indexOf("/", index + 3)

    if (parentIndex > 0) {
      (withProtocol.substring(0, parentIndex + 1), withProtocol.substring(parentIndex + 1))
    } else {
      (withProtocol, "")
    }
  }
}

class HdfsFileSystem(properties: Map[String, String], rootUrl: String) {
  private def buildFileSystem(): FileSystem = {
    val hadoopConf = SparkSession.active.sparkContext.hadoopConfiguration
    properties.foreach { case (k, v) =>
      hadoopConf.set(k, v)
    }
    FileSystem.get(new java.net.URI(rootUrl), hadoopConf)
  }

  def folderExist(folderPath: String): Boolean = {
    val fs = buildFileSystem()
    try {
      val targetPath = new Path(rootUrl, folderPath)
      fs.exists(targetPath)
    } finally {
      fs.close()
    }
  }

  def fileExists(filePath: String): Boolean = {
    folderExist(filePath)
  }

  /**
   * create folder if not exist
   * @param folderPath folderPath
   */
  def createFolderIfNotExist(folderPath: String): Path = {
    val fs = buildFileSystem()
    try {
      val targetPath = new Path(rootUrl, folderPath)
      if (!fs.exists(targetPath)) {
        fs.mkdirs(targetPath)
      }
      targetPath
    } finally {
      fs.close()
    }
  }

  /**
   * list sub directories for the given parent
   * @param parent parent directory
   * @return Seq[String] subdirectories
   */
  def listDirectories(parent: String): Seq[String] = {
    val fs = buildFileSystem()
    try {
      val targetPath = new Path(rootUrl, parent)
      fs.listStatus(targetPath).filter(_.isDirectory).map(_.getPath.getName)
    } catch {
      case _: Throwable => Seq.empty[String]
    } finally {
      fs.close()
    }
  }

  /**
   * list files for the given parent
   * @param parent parent directory
   * @return Seq[String] files
   */
  def listFiles(parent: String): Seq[String] = {
    val fs = buildFileSystem()
    try {
      val targetPath = new Path(rootUrl, parent)
      fs.listStatus(targetPath).filter(_.isFile).map(_.getPath.getName)
    } catch {
      case _: Throwable => Seq.empty[String]
    } finally  {
      fs.close()
    }
  }

  /**
   * save the given content to file
   * @param filePath file path
   * @param content content to save
   * @param ifNotExist create file it not exist
   */
  def saveFile(filePath: String, content: String, ifNotExist: Boolean = true): Unit = {
    val fs = buildFileSystem()
    try {
      val targetFile = new Path(rootUrl, filePath)
      if (fs.exists(targetFile) && !ifNotExist) {
        throw new RuntimeException(s"file : $filePath is existing")
      }

      val writer = new BufferedWriter(new OutputStreamWriter(fs.create(targetFile)));
      writer.write(content);
      writer.close();
    } finally {
      fs.close()
    }
  }

  /**
   * read file
   * @param filePath file path
   * @return String
   */
  def readFile(filePath: String): String = {
    val fs = buildFileSystem()
    try {
      val targetFile = new Path(rootUrl, filePath)
      val sb = new StringBuffer()
      if (!fs.exists(targetFile)) {
        throw new FileNotFoundException(s"file : $filePath is not found")
      }

      val br = new BufferedReader(new InputStreamReader(fs.open(targetFile)))
      var line: String = br.readLine()
      while (line != null) {
        sb.append(line + "\n")
        line = br.readLine()
      }

      sb.toString
    } finally {
      fs.close()
    }
  }

  /**
   * delete directory
   * @param dir
   */
  def deleteDirectory(dir: String): Unit = {
    val fs = buildFileSystem()
    try {
      val deleting = new Path(rootUrl, dir)
      fs.delete(deleting, true)
    } finally {
      fs.close()
    }
  }

  /**
   * delete file
   * @param file
   */
  def deleteFile(file: String): Unit = {
    val fs = buildFileSystem()
    try {
      val deleting = new Path(rootUrl, file)
      fs.delete(deleting, true)
    } finally {
      fs.close()
    }
  }
}
