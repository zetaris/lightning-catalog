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
      LOGGER.info(s"query : $query")
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
}
