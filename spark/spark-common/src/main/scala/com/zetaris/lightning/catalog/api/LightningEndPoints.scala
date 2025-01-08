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
import org.apache.spark.sql.catalyst.util.{DateFormatter, TimestampFormatter}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SparkDateTimeUtils, SparkSession, UDTUtils}
import org.apache.spark.unsafe.types.CalendarInterval
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods.compact
import org.json4s.{JArray, JBool, JDecimal, JDouble, JField, JLong, JNull, JObject, JString}
import org.slf4j.LoggerFactory

import java.io._
import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate, LocalDateTime}
import java.util
import java.util.Base64
import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response, StreamingOutput}
import scala.collection.mutable
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
      val itr = df.toLocalIterator()

      new StreamingOutput() {
        override def write(output: OutputStream): Unit = {
          val writer = new BufferedWriter(new OutputStreamWriter(output))
          writer.write("[")
          while(itr.hasNext) {
            val json = rowToJson(itr.next())
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
        LOGGER.error(e.getMessage, e)
        buildErrorMessage(e)
      case Success(stream) => Response.ok(stream).build()
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
      val itr = df.toLocalIterator()
      var recCount = 0
      new StreamingOutput() {
        override def write(output: OutputStream): Unit = {
          val writer = new BufferedWriter(new OutputStreamWriter(output))
          var keepGoing = true
          writer.write("[")
          while(itr.hasNext && keepGoing) {
            val json = rowToJson(itr.next())
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
        LOGGER.error(e.getMessage, e)
        buildErrorMessage(e)
      case Success(stream) => Response.ok(stream).build()
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
      val itr = df.toLocalIterator()
      var recCount = 0
      new StreamingOutput() {
        override def write(output: OutputStream): Unit = {
          val writer = new BufferedWriter(new OutputStreamWriter(output))
          while(itr.hasNext) {
            val json = rowToJson(itr.next())
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
        LOGGER.error(e.getMessage, e)
        buildErrorMessage(e)
      case Success(stream) => Response.ok(stream).build()
    }
  }

  def buildErrorMessage(th: Throwable): Response = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    th.printStackTrace(pw)

    val errorMessage = new util.HashMap[String, String]()
    errorMessage.put("error", "Spark execution error")
    errorMessage.put("message", sw.toString)

    val payload = new ObjectMapper().writeValueAsString(errorMessage)
    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity(payload)
      .build()

  }

  // https://issues.apache.org/jira/browse/SPARK-50364
  def rowToJson(row: Row): String = {
    require(row.schema != null, "JSON serialization requires a non-null schema.")

    lazy val zoneId = SparkDateTimeUtils.getZoneId(spark().sqlContext.getConf("spark.sql.session.timeZone"))
    lazy val dateFormatter = DateFormatter()
    lazy val timestampFormatter = TimestampFormatter(zoneId)

    // Convert an iterator of values to a json array
    def iteratorToJsonArray(iterator: Iterator[_], elementType: DataType): JArray = {
      JArray(iterator.map(toJson(_, elementType)).toList)
    }

    // Convert a value to json.
    def toJson(value: Any, dataType: DataType): JValue = (value, dataType) match {
      case (null, _) => JNull
      case (b: Boolean, _) => JBool(b)
      case (b: Byte, _) => JLong(b)
      case (s: Short, _) => JLong(s)
      case (i: Int, _) => JLong(i)
      case (l: Long, _) => JLong(l)
      case (f: Float, _) => JDouble(f)
      case (d: Double, _) => JDouble(d)
      case (d: BigDecimal, _) => JDecimal(d)
      case (d: java.math.BigDecimal, _) => JDecimal(d)
      case (d: Decimal, _) => JDecimal(d.toBigDecimal)
      case (s: String, _) => JString(s)
      case (b: Array[Byte], BinaryType) =>
        JString(Base64.getEncoder.encodeToString(b))
      case (d: LocalDate, _) => JString(dateFormatter.format(d))
      case (d: Date, _) => JString(dateFormatter.format(d))
      case (i: Instant, _) => JString(timestampFormatter.format(i))
      case (t: Timestamp, _) => JString(timestampFormatter.format(t))
      case (d: LocalDateTime, _) => JString(timestampFormatter.format(d))
      case (i: CalendarInterval, _) => JString(i.toString)
      case (a: Array[_], ArrayType(elementType, _)) =>
        iteratorToJsonArray(a.iterator, elementType)
      case (a: mutable.ArraySeq[_], ArrayType(elementType, _)) =>
        iteratorToJsonArray(a.iterator, elementType)
      case (s: Seq[_], ArrayType(elementType, _)) =>
        iteratorToJsonArray(s.iterator, elementType)
      case (m: Map[String @unchecked, _], MapType(StringType, valueType, _)) =>
        new JObject(m.toList.sortBy(_._1).map {
          case (k, v) => k -> toJson(v, valueType)
        })
      case (m: Map[_, _], MapType(keyType, valueType, _)) =>
        new JArray(m.iterator.map {
          case (k, v) =>
            new JObject("key" -> toJson(k, keyType) :: "value" -> toJson(v, valueType) :: Nil)
        }.toList)
      case (row: Row, schema: StructType) =>
        var n = 0
        val elements = new mutable.ListBuffer[JField]
        val len = row.length
        while (n < len) {
          val field = schema(n)
          elements += (field.name -> toJson(row(n), field.dataType))
          n += 1
        }
        new JObject(elements.toList)
      case (v: Any, udt: UserDefinedType[Any @unchecked]) =>
        toJson(UDTUtils.toRow(v, udt), udt.sqlType)
      case _ =>
        throw new IllegalArgumentException(s"Failed to convert value $value " +
          s"(class of ${value.getClass}}) with the type of $dataType to JSON.")
    }
    val jValue = toJson(row, row.schema)
    compact(jValue)
  }
}
