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

// package com.zetaris.lightning.catalog.api

// import org.apache.spark.sql.SparkSession
// import org.glassfish.jersey.jetty.JettyHttpContainerFactory
// import org.glassfish.jersey.server.ResourceConfig
// import org.slf4j.LoggerFactory

// import javax.ws.rs.core.UriBuilder

// object LightningAPIServer {
//   val API_PORT_KEY = "lightning.server.port"
//   val defaultPort = 8080
//   val LOGGER = LoggerFactory.getLogger(getClass)

//   val spark: SparkSession = SparkSession.builder()
//     .getOrCreate()


//   def main(args: Array[String]): Unit = {
//     LOGGER.info("Starting Lightning API server...")

//     val config = new ResourceConfig(
//       classOf[CORSFilter],
//       classOf[LightningResource]
//     )

//     val serverPort = spark.sparkContext.getConf.getInt(API_PORT_KEY, defaultPort)

//     val baseUri = UriBuilder.fromUri("http://localhost/").port(serverPort).build()
//     val server = JettyHttpContainerFactory.createServer(baseUri, config)

//     server.start()

//     Runtime.getRuntime().addShutdownHook( new Thread() {
//       override def run(): Unit = {
//         println("Shuttding down API server")
//         server.stop()
//       }
//     })

//     Thread.currentThread().join()

//   }
// }

package com.zetaris.lightning.catalog.api

import org.apache.spark.sql.SparkSession
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{ContextHandler, ResourceHandler, HandlerList}
import org.glassfish.jersey.jetty.JettyHttpContainerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.LoggerFactory
import java.nio.file.Paths

import javax.ws.rs.core.UriBuilder
import org.apache.spark.sql.SparkSession

object LightningAPIServer {
  val LOGGER = LoggerFactory.getLogger(getClass)
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  def main(args: Array[String]): Unit = {
    LOGGER.info("Starting Lightning API and GUI servers...")

    // API Server Configuration
    val serverPort = sys.env.getOrElse("LIGHTNING_SERVER_PORT", "8080").toInt
    // val apiBaseUri = UriBuilder.fromUri("http://localhost/").port(serverPort).build()
    val apiBaseUri = UriBuilder.fromUri("http://0.0.0.0/").host("localhost").port(serverPort).build()

    
    // Jersey configuration for API
    val config = new ResourceConfig(
      classOf[CORSFilter],
      classOf[LightningResource]
    )
    val apiServer = JettyHttpContainerFactory.createServer(apiBaseUri, config)
    apiServer.start()
    LOGGER.info(s"API Server started on port $serverPort")

    // GUI Server Configuration
    val guiPort = sys.env.getOrElse("LIGHTNING_GUI_PORT", "3000").toInt
    val guiServer = new Server(guiPort)
    val staticHandler = new ResourceHandler()
    staticHandler.setDirectoriesListed(true)
    
    val jarLocation = Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).getParent.toString
    val webDir = Paths.get(jarLocation, "../web").toString
    staticHandler.setResourceBase(webDir)

    val guiContext = new ContextHandler()
    guiContext.setContextPath("/")
    guiContext.setHandler(staticHandler)
    guiContext.setVirtualHosts(Array("localhost", "0.0.0.0"))
    
    guiServer.setHandler(guiContext)
    guiServer.start()
    LOGGER.info(s"GUI Server started. Access the GUI at http://localhost:$guiPort")

    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = {
        LOGGER.info("Shutting down API and GUI servers")
        apiServer.stop()
        guiServer.stop()
      }
    })

    apiServer.join()
    guiServer.join()
  }
}
