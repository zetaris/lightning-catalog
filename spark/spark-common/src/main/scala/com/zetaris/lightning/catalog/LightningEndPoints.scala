package com.zetaris.lightning.catalog

import org.apache.spark.sql.SparkSession
import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response}
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper

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

  // SQL query execute endpoint
  @POST
  @Path("/query")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def executeQuery(queryString: String): Response = {
    try {
      LOGGER.info(s"Executing query: $queryString")
      val df = spark.sql(queryString)

      // Convert DataFrame rows to a JSON array
      val resultJson = df.toJSON.collect()

      // Serialize the array of JSON strings
      val jsonResponse = mapper.writeValueAsString(resultJson)

      LOGGER.info(s"Query result: $jsonResponse")
      Response.ok(jsonResponse).build()
    } catch {
      case sparkException: Exception =>
        LOGGER.error(s"Spark error: ${sparkException.getMessage}")

        // Escape special characters like newlines and double quotes
        val safeMessage = sparkException.getMessage
          .replace("\"", "'")  // Replace double quotes with single quotes
          .replace("\n", " ")  // Replace newline characters with space
          .replace("\r", "")   // Remove carriage return characters

        // Return structured error response with escaped error message
        val errorResponse = s"""{
          "error": "Spark execution error",
          "message": "$safeMessage"
        }"""
        
        Response.status(Response.Status.OK)
          .entity(errorResponse)
          .build()
    }
  }
  

}
