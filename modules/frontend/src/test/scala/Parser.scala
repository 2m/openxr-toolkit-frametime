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

class ParserSuite extends munit.FunSuite:
  test("parses openxr-toolkit stats file"):
    val csv = """|time,FPS,appCPU (us),renderCPU (us),appGPU (us),VRAM (MB),VRAM (%)
                 |2025-02-19 18:23:28 +0200,1.0,704578222,0,0,1179,7
                 |2025-02-19 18:23:29 +0200,17.0,13009,957,2074,2157,14""".stripMargin
    val obtained = parseCsv(csv)
    val expected = Array(
      Stats(704578222, 0, 0),
      Stats(13009, 957, 2074)
    )
    assertEquals(obtained.toSeq, expected.toSeq)

  test("parses openxr-toolkit stats file"):
    val csv =
      """|Time (µs),Time (UTC),Time (Local),Frame Interval (µs),FPS,Count,App CPU (µs),Render CPU (µs),Render GPU (µs),Wait CPU (µs),Begin CPU (µs),Submit CPU (µs),VRAM Budget,VRAM Current Usage ,VRAM Current Reserveration,VRAM Available for Reserveration,GPU API,GPU P-State Min,GPU P-State Max,GPU Limit Bits,GPU Thermal Limit,GPU Power Limit,GPU API Limit
         |97986,"2025-02-19T16:25:14.821","2025-02-19T18:25:14.821",10886,91.86110600771633,9,1589,8329,10670,566,81,36,16024338432,8643862528,0,8146386944,NVAPI,0,0,0,false,false,false
         |212983,"2025-02-19T16:25:14.936","2025-02-19T18:25:14.936",11499,86.96408383337682,10,1483,9191,11496,435,72,40,16024338432,8643862528,0,8146386944,NVAPI,0,0,0,false,false,false""".stripMargin
    val obtained = parseCsv(csv)
    val expected = Array(
      Stats(1589, 8329, 10670),
      Stats(1483, 9191, 11496)
    )
    assertEquals(obtained.toSeq, expected.toSeq)
