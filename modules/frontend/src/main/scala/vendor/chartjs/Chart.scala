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

package oxrt.frametimes.vendor.chartjs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

import org.scalajs.dom

// Copied from https://github.com/raquo/laminar-full-stack-demo/blob/master/client/src/main/scala/vendor/chartjs/Chart.scala
// This Scala.js import:
//
//     @js.native
//     @JSImport("chart.js")
//     class Chart() extends js.Object
//
// is equivalent to the following JS import:
//
//     import { Chart } from "chart.js"
//
// where `Chart` is implied from the Scala class/object name that follows.
//
// If instead you wanted to use a different name in Scala for the imported class:
//
//     import { Chart => JsChart } from "chart.js"
//
// you would instead say:
//
//     @js.native
//     @JSImport("chart.js", "Chart")
//     class JsChart() extends js.Object
//
// See https://www.scala-js.org/doc/interoperability/facade-types.html for more details.

// BEGIN[wind-gradient]
@js.native
@JSImport("chart.js")
class Chart(
    val canvas: dom.HTMLCanvasElement | dom.CanvasRenderingContext2D,
    val config: ChartConfig
) extends js.Object:

  /** Call this after mutating chart config, for it to take effect. */
  def update(): Unit = js.native

  def destroy(): Unit = js.native

@js.native
@JSImport("chart.js")
object Chart extends js.Object:

  def defaults: ChartConfigOptions = js.native

  // Can accept: chart.js controllers, elements, plugins
  def register(components: js.Object*): Unit = js.native

  def unregister(components: js.Object*): Unit = js.native
// END[wind-gradient]

@js.native
@JSImport("chart.js")
object Colors extends js.Object

@js.native
@JSImport("chart.js")
object BarController extends js.Object

// BEGIN[wind-gradient]
@js.native
@JSImport("chart.js")
object LineController extends js.Object

@js.native
@JSImport("chart.js")
object CategoryScale extends js.Object

@js.native
@JSImport("chart.js")
object LinearScale extends js.Object
// END[wind-gradient]

@js.native
@JSImport("chart.js")
object BarElement extends js.Object

@js.native
@JSImport("chart.js")
object LineElement extends js.Object

@js.native
@JSImport("chart.js")
object PointElement extends js.Object

@js.native
@JSImport("chart.js")
object Legend extends js.Object

@js.native
@JSImport("chart.js")
object Tooltip extends js.Object
