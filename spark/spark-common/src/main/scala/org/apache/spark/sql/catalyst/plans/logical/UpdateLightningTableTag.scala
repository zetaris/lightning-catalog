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

package org.apache.spark.sql.catalyst.plans.logical

import org.apache.spark.sql.catalyst.analysis.AssignmentUtils
import org.apache.spark.sql.catalyst.expressions.Expression

case class UpdateLightningTableTag(table: LogicalPlan,
                              assignments: Seq[Assignment],
                              condition: Option[Expression],
                              rewritePlan: Option[LogicalPlan] = None) extends Command with SupportsSubquery {
  lazy val aligned: Boolean = AssignmentUtils.aligned(table.output, assignments)

  override def children: Seq[LogicalPlan] = if (rewritePlan.isDefined) {
    table :: rewritePlan.get :: Nil
  } else {
    table :: Nil
  }

  override protected def withNewChildrenInternal(newChildren: IndexedSeq[LogicalPlan]): UpdateLightningTableTag = {
    if (newChildren.size == 1) {
      copy(table = newChildren.head, rewritePlan = None)
    } else {
      require(newChildren.size == 2, "UpdateTable expects either one or two children")
      val Seq(newTable, newRewritePlan) = newChildren.take(2)
      copy(table = newTable, rewritePlan = Some(newRewritePlan))
    }
  }
}
