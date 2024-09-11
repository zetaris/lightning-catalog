package com.zetaris.lightning.catalog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.http.scaladsl.model.HttpResponse
import org.apache.spark.sql.SparkSession
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ServerSettings
import akka.actor.CoordinatedShutdown
import akka.Done

object LightningEndPoints {

  // Akka system and materializer initialization
  implicit val system: ActorSystem = ActorSystem("LightningSystem")
  implicit val materializer: Materializer = Materializer(system)

  // Initialize SparkSession once and reuse it
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

  def main(args: Array[String]): Unit = {

    // Start the API server for query execution
    startApiServer()

    // Keep the application running
    println("API server is running. Press RETURN to stop...")
    StdIn.readLine()

    // Stop the SparkSession and Akka system when done
    spark.stop()
    system.terminate()
  }

  def startApiServer(): Unit = {
    // Define HTTP route for query execution
    val route: Route =
      path("query" / Segment) { queryString =>
        get {
          complete {
            Future {
              try {
                // Execute the query using Spark and return results
                val result = spark.sql(queryString).collect().map(_.toString).mkString("\n")

                // Return the result to the client
                HttpResponse(entity = result)
              } catch {
                case e: Exception =>
                  HttpResponse(entity = s"Error executing query: ${e.getMessage}")
              }
            }
          }
        }
      }

    // Prevent Akka from shutting down unexpectedly by properly handling CoordinatedShutdown
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceUnbind, "unbind-http") { () =>
      Future.successful(Done)
    }

    // Start the HTTP server
    val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(route)

    bindingFuture.onComplete {
      case scala.util.Success(binding) =>
        val address = binding.localAddress
        println(s"API Server online at http://${address.getHostString}:${address.getPort}/")

      case scala.util.Failure(ex) =>
        println(s"Failed to bind HTTP endpoint, terminating system: ${ex.getMessage}")
        system.terminate()
    }
  }
}
