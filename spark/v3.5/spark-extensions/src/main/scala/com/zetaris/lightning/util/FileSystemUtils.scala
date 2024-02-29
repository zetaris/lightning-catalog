/*
 * Copyright 2023 ZETARIS Pty Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.zetaris.lightning.util

import org.apache.commons.io.FileUtils

import java.io.{File, FileFilter}
import java.nio.charset.Charset

object FileSystemUtils {

  def folderExist(folderPath: String): Boolean = new File(folderPath).exists()

  def fileExists(filePath: String): Boolean = folderExist(filePath)

  /**
   * create folder if not exist
   * @param folderPath folderPath
   */
  def createFolderIfNotExist(folderPath: String): Unit = {
    val directory = new File(folderPath)
    if (!directory.exists()) {
      directory.mkdir()
    }
  }

  /**
   * list sub directories for the given parent
   * @param parent parent directory
   * @return Seq[String] subdirectories
   */
  def listDirectories(parent: String): Seq[String] = {
    listFiles(parent, new FileFilter() {
      override def accept(pathname: File): Boolean = pathname.isDirectory
    })
  }

  /**
   * list files for the given parent
   * @param parent parent directory
   * @return Seq[String] files
   */
  def listFiles(parent: String): Seq[String] = {
    listFiles(parent, new FileFilter() {
      override def accept(pathname: File): Boolean = pathname.isFile
    })
  }

  /**
   * list files with the given parent and file filter
   * @param parent
   * @param fileFilter
   * @return
   */
  private def listFiles(parent: String, fileFilter: FileFilter): Seq[String] = {
    val directory = new File(parent)
    if (directory.exists()) {
      directory.listFiles(fileFilter).map(_.getName)
    } else {
      Seq()
    }
  }

  /**
   * save the given content to file
   * @param filePath file path
   * @param content content to save
   * @param ifNotExist create file it not exist
   */
  def saveFile(filePath: String, content: String, ifNotExist: Boolean = true): Unit = {
    val file = new File(filePath)
    if (file.exists() && !ifNotExist) {
      throw new RuntimeException(s"file : $filePath is existing")
    }

    FileUtils.write(file, content, Charset.defaultCharset)
  }

  /**
   * read file
   * @param filePath file path
   * @return String
   */
  def readFile(filePath: String): String = {
    val file = new File(filePath)
    FileUtils.readFileToString(file, Charset.defaultCharset)
  }

  def deleteDirectory(dir: String): Unit = {
    val file = new File(dir)
    FileUtils.deleteDirectory(file)
  }
}
