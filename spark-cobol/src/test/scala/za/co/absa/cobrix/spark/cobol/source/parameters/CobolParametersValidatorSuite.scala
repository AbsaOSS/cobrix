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

package za.co.absa.cobrix.spark.cobol.source.parameters

import org.scalatest.wordspec.AnyWordSpec
import za.co.absa.cobrix.cobol.parser.recordformats.RecordFormat.{FixedLength, VariableBlock}
import za.co.absa.cobrix.cobol.reader.parameters.{MultisegmentParameters, ReaderParameters}

class CobolParametersValidatorSuite extends AnyWordSpec {
  "validateParametersForWriting" should {
    "detect validation issues" in {
      val readParams = ReaderParameters(
        recordFormat = VariableBlock,
        occursMappings = Map("A" -> Map("B" -> 1)),
        startOffset = 1,
        fileEndOffset = 2,
        multisegment = Some(MultisegmentParameters("SEG", None, Seq.empty, "", Map.empty, Map.empty))
      )

      val ex = intercept[IllegalArgumentException] {
        CobolParametersValidator.validateParametersForWriting(readParams)
      }

      assert(ex.getMessage.contains("Writer validation issues: Only 'F' and 'V' values for 'record_format' are supported for writing, provided value: 'VB';"))
      assert(ex.getMessage.contains("OCCURS mapping option ('occurs_mappings') is not supported for writing"))
      assert(ex.getMessage.contains("'record_start_offset' and 'record_end_offset' are not supported for writing"))
      assert(ex.getMessage.contains("'file_start_offset' and 'file_end_offset' are not supported for writing"))
      assert(ex.getMessage.contains("Multi-segment options ('segment_field', 'segment_filter', etc) are not supported for writing"))
    }

    "do not throw exceptions if the configuration is okay" in {
      val readParams = ReaderParameters(
        recordFormat = FixedLength,
        variableSizeOccurs = true
      )

      CobolParametersValidator.validateParametersForWriting(readParams)
    }
  }

}
