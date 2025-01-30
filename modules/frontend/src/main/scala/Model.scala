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

import com.raquo.laminar.api.L.*

final class DataItemID

enum DataItemKind:
  case AppCpu, RenderCpu, AppGpu

case class DataItem(id: DataItemID, kind: DataItemKind, bucket: BigDecimal, percent: BigDecimal)

type DataList = List[DataItem]

final class Model:
  val dataVar: Var[DataList] = Var(List())
  val dataSignal = dataVar.signal

  def addDataItem(item: DataItem): Unit =
    dataVar.update(data => data :+ item)

  def removeDataItem(id: DataItemID): Unit =
    dataVar.update(data => data.filter(_.id != id))

  def makeDataItemUpdater[A](id: DataItemID, f: (DataItem, A) => DataItem): Observer[A] =
    dataVar.updater: (data, newValue) =>
      data.map: item =>
        if item.id == id then f(item, newValue) else item
