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

package org.apache.spark.sql

import java.util.Random
import scala.util.Try

// copied from org.apache.spark.util.SparkClassUtils
private[spark] trait SparkClassUtils {
  val random = new Random()

  def getSparkClassLoader: ClassLoader = getClass.getClassLoader

  def getContextOrSparkClassLoader: ClassLoader =
    Option(Thread.currentThread().getContextClassLoader).getOrElse(getSparkClassLoader)

  // scalastyle:off classforname
  /**
   * Preferred alternative to Class.forName(className), as well as
   * Class.forName(className, initialize, loader) with current thread's ContextClassLoader.
   */
  def classForName[C](
                       className: String,
                       initialize: Boolean = true,
                       noSparkClassLoader: Boolean = false): Class[C] = {
    if (!noSparkClassLoader) {
      Class.forName(className, initialize, getContextOrSparkClassLoader).asInstanceOf[Class[C]]
    } else {
      Class.forName(className, initialize, Thread.currentThread().getContextClassLoader).
        asInstanceOf[Class[C]]
    }
    // scalastyle:on classforname
  }

  /** Determines whether the provided class is loadable in the current thread. */
  def classIsLoadable(clazz: String): Boolean = {
    Try { classForName(clazz, initialize = false) }.isSuccess
  }

  /**
   * Returns true if and only if the underlying class is a member class.
   *
   * Note: jdk8u throws a "Malformed class name" error if a given class is a deeply-nested
   * inner class (See SPARK-34607 for details). This issue has already been fixed in jdk9+, so
   * we can remove this helper method safely if we drop the support of jdk8u.
   */
  def isMemberClass(cls: Class[_]): Boolean = {
    try {
      cls.isMemberClass
    } catch {
      case _: InternalError =>
        // We emulate jdk8u `Class.isMemberClass` below:
        //   public boolean isMemberClass() {
        //     return getSimpleBinaryName() != null && !isLocalOrAnonymousClass();
        //   }
        // `getSimpleBinaryName()` returns null if a given class is a top-level class,
        // so we replace it with `cls.getEnclosingClass != null`. The second condition checks
        // if a given class is not a local or an anonymous class, so we replace it with
        // `cls.getEnclosingMethod == null` because `cls.getEnclosingMethod()` return a value
        // only in either case (JVM Spec 4.8.6).
        //
        // Note: The newer jdk evaluates `!isLocalOrAnonymousClass()` first,
        // we reorder the conditions to follow it.
        cls.getEnclosingMethod == null && cls.getEnclosingClass != null
    }
  }
}

private[spark] object SparkClassUtils extends SparkClassUtils

