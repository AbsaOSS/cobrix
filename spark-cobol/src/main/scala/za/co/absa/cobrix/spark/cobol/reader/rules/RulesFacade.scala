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

package za.co.absa.cobrix.spark.cobol.reader.rules

import org.apache.log4j.Logger
import za.co.absa.cobrix.spark.cobol.reader.rules.language.Tokens
import za.co.absa.cobrix.spark.cobol.reader.rules.parsing.RuleParserAbstractFactory

/**
  * This class represents a facade to the rules interpretation and evaluation module.
  */
object RulesFacade {

  private val logger = Logger.getLogger(RulesFacade.getClass.getSimpleName)

  def extractRules(parameters: Map[String,String]): Seq[RuleExpression] = {
    Tokens.extractSortedRules(parameters)
  }

  def getRules(expressions: Seq[RuleExpression]): Seq[Rule] = {
    expressions match {
      case null => throw new IllegalArgumentException("Expected Seq[RuleExpression], Received null")
      case _    => getParser.parse(expressions)
    }
  }

  private def getParser = RuleParserAbstractFactory.buildNashornParser()
}