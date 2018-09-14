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

package za.co.absa.cobrix.spark.cobol.reader.rules.language

import za.co.absa.cobrix.spark.cobol.reader.rules.RuleExpression

/**
  * This object provides tokens extraction and cleaning utils.
  */
private[rules] object Tokens {

  private val FIELD_WRAPPER_PATTERN = "field\\(([^)]+)\\)".r
  private val FIELD_WRAPPER_NAME = "field"
  private val PARENTHESES_CONTENT_PATTERN = "\\(([^)]+)\\)".r

  private val RULE_NUMBER_PATTERN = "(?<=rule)\\s*[0-9]+\\s*(?=:)".r
  private val RULE_PATTERN = "(?<=:).+".r

  def getFieldsFromExpression(expression: String): Seq[String] = {
    FIELD_WRAPPER_PATTERN
      .findAllMatchIn(expression)
      .map(field => cleanParentheses(getParenthesesContent(field.matched)).trim)
      .toSeq
      .distinct
  }

  def cleanParentheses(str: String) = str.replaceAll("[()]", "")

  def cleanExpressionFields(expression: String) = expression.replaceAll(FIELD_WRAPPER_NAME, "")

  def getParenthesesContent(str: String): String = PARENTHESES_CONTENT_PATTERN.findFirstIn(str).get

  def extractSortedRules(map: Map[String,String]): Seq[RuleExpression] = {
    map.filter(entry => isRule(entry._1))
      .toSeq.sortBy(entry => extractRuleNumber(entry._1))
      .map(entry => (extractRule(entry._1), entry._2))
      .filter(!_._1.isEmpty)
      .map(entry => RuleExpression(entry._1, entry._2))
  }

  private def isRule(value: String): Boolean = RULE_NUMBER_PATTERN.findFirstIn(value).isDefined

  private def extractRule(value: String): String = RULE_PATTERN.findFirstIn(value).get.trim

  private def extractRuleNumber(value: String): Int = RULE_NUMBER_PATTERN.findFirstIn(value).get.trim.toInt
}