package com.zetaris.lightning.catalog

import org.apache.spark.sql.SparkSession
import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response}
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.{File, PrintWriter}
import scala.io.Source
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import com.zetaris.lightning.parser.LightningParserExtension
import com.zetaris.lightning.execution.command.{CompileUSLSpec, CreateTableSpec, ActivateUSLTableSpec, LoadUSL, UpdateUSL}
import com.zetaris.lightning.model.serde.UnifiedSemanticLayer
import com.zetaris.lightning.parser.LightningExtendedParser
import com.zetaris.lightning.model.LightningModelFactory
import com.zetaris.lightning.execution.command.LightningCommandBase

// SparkSession Init
@Path("/api")
class LightningResource {
  val LOGGER = LoggerFactory.getLogger(getClass)

  // SparkSession Settings
  val spark: SparkSession = SparkSession.builder()
    .appName("LightningCatalogApp")
    .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
    .config("spark.sql.extensions", "com.zetaris.lightning.spark.LightningSparkSessionExtension")
    .config("spark.sql.catalog.lightning", "com.zetaris.lightning.catalog.LightningCatalog")
    .config("spark.sql.catalog.lightning.type", "hadoop")
    // .config("spark.sql.catalog.lightning.warehouse", "/tmp/ligt-model")
    .config("spark.sql.catalog.lightning.warehouse", "./../../env/ligt-model")
    .config("spark.sql.catalog.lightning.accessControlProvider", "com.zetaris.lightning.analysis.NotAppliedAccessControlProvider")
    .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
    .enableHiveSupport()
    .getOrCreate()

  // Jackson ObjectMapper for JSON conversion
  val mapper = new ObjectMapper()
  // Directory to save the semantic layer info files
  // val envFilesDir = Paths.get("./../../env/").toAbsolutePath.toString + "/"
  val uslFilesDir = Paths.get("./../../env/ligt-model/metastore/usl/").toAbsolutePath.toString + "/"
  val envFilesDir = Paths.get("./../../env/ligt-model/").toAbsolutePath.toString + "/"
  // Ensure the directory exists
  new File(uslFilesDir).mkdirs()
  new File(envFilesDir).mkdirs()

  // SQL query execute endpoint
  @POST
  @Path("/query")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def executeQuery(queryString: String): Response = {
    try {
      LOGGER.info(s"Executing query: $queryString")
      val df = spark.sql(queryString)

      if (df == null) {
        throw new RuntimeException("Query execution returned null DataFrame.")
      }

      // Convert DataFrame rows to a JSON array
      val resultJson = df.toJSON.collect()

      // Serialize the array of JSON strings
      val jsonResponse = mapper.writeValueAsString(resultJson)

      LOGGER.info(s"Query result: $jsonResponse")
      Response.ok(jsonResponse).build()
    } catch {
      case sparkException: Exception =>
        LOGGER.error("Spark error occurred", sparkException)

        // Escape special characters like newlines and double quotes
        val errorMessage = Option(sparkException.getMessage).getOrElse("An unknown error occurred.")
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
    }
  }

  // API to retrieve saved semantic layer info from the file system
  @POST
  @Path("/get-semantic-layer")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getSemanticLayer(payload: String): Response = {
    try {
      // Parse the incoming JSON payload to extract the fileName
      val jsonNode = mapper.readTree(payload)
      val fileName = jsonNode.get("fileName").asText()

      // Ensure the file has a .json extension
      val correctedFileName = if (fileName.endsWith(".json")) fileName else s"$fileName.json"

      LOGGER.info(s"Retrieving semantic layer info from $correctedFileName")

      val filePath = s"$uslFilesDir$correctedFileName"
      val file = new File(filePath)

      if (!file.exists()) {
        throw new RuntimeException(s"File not found: $correctedFileName")
      }

      // Read the file content and return it as a JSON string
      val fileContent = Source.fromFile(filePath).mkString
      Response.ok(fileContent).build()
    } catch {
      case ex: Exception =>
        LOGGER.error("Failed to retrieve semantic layer info", ex)

        val errorResponse = s"""{
          "error": "Failed to retrieve semantic layer",
          "message": "${ex.getMessage}"
        }"""

        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
    }
  }

  // API to save semantic layer info to the file system
  @POST
  @Path("/save-semantic-layer")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def saveSemanticLayer(jsonString: String, @QueryParam("fileName") fileNameParam: String): Response = {
    try {
      LOGGER.info(s"Saving semantic layer info")

      // Parse the JSON string to check validity
      val jsonData = mapper.readTree(jsonString)

      // Check if fileName is provided, if not, generate it
      val fileName = if (Option(fileNameParam).exists(_.trim.nonEmpty)) {
        val trimmedFileName = fileNameParam.trim
        // Check if the fileName already ends with .json, if not, append .json
        if (trimmedFileName.endsWith(".json")) trimmedFileName else s"$trimmedFileName.json"
      } else {
        val dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss")
        val timestamp = dateFormat.format(new Date())
        s"semantic_layer_$timestamp.json"
      }

      val filePath = s"$uslFilesDir/$fileName"

      // Write the JSON content to a file
      val writer = new PrintWriter(new File(filePath))
      writer.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData))
      writer.close()

      LOGGER.info(s"Semantic layer info saved to $filePath")

      val responseJson = s"""{
        "message": "Semantic layer saved successfully",
        "file": "$filePath"
      }"""

      Response.ok(responseJson).build()
    } catch {
      case ex: Exception =>
        LOGGER.error("Failed to save semantic layer info", ex)

        val errorResponse = s"""{
          "error": "Failed to save semantic layer",
          "message": "${ex.getMessage}"
        }"""

        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
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
  @Path("/parse-ddl")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def parseDDL(ddlQuery: String): Response = {
    try {
      LOGGER.info(s"Parsing DDL query: $ddlQuery")

      // Extending the SQL parser of SparkSession to parse DDL statements
      val parser = new LightningExtendedParser(spark.sessionState.sqlParser)

      // Parsing the DDL statement with the parser and converting it into a LogicalPlan
      val logicalPlan = parser.parseLightning(ddlQuery)

      // Extracting table information from the DDL statement and converting it to JSON
      val createTableSpec = logicalPlan.asInstanceOf[CreateTableSpec]

      // Call the updated `toJson` method with the new signature
      val jsonResult = UnifiedSemanticLayer.toJson(
        namespace = Seq("namespace_placeholder"), 
        name = "ddl_output", 
        tables = Seq(createTableSpec)
      )

      // Returning the JSON result to the client
      Response.ok(jsonResult).build()

    } catch {
      case ex: Exception =>
        LOGGER.error("Failed to parse DDL", ex)
        val errorResponse = s"""{
          "error": "Failed to parse DDL",
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
  @Path("/load-usl")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def loadUSL(uslData: String): Response = {
    try {
      LOGGER.info(s"Received USL load request: $uslData")
      val mapper = new ObjectMapper()
      val jsonNode = mapper.readTree(uslData)
      val namespace = jsonNode.get("namespace").asText().split("\\.").toSeq
      val name = jsonNode.get("name").asText()

      LOGGER.info(s"Loading USL: $name in namespace: ${namespace.mkString(".")}")

      // Load USL using LoadUSL class
      val loadUSLSpec = LoadUSL(namespace = namespace, name = name)

      // Execute and return the result
      val result = loadUSLSpec.runCommand(spark)
      val jsonResult = result.head.getString(0)
      Response.ok(jsonResult).build()

    } catch {
      case ex: IllegalArgumentException =>
        LOGGER.error("Invalid load request", ex)
        Response.status(Response.Status.BAD_REQUEST)
          .entity(s"""{"error": "Invalid load request", "message": "${ex.getMessage}"}""")
          .build()

      case ex: Exception =>
        LOGGER.error("Failed to load USL", ex)
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(s"""{"error": "Failed to load USL", "message": "${ex.getMessage}"}""")
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
  
}
