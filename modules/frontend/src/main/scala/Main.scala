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
import scala.util.Try

import com.raquo.laminar.api.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom
import org.scalajs.dom.File
import org.scalajs.dom.FileReader
import org.scalajs.dom.HTMLInputElement

@main
def main() =
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
      val data = contents
        .split("\n")
        .drop(1)
        .flatMap: dataLine =>
          val Array(_, _, appCPU, renderCPU, appGPU, _, _) = dataLine.split(",")

          val appCpuMicros = Try(Some(appCPU.toInt)).getOrElse(None)
          val renderCpuMicros = Try(Some(renderCPU.toInt)).getOrElse(None)
          val appGpuMicros = Try(Some(appGPU.toInt)).getOrElse(None)

          for
            a <- appCpuMicros
            c <- renderCpuMicros
            g <- appGpuMicros
          yield Some((a, c, g))

      chartHistogram(data.flatten.map(_._1), DataItemKind.AppCpu)
      chartHistogram(data.flatten.map(_._2), DataItemKind.RenderCpu)
      chartHistogram(data.flatten.map(_._3), DataItemKind.AppGpu)

    reader.readAsText(file)

  def chartHistogram(data: Array[Int], kind: DataItemKind) =
    val hist = Timed(s"$kind histogram"):
      histogram(data.toList)

    Timed(s"$kind data add"):
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
        case _ if maxMicros > MaxFrametimeMicros => accu
        case Nil                                 => accu
        case data =>
          val (bucket, rest) = data.partition(_ < maxMicros)
          getBucket(maxMicros + BucketSizeMicros, accu :+ (maxMicros, bucket.size), rest)

    getBucket(BucketSizeMicros, Nil, allData)

  val chartConfig =
    import typings.chartJs.mod.*

    new ChartConfiguration:
      `type` = ChartType.line
      data = new ChartData:
        datasets = js.Array(
          new ChartDataSets:
            label = "appCPU"
            hidden = true
            borderWidth = 1
            borderColor = "green"
            backgroundColor = "green"
            fill = false
          ,
          new ChartDataSets:
            label = "renderCPU"
            borderWidth = 1
            borderColor = "pink"
            backgroundColor = "pink"
            fill = false
          ,
          new ChartDataSets:
            label = "appGPU"
            borderWidth = 1
            borderColor = "blue"
            backgroundColor = "blue"
            fill = false
        )
      options = new ChartOptions:
        scales = new ChartScales:
          yAxes = js.Array(
            new CommonAxe:
              scaleLabel = new ScaleTitleOptions:
                display = true
                labelString = "Percentage of all measurements"
              ticks = new TickOptions:
                beginAtZero = true
          )
          xAxes = js.Array(
            new ChartXAxe:
              scaleLabel = new ScaleTitleOptions:
                display = true
                labelString = "Frametime in milliseconds"
              ticks = new TickOptions:
                maxTicksLimit = MaxFrametimeMicros / 1000.0
          )

  def renderDataChart(): Element =
    import scala.scalajs.js.JSConverters.*
    import typings.chartJs.mod.*

    var optChart: Option[Chart] = None

    def toChartPoint(d: DataItem) =
      val cp = ChartPoint()
      cp.x = d.bucket.setScale(2, RoundingMode.DOWN).toString
      cp.y = d.percent.setScale(2, RoundingMode.DOWN).toString
      cp

    canvasTag(
      // Regular properties of the canvas
      width := "100%",
      height := "400px",

      // onMountUnmount callback to bridge the Laminar world and the Chart.js world
      onMountUnmountCallback(
        // on mount, create the `Chart` instance and store it in optChart
        mount = nodeCtx =>
          val domCanvas: dom.HTMLCanvasElement = nodeCtx.thisNode.ref
          val chart = Chart.apply.newInstance2(domCanvas, chartConfig)
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
          chart.data.labels = (0 to MaxFrametimeMicros by BucketSizeMicros).map(_ / 1000.0).toJSArray

          chart.data.datasets.get(0).data = data.filter(_.kind == DataItemKind.AppCpu).map(toChartPoint).toJSArray
          chart.data.datasets.get(1).data = data.filter(_.kind == DataItemKind.RenderCpu).map(toChartPoint).toJSArray
          chart.data.datasets.get(2).data = data.filter(_.kind == DataItemKind.AppGpu).map(toChartPoint).toJSArray
          chart.update()
      }
    )

class Timed[T](label: String):
  def apply(thunk: => T): T =
    val time = System.currentTimeMillis
    val res = thunk
    println(s"$label took ${System.currentTimeMillis - time}ms")
    res
