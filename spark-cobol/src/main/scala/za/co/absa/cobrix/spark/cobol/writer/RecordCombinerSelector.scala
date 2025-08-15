/*
 * Copyright 2018 ABSA Group Limited
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

package za.co.absa.cobrix.spark.cobol.writer

import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters
import za.co.absa.cobrix.cobol.reader.schema.CobolSchema

object RecordCombinerSelector {
  /**
    * Selects and returns an appropriate implementation of the `RecordCombiner` based on the provided COBOL schema 
    * and reader parameters.
    *
    * Currently, only basic fixed record length combiner is implemented. 
    * This method is to be extended as writing capabilities evolve.
    *
    * @param cobolSchema      The COBOL schema ot output record.
    * @param readerParameters Configuration parameters that specify how records should be formed.
    * @return A `RecordCombiner` implementation suitable for combining records based on the given schema and parameters.
    */
  def selectCombiner(cobolSchema: CobolSchema, readerParameters: ReaderParameters): RecordCombiner = {
    new BasicRecordCombiner
  }

}
