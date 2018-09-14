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

/**
  * This class holds expressions (possibly provided by users) and expected return from them.
  *
  * @param expression String containing expressions in the format: field(a) == 2 || field(b) == field(c)
  * @param result Whatever string the rule should return upon successful evaluation
  */
case class RuleExpression(expression: String, result: String)