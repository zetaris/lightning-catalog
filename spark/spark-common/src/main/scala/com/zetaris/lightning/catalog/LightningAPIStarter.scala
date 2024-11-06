package com.zetaris.lightning.catalog

import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.jetty.JettyHttpContainerFactory
import javax.ws.rs.core.UriBuilder
import org.eclipse.jetty.server.HttpConnectionFactory
import org.slf4j.LoggerFactory
import scala.jdk.CollectionConverters._
import com.zetaris.lightning.util.CORSFilter

object LightningAPIStarter {
  val LOGGER = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    LOGGER.info("Starting REST services...")

    val config = new ResourceConfig(
      classOf[CORSFilter],
      classOf[LightningResource],
    //   classOf[UserResource],
    //   classOf[TrustStoreResource],
    //   classOf[AuthenticationResource],
    //   classOf[ExportResource]
    )

    val baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build()
    val server = JettyHttpContainerFactory.createServer(baseUri, config)

    server.getConnectors
      .flatMap(_.getConnectionFactories().asScala)
      .foreach {
        case httpConn: HttpConnectionFactory => httpConn.getHttpConfiguration.setSendServerVersion(false)
      }

    server.start()
    LOGGER.info("REST API server started at http://localhost:8080/")
  }
}
