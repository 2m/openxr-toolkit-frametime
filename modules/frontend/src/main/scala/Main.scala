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

package oxrt.frametimes

import scala.annotation.tailrec
import scala.math.BigDecimal.RoundingMode
import scala.scalajs.js

import com.raquo.laminar.api.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom
import org.scalajs.dom.File
import org.scalajs.dom.FileReader
import org.scalajs.dom.HTMLInputElement
import oxrt.frametimes.vendor.chartjs.*

@main
def main() =
  Chart.register(
    ChartAnnotationPlugin,
    Legend,
    LineController,
    LineElement,
    PointElement,
    CategoryScale,
    LinearScale,
    Tooltip
  )
  renderOnDomContentLoaded(dom.document.querySelector("#app"), App.app)

object App:
  val model = new Model
  import model.*

  val statsFile = Var(Option.empty[File])

  val BucketSizeMicros = 100
  val MaxFrametimeMicros = 16_000

  val app = div(
    input(`type` := "file", onChange --> { evt => readSingleFile(evt.target.asInstanceOf[HTMLInputElement].files(0)) }),
    renderDataChart()
  )

  def readSingleFile(file: File) =
    val reader = new FileReader()
    reader.onload = e =>
      val contents = e.target.asInstanceOf[FileReader].result.asInstanceOf[String]
      val data = parseCsv(contents)

      chartHistogram(data.map(_.appCpu), DataItemKind.AppCpu)
      chartHistogram(data.map(_.renderCpu), DataItemKind.RenderCpu)
      chartHistogram(data.map(_.appGpu), DataItemKind.AppGpu)

    reader.readAsText(file)

  def chartHistogram(data: Array[Int], kind: DataItemKind) =
    println(s"Crunching $kind data")

    val hist = Timed(s"histogram"):
      histogram(data.toList)

    Timed(s"data add"):
      addDataItem(DataItem(DataItemID(), kind, 0, 0))
      hist
        .foreach: (bucket, count) =>
          addDataItem(
            DataItem(
              DataItemID(),
              kind,
              BigDecimal(bucket) / BigDecimal(1000),
              BigDecimal(count * 100) / BigDecimal(data.size)
            )
          )

  def histogram(allData: List[Int]): List[(Int, Int)] =
    @tailrec def getBucket(maxMicros: Int, accu: List[(Int, Int)], data: List[Int]): List[(Int, Int)] =
      data match
        case data if maxMicros > MaxFrametimeMicros =>
          println(s"Dropping frametimes, as they were more than 16ms: $data")
          accu
        case Nil => accu
        case data =>
          val (bucket, rest) = data.partition(_ < maxMicros)
          getBucket(maxMicros + BucketSizeMicros, accu :+ (maxMicros, bucket.size), rest)

    getBucket(BucketSizeMicros, Nil, allData)

  type ChartDataType = js.Array[js.Object]
  type ChartLabelType = String

  val chartConfig =
    import typings.chartJs.chartJsStrings
    import typings.chartJs.distTypesIndexMod.*

    def dataset[T](
        datasetLabel: String,
        color: String,
        datasetHidden: Boolean = false
    ): ChartDataset[T, ChartDataType] =
      new js.Object:
        val label = datasetLabel
        val hidden = datasetHidden
        val borderWidth = 1
        val borderColor = color
        val backgroundColor = color
        val fill = false
      .asInstanceOf[ChartDataset[T, ChartDataType]]

    def options[T](): ChartOptions[T] =
      new js.Object:
        val scales = new js.Object:
          val y = new js.Object:
            val title = new js.Object:
              val display = true
              val text = "Percentage of all measurements"
            val beginAtZero = true
          val x = new js.Object:
            val title = new js.Object:
              val display = true
              val text = "Frametime in milliseconds"
            val ticks = new js.Object:
              val maxTicksLimit = MaxFrametimeMicros / 1000.0 + 1
        val elements = new js.Object:
          val point = new js.Object:
            val pointStyle = false
        val plugins = new js.Object:
          val annotation = new js.Object:
            val annotations = js.Array(
              new js.Object:
                val `type` = "line"
                val xMin = "11.10"
                val xMax = "11.10"
                val borderDash = js.Array(5, 5)
                val borderWidth = 1
                val label = new js.Object:
                  val display = true
                  val position = "end"
                  val content = "11.11ms (90Hz)"
                  val backgroundColor = "rgba(0,0,0,0.5)"
              ,
              new js.Object:
                val `type` = "line"
                val xMin = "8.30"
                val xMax = "8.30"
                val borderDash = js.Array(5, 5)
                val borderWidth = 1
                val label = new js.Object:
                  val display = true
                  val position = "end"
                  val content = "8.33ms (120Hz)"
                  val backgroundColor = "rgba(0,0,0,0.5)"
            )
      .asInstanceOf[ChartOptions[T]]

    ChartConfiguration[chartJsStrings.line, ChartDataType, ChartLabelType](
      `type` = chartJsStrings.line,
      data = ChartData(
        datasets = js.Array(
          dataset("appCPU", "green", datasetHidden = true),
          dataset("renderCPU", "pink"),
          dataset("appGPU", "blue")
        )
      )
    ).setOptions(options())

  def renderDataChart(): Element =
    import scala.scalajs.js.JSConverters.*
    import typings.chartJs.mod.*
    import typings.chartJs.*

    var optChart: Option[Chart[chartJsStrings.line, ChartDataType, ChartLabelType]] = None

    def toChartPoint(d: DataItem) =
      new js.Object:
        val x = d.bucket.setScale(2, RoundingMode.DOWN).toString
        val y = d.percent.setScale(2, RoundingMode.DOWN).toString

    canvasTag(
      // Regular properties of the canvas
      width := "100%",
      height := "400px",

      // onMountUnmount callback to bridge the Laminar world and the Chart.js world
      onMountUnmountCallback(
        // on mount, create the `Chart` instance and store it in optChart
        mount = nodeCtx =>
          val domCanvas: dom.HTMLCanvasElement = nodeCtx.thisNode.ref
          val chart = Chart[chartJsStrings.line, ChartDataType, ChartLabelType](domCanvas, chartConfig)
          optChart = Some(chart)
        ,
        // on unmount, destroy the `Chart` instance
        unmount = thisNode =>
          for chart <- optChart do chart.destroy()
          optChart = None
      ),

      // Bridge the FRP world of dataSignal to the imperative world of the `chart.data`
      dataSignal --> { data =>
        for chart <- optChart do
          chart.data.labels = (0 to MaxFrametimeMicros by BucketSizeMicros)
            .map(BigDecimal(_))
            .map(_ / 1000.0)
            .map(_.setScale(2, RoundingMode.DOWN))
            .map(_.toString)
            .toJSArray

          chart.data.datasets(0).data = data.filter(_.kind == DataItemKind.AppCpu).map(toChartPoint).toJSArray
          chart.data.datasets(1).data = data.filter(_.kind == DataItemKind.RenderCpu).map(toChartPoint).toJSArray
          chart.data.datasets(2).data = data.filter(_.kind == DataItemKind.AppGpu).map(toChartPoint).toJSArray
          chart.update()
      }
    )

class Timed[T](label: String):
  def apply(thunk: => T): T =
    val time = System.currentTimeMillis
    val res = thunk
    println(s"$label took ${System.currentTimeMillis - time}ms")
    res
