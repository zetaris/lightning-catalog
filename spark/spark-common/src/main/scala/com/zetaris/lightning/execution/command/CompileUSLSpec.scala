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

package com.zetaris.lightning.execution.command

import com.zetaris.lightning.model.{LightningModelFactory, ReferenceColumnFoundException, ReferenceTableFoundException, TableDuplicatedException}
import com.zetaris.lightning.model.serde.UnifiedSemanticLayer
import com.zetaris.lightning.parser.LightningExtendedParser
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.{Row, SparkSession}

case class CompileUSLSpec(name: String,
                          deploy: Boolean,
                          ifNotExit: Boolean,
                          namespace: Seq[String],
                          inputDDLs: String) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("json", StringType, false)()
  )

  private def checkTableDuplication(createTableSpecs: Seq[CreateTableSpec]): Unit = {
    val duplications = scala.collection.mutable.ArrayBuffer.empty[String]
    createTableSpecs.groupBy(_.name).foreach { grouped =>
      if(grouped._2.size > 1) {
        duplications += grouped._1
      }
    }

    if (duplications.nonEmpty) {
      throw TableDuplicatedException(s"duplicated tables : ${duplications.mkString(",")}")
    }
  }

  private def checkReferenceTableColumn(createTableSpecs: Seq[CreateTableSpec], foreignKey: ForeignKey): Unit = {

    foreignKey.refTable.zip(foreignKey.refColumns).foreach { parent =>
      val parentTableSpec = createTableSpecs.find(_.name.equalsIgnoreCase(parent._1)).getOrElse(
        throw ReferenceTableFoundException(s"${parent._1} is not found")
      )

      parentTableSpec.columnSpecs.find(_.name.equalsIgnoreCase(parent._2)).getOrElse(
        throw ReferenceColumnFoundException(s"${parent._1}.${parent._2} is not found")
      )

    }
  }

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))
    val withoutPrefix = namespace.drop(1)

    validateNamespace(model, withoutPrefix.toArray)

    val parser = new LightningExtendedParser(sparkSession.sessionState.sqlParser)
    val createTableSpecs = inputDDLs.split(";.*?\\n").map { ddl =>
      val createTableSpec = parser.parseLightning(ddl).asInstanceOf[CreateTableSpec]
      createTableSpec.copy(namespace = namespace :+ name)
    }

    checkTableDuplication(createTableSpecs)
    val createTableSpecWithFqnTable = createTableSpecs.map { tableSpec =>
      val fkWithFqn = tableSpec.foreignKeys.map { fk =>
        checkReferenceTableColumn(createTableSpecs, fk)
        val fqnParentTable = fk.refTable.map(pt => s"${toFqn(namespace)}.$name.$pt")
        fk.copy(refTable = fqnParentTable)
      }

      val tableSpecWithFqn = tableSpec.copy(foreignKeys = fkWithFqn)

      val columnSpecWithFqn = tableSpecWithFqn.columnSpecs.map { cs =>
        if (cs.foreignKey.isDefined) {
          checkReferenceTableColumn(createTableSpecs, cs.foreignKey.get)
          val fk = cs.foreignKey.get
          val fqnParentTable = fk.refTable.map(pt =>  s"${toFqn(namespace)}.$name.$pt" )
          val fkWithFqn = fk.copy(refTable = fqnParentTable)
          cs.copy(foreignKey = Some(fkWithFqn))
        } else {
          cs
        }
      }

      tableSpecWithFqn.copy(columnSpecs = columnSpecWithFqn)
    }


    if (deploy) {
      model.saveUnifiedSemanticLayer(withoutPrefix, name, createTableSpecWithFqnTable)
    }

    val json = UnifiedSemanticLayer.toJson(withoutPrefix, name, createTableSpecWithFqnTable)
    Row(json) :: Nil
  }
}
