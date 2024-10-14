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
    .config("spark.sql.catalog.lightning.warehouse", "/tmp/ligt-model")
    .config("spark.sql.catalog.lightning.accessControlProvider", "com.zetaris.lightning.analysis.NotAppliedAccessControlProvider")
    .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
    .enableHiveSupport()
    .getOrCreate()

  // Jackson ObjectMapper for JSON conversion
  val mapper = new ObjectMapper()
  // Directory to save the semantic layer info files
  val envFilesDir = Paths.get("./../../env/").toAbsolutePath.toString + "/"
  // Ensure the directory exists
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

      val filePath = s"$envFilesDir$correctedFileName"
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

      val filePath = s"$envFilesDir/$fileName"

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

  // API to get the list of files in envFilesDir
  @GET
  @Path("/list-semantic-layers")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def listSemanticLayers(): Response = {
    try {
      LOGGER.info(s"Listing files in directory: $envFilesDir")

      val directory = new File(envFilesDir)
      if (!directory.exists() || !directory.isDirectory) {
        throw new RuntimeException(s"Directory not found or is not a directory: $envFilesDir")
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
      // Converted to CreateTableSpec and then Serialized to JSON
      val createTableSpec = logicalPlan.asInstanceOf[CreateTableSpec]
      val jsonResult = UnifiedSemanticLayer.toJson("ddl_output", Seq("namespace_placeholder"), Seq(createTableSpec))
      
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
  
}
