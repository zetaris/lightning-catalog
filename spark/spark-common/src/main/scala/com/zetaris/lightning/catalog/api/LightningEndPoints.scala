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

package com.zetaris.lightning.catalog.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response}
import scala.util.{Failure, Success, Try}

// SparkSession Init
@Path("/api")
class LightningResource {
  val LOGGER = LoggerFactory.getLogger(getClass)
  val mapper = new ObjectMapper()

  private def spark(): SparkSession = LightningAPIServer.spark

  @POST
  @Path("/q")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def query(query: String): Response = {
    Try {
      val df = spark().sql(query)
      val resultJson = df.toJSON.collect()
      mapper.writeValueAsString(resultJson)
    } match {
      case Failure(e) =>
        LOGGER.error("Spark error occurred", e)

        val errorResponse = s"""{
          "error": "Spark execution error",
          "message": "${e.getMessage}"
        }"""

        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
      case Success(json) =>
        Response.ok(json).build()
    }
  }

/**
  @POST
  @Path("/query")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def executeQuery(queryString: String): Response = {
    Try {
      val df = spark().sql(queryString)
      val resultJson = df.toJSON.collect()
      mapper.writeValueAsString(resultJson)
    } match {
      case Failure(e) =>
        LOGGER.error("Spark error occurred", e)

        val errorMessage = Option(e.getMessage).getOrElse("An unknown error occurred.")
        val safeMessage = errorMessage
          .replace("\"", "'")
          .replace("\n", " ")
          .replace("\r", "")

        val errorResponse = s"""{
          "error": "Spark execution error",
          "message": "$safeMessage"
        }"""

        Response.status(Response.Status.OK)
          .entity(errorResponse)
          .build()
      case Success(json) =>
        Response.ok(json).build()
    }
  }

  @POST
  @Path("/save-semantic-layer")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def saveSemanticLayer(@QueryParam("namespace") namespace: String,
                        @QueryParam("namespace") name: String,
                        ddl: String): Response = {
    Try {
      spark().sql(s"""COMPILE USL IF NOT EXISTS $name DEPLOY NAMESPACE $namespace DDL
          |$ddl
          |""".stripMargin)
        .collect()(0).getString(0)
    } match {
      case Failure(ex) =>
        val errorResponse = s"""{
          "error": "Failed to save semantic layer",
          "message": "${ex.getMessage}"
        }"""

        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
      case Success(json) =>
        Response.ok(json).build()
    }
  }

  // API to get the list of files in uslFilesDir
  @GET
  @Path("/list-semantic-layers")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def listSemanticLayers(): Response = {
    try {
      LOGGER.info(s"Listing files in directory: $uslFilesDir")

      val directory = new File(uslFilesDir)
      if (!directory.exists() || !directory.isDirectory) {
        throw new RuntimeException(s"Directory not found or is not a directory: $uslFilesDir")
      }

      // List files in the directory and filter only .json files
      val files = directory.listFiles().filter(_.isFile).filter(_.getName.endsWith(".json"))

      // Map file names to a JSON array
      val fileNames = files.map(_.getName)
      val jsonResponse = mapper.writeValueAsString(fileNames)

      LOGGER.info(s"Files listed: ${fileNames.mkString(", ")}")

      Response.ok(jsonResponse).build()
    } catch {
      case ex: Exception =>
        LOGGER.error("Failed to list files", ex)

        val errorResponse = s"""{
          "error": "Failed to list semantic layers",
          "message": "${ex.getMessage}"
        }"""

        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
    }
  }

  @POST
  @Path("/compile-usl")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def compileUSL(uslData: String): Response = {
    try {
      LOGGER.info(s"Received USL data: $uslData")
      val mapper = new ObjectMapper()
      val jsonNode = mapper.readTree(uslData)
      var ddlQuery = jsonNode.get("ddl").asText()

      // Validate DDL query
      if (ddlQuery == null || ddlQuery.trim.isEmpty) {
        throw new IllegalArgumentException("DDL query cannot be null or empty.")
      }

      val name = jsonNode.get("name").asText()
      val deploy = jsonNode.get("deploy").asBoolean(false)
      val ifNotExists = jsonNode.get("ifNotExists").asBoolean(false)
      val namespace = jsonNode.get("namespace").asText().split("\\.").toSeq

      LOGGER.info(s"Compiling USL with DDL Query: $ddlQuery")

      // Extract only the DDL part after the command (e.g., after "COMPILE USL IF NOT EXISTS")
      val ddlParts = ddlQuery.split("DDL", 2)  // Split to get the query part after "DDL"
      if (ddlParts.length < 2) {
        throw new IllegalArgumentException("Invalid DDL query format.")
      }
      ddlQuery = ddlParts(1).trim  // Use only the actual query after DDL

      // Compile USL using CompileUSLSpec
      val compileUSLSpec = CompileUSLSpec(
        name = name,
        deploy = deploy,
        ifNotExit = ifNotExists,
        namespace = namespace,
        inputDDLs = ddlQuery
      )

      // Execute and return the result
      val result = compileUSLSpec.runCommand(spark)
      val jsonResult = result.head.getString(0)
      Response.ok(jsonResult).build()

    } catch {
      case ex: IllegalArgumentException =>
        LOGGER.error("Invalid DDL format", ex)
        Response.status(Response.Status.BAD_REQUEST)
          .entity(s"""{"error": "Invalid DDL format", "message": "${ex.getMessage}"}""")
          .build()

      case ex: Exception =>
        LOGGER.error("Failed to compile USL", ex)
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(s"""{"error": "Failed to compile USL", "message": "${ex.getMessage}"}""")
          .build()
    }
  }

  @POST
  @Path("/update-usl")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def updateUSL(uslData: String): Response = {
    try {
      LOGGER.info(s"Received USL update request: $uslData")
      val mapper = new ObjectMapper()
      val jsonNode = mapper.readTree(uslData)
      val namespace = jsonNode.get("namespace").asText().split("\\.").toSeq
      val name = jsonNode.get("name").asText()
      val uslJson = jsonNode.get("uslJson").asText()

      LOGGER.info(s"Updating USL: $name in namespace: ${namespace.mkString(".")}")

      // Update USL using UpdateUSL class
      val updateUSLSpec = UpdateUSL(namespace = namespace, name = name, json = uslJson)

      // Execute and return the result
      val result = updateUSLSpec.runCommand(spark)
      val updatedResult = result.head.getString(0)
      Response.ok(s"""{"updated": "$updatedResult"}""").build()

    } catch {
      case ex: IllegalArgumentException =>
        LOGGER.error("Invalid update request", ex)
        Response.status(Response.Status.BAD_REQUEST)
          .entity(s"""{"error": "Invalid update request", "message": "${ex.getMessage}"}""")
          .build()

      case ex: Exception =>
        LOGGER.error("Failed to update USL", ex)
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(s"""{"error": "Failed to update USL", "message": "${ex.getMessage}"}""")
          .build()
    }
  }

  @POST
  @Path("/activate-usl-table")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def activateUSLTable(uslData: String): Response = {
    try {
      val mapper = new ObjectMapper()
      val jsonNode = mapper.readTree(uslData)

      // tableFull is received in the form "namespace.uslName.tableName"
      val tableFull = jsonNode.get("table").asText()
      val tableParts = tableFull.split("\\.").toSeq

      // Make sure there are at least 3 levels (namespace.uslName.tableName)
      if (tableParts.size < 3) {
        throw new RuntimeException("table name identifier should be at least 3 level")
      }

      val namespace = tableParts.dropRight(2)  // ex) "lightning.metastore.crm"
      val uslName = tableParts.dropRight(1).last  // ex) "USLName"
      val tableName = tableParts.last  // ex) "region_master"
      
      val query = jsonNode.get("query").asText()

      LOGGER.info(s"Activating USL table: ${tableParts.mkString(".")} with query: $query")

      // Activate USL table using ActivateUSLTableSpec
      val activateTableSpec = ActivateUSLTableSpec(tableParts, query)
      val result = activateTableSpec.runCommand(spark)

      val jsonResult = result.map(_.getString(0)).mkString
      Response.ok(jsonResult).build()

    } catch {
      case ex: Exception =>
        LOGGER.error("Failed to activate USL table", ex)
        val errorResponse = s"""{
          "error": "Failed to activate USL table",
          "message": "${ex.getMessage}"
        }"""
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
    }
  }

  // API to delete a specific USL file by fileName
  @POST
  @Path("/delete-usl-table")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def deleteUSLTable(payload: String): Response = {
    try {
      // Parse JSON payload to get the file name
      val jsonNode = mapper.readTree(payload)
      val fileName = jsonNode.get("fileName").asText()

      // Ensure fileName ends with "_table_query.json"
      val correctedFileName = if (fileName.endsWith("_table_query.json")) fileName else s"${fileName}_table_query.json"
      val filePath = s"$uslFilesDir$correctedFileName"

      val file = new File(filePath)
      if (!file.exists()) {
        throw new RuntimeException(s"File not found: $correctedFileName")
      }

      // Attempt to delete the file
      if (file.delete()) {
        LOGGER.info(s"Successfully deleted file: $correctedFileName")
        val successResponse = s"""{
          "message": "File deleted successfully",
          "file": "$correctedFileName"
        }"""
        Response.ok(successResponse).build()
      } else {
        throw new RuntimeException(s"Failed to delete file: $correctedFileName")
      }

    } catch {
      case ex: Exception =>
        LOGGER.error("Failed to delete USL table file", ex)
        val errorResponse = s"""{
          "error": "Failed to delete USL table file",
          "message": "${ex.getMessage}"
        }"""
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
    }
  }

  // API to save content to the file system with specified file name and extension
  @POST
  @Path("/save-to-file")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def saveToFile(jsonString: String, @QueryParam("fileName") fileNameParam: String, @QueryParam("extension") extensionParam: String): Response = {
    try {
      LOGGER.info(s"Saving content to file")

      // Parse the JSON string to check validity
      val jsonData = mapper.readTree(jsonString)

      // Check if fileName and extension are provided, if not, set defaults
      val fileName = if (Option(fileNameParam).exists(_.trim.nonEmpty)) {
        fileNameParam.trim
      } else {
        val dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss")
        val timestamp = dateFormat.format(new Date())
        s"file_$timestamp"
      }

      val extension = if (Option(extensionParam).exists(_.trim.nonEmpty)) {
        extensionParam.trim
      } else {
        "txt"
      }

      val fullFileName = if (fileName.endsWith(s".$extension")) fileName else s"$fileName.$extension"
      val filePath = s"$envFilesDir/$fullFileName"

      // Write the JSON content to a file
      val writer = new PrintWriter(new File(filePath))
      writer.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData))
      writer.close()

      LOGGER.info(s"Content saved to $filePath")

      val responseJson = s"""{
        "message": "Content saved successfully",
        "file": "$filePath"
      }"""

      Response.ok(responseJson).build()
    } catch {
      case ex: Exception =>
        LOGGER.error("Failed to save content to file", ex)

        val errorResponse = s"""{
          "error": "Failed to save content",
          "message": "${ex.getMessage}"
        }"""

        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
    }
  }

  @POST
  @Path("/read-usl-file")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def readUSLFile(payload: String): Response = {
    try {
      // Parse the JSON payload to get the file name
      val jsonNode = mapper.readTree(payload)
      val fileName = jsonNode.get("fileName").asText()

      // Ensure fileName is provided
      if (fileName == null || fileName.trim.isEmpty) {
        throw new IllegalArgumentException("File name must be provided.")
      }

      // Construct full path to the file
      val filePath = s"$uslFilesDir$fileName"

      // Check if the file exists
      val file = new File(filePath)
      if (!file.exists() || !file.isFile) {
        throw new FileNotFoundException(s"File not found: $fileName")
      }

      // Read the file content
      val content = scala.io.Source.fromFile(file).getLines.mkString("\n")

      LOGGER.info(s"Read content from file: $fileName")

      // Return the content as a JSON response
      val responseJson = s"""{
        "fileName": "$fileName",
        "content": ${mapper.writeValueAsString(content)}
      }"""

      Response.ok(responseJson).build()
    } catch {
      case ex: FileNotFoundException =>
        LOGGER.error(s"File not found", ex)  // Removed `fileName` from log to avoid undefined variable error
        Response.status(Response.Status.NOT_FOUND)
          .entity(s"""{"error": "File not found", "message": "${ex.getMessage}"}""")
          .build()
      case ex: Exception =>
        LOGGER.error("Failed to read file content", ex)
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(s"""{"error": "Failed to read file content", "message": "${ex.getMessage}"}""")
          .build()
    }
  }
*/
  }
