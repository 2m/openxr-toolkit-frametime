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

import scala.util.Try

case class Stats(appCpu: Int, renderCpu: Int, appGpu: Int)

def parseCsv(csv: String) =
  val contents = csv.split("\n")
  val header = contents(0).split(",")

  val (appCpuIndex, renderCpuIndex, appGpuIndex) =
    (
      header.indexWhere(Seq("appCPU (us)", "App CPU (µs)").contains(_)),
      header.indexWhere(Seq("renderCPU (us)", "Render CPU (µs)").contains(_)),
      header.indexWhere(Seq("appGPU (us)", "Render GPU (µs)").contains(_))
    )

  contents.tail
    .flatMap: dataLine =>
      val data = dataLine.split(",")

      val appCpu = data(appCpuIndex)
      val renderCpu = data(renderCpuIndex)
      val appGpu = data(appGpuIndex)

      val appCpuMicros = Try(Some(appCpu.toInt)).getOrElse(None)
      val renderCpuMicros = Try(Some(renderCpu.toInt)).getOrElse(None)
      val appGpuMicros = Try(Some(appGpu.toInt)).getOrElse(None)

      for
        a <- appCpuMicros
        c <- renderCpuMicros
        g <- appGpuMicros
      yield Some(Stats(a, c, g))
    .flatten
