/*
 * Copyright 2025 github.com/2m/openxr-toolkit-frametimes/contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package oxrt.frametimes.utils

import scala.scalajs.js

// BEGIN[wind-gradient]
trait DynamicJsObject extends js.Object
// END[wind-gradient]

/** These updateDynamic helpers provide an easy way to set rarely used values on JS types that you don't care to type
  * properly.
  *
  * For example, we use one of them to set a custom backgroundColor on ChartDataset. We have alternatives to that:
  *
  * 1) We could add a backgroundColor property to the constructor of ChartDataset, then we can set it like any other
  * parameter.
  *
  * 2) We could create an instance of an anonymous class extending ChartDataset using the `new` keyword, and define the
  * background property just for that class, like so:
  *
  * new ChartDataset(..constructor params..) { val backgroundColor: String = ... }
  */
object DynamicJsObject:

  // #TODO update comments

  // Note: js.Object classes have significant caveats when it comes to
  // overloading methods, so it's best to define unique JSName-s for them
  // to avoid runtime dispatch and surprising behaviour.
  // See https://www.scala-js.org/doc/interoperability/sjs-defined-js-classes.html

  // Note: overloading these methods causes problems with implicit resolution,
  // for example updateDynamic("foo" -> 1, "bar" -> "yes") fails to compile with
  // "None of the overloaded alternatives of method updateDynamic in class ...
  // match arguments", even though commenting out one of them works. I think is
  // because the compiler fails to consider the implicit conversions from `1` and
  // `"yes"` to js.Any in case of overloads. I'm not sure if it is supposed to, but
  // it would have been nice if that worked. #TODO[Scala]

  // BEGIN[wind-gradient]
  extension (obj: DynamicJsObject)

    def updateDynamic(keyValuePairs: (String, js.Any)*): obj.type =
      keyValuePairs.foreach { (key, value) =>
        obj.asInstanceOf[js.Dynamic].updateDynamic(key)(value)
      }
      obj
    // END[wind-gradient]

    // def updateDynamic(key: String, value: js.Any): obj.type = {
    //   obj.asInstanceOf[js.Dynamic].updateDynamic(key)(value)
    //   obj
    // }
    //
    // def updateDynamic(keyValuePair: (String, js.Any)): obj.type = {
    //   obj.asInstanceOf[js.Dynamic].updateDynamic(keyValuePair._1)(keyValuePair._2)
    //   obj
    // }
