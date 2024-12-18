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
import com.zetaris.lightning.execution.command.ShowDataQualityResult
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter, PrintWriter, StringWriter}
import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response, StreamingOutput}
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

      new StreamingOutput() {
        override def write(output: OutputStream): Unit = {
          //val itr = df.toJSON.toLocalIterator()
          val itr = df.toLocalIterator()
          val writer = new BufferedWriter(new OutputStreamWriter(output))
          writer.write("[")
          while(itr.hasNext) {
            val json = itr.next().json
            val jsonStr = mapper.writeValueAsString(json)
            if (itr.hasNext) {
              writer.write(s"$jsonStr,")
            } else {
              writer.write(jsonStr)
            }
          }
          writer.write("]")
          writer.flush()
        }
      }
    } match {
      case Failure(e) =>
        LOGGER.error("Spark error occurred", e)
        val sw = new StringWriter()
        val pw = new PrintWriter(sw)
        e.printStackTrace(pw)

        val errorResponse = s"""{
          "error": "Spark execution error",
          "message": "${sw.toString}"
        }"""

        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
      case Success(stream) =>
        Response.ok(stream).build()
    }
  }

  @GET
  @Path("/qdq")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def queryDQ(@QueryParam("name") name: String,
              @QueryParam("table") table: String,
              @QueryParam("validRecord") validRecord: Boolean,
              @QueryParam("limit") limit: Int): Response = {
    Try {
      LOGGER.info(s"qdq : $name on $table")
      val dq = ShowDataQualityResult(name, table.split("\\."), validRecord, limit)
      val df = dq.runQuery(spark())
      var recCount = 0
      new StreamingOutput() {
        override def write(output: OutputStream): Unit = {
          val itr = df.toLocalIterator()
          val writer = new BufferedWriter(new OutputStreamWriter(output))
          var keepGoing = true
          writer.write("[")
          while(itr.hasNext && keepGoing) {
            val json = itr.next().json
            recCount += 1
            val jsonStr = mapper.writeValueAsString(json)
            if (itr.hasNext) {
              writer.write(s"$jsonStr,")
            } else {
              writer.write(jsonStr)
            }

            if (limit > 0 && recCount >= limit) {
              keepGoing = false
            }
          }
          writer.write("]")
          writer.flush()
        }
      }
    } match {
      case Failure(e) =>
        LOGGER.error("Spark error occurred", e)
        val sw = new StringWriter()
        val pw = new PrintWriter(sw)
        e.printStackTrace(pw)

        val errorResponse = s"""{
          "error": "Spark execution error",
          "message": "${sw.toString}"
        }"""

        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
      case Success(stream) =>
        Response.ok(stream).build()
    }
  }

  @GET
  @Path("/edq")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def exportDQ(@QueryParam("name") name: String,
              @QueryParam("table") table: String,
              @QueryParam("validRecord") validRecord: Boolean): Response = {
    Try {
      LOGGER.info(s"edq : $name on $table")
      val dq = ShowDataQualityResult(name, table.split("\\."), validRecord)
      val df = dq.runQuery(spark())
      var recCount = 0
      new StreamingOutput() {
        override def write(output: OutputStream): Unit = {
          val itr = df.toLocalIterator()
          val writer = new BufferedWriter(new OutputStreamWriter(output))
          while(itr.hasNext) {
            val json = itr.next().json
            recCount += 1
            val jsonStr = mapper.writeValueAsString(json)
            if (itr.hasNext) {
              writer.write(s"$jsonStr,")
            } else {
              writer.write(jsonStr)
            }
          }
          writer.flush()
        }
      }
    } match {
      case Failure(e) =>
        LOGGER.error("Spark error occurred", e)
        val sw = new StringWriter()
        val pw = new PrintWriter(sw)
        e.printStackTrace(pw)

        val errorResponse = s"""{
          "error": "Spark execution error",
          "message": "${sw.toString}"
        }"""

        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(errorResponse)
          .build()
      case Success(stream) =>
        Response.ok(stream).build()
    }
  }

}
