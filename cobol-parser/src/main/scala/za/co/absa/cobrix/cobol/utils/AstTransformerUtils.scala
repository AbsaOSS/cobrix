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

package za.co.absa.cobrix.cobol.utils

import za.co.absa.cobrix.cobol.parser.asttransform.AstTransformer
import za.co.absa.cobrix.cobol.reader.parameters.ReaderParameters

import java.lang.reflect.Constructor
import scala.util.Try

object AstTransformerUtils {
  /**
    * Loads and instantiates an AstTransformer by its fully qualified class name using reflection.
    *
    * The method attempts to find and invoke a constructor that accepts a ReaderParameters argument.
    * If no such constructor exists, it falls back to using the default no-argument constructor.
    *
    * @param fullyQualifiedName the fully qualified class name of the AstTransformer implementation to load
    * @param readerParameters   the reader parameters to pass to the constructor if a matching constructor is available
    * @return an instance of the specified AstTransformer class
    * @throws java.lang.ClassNotFoundException if the class with the given fully qualified name cannot be found
    * @throws java.lang.ClassCastException     if the instantiated object cannot be cast to AstTransformer
    * @throws java.lang.NoSuchMethodException  if neither a constructor accepting ReaderParameters nor a default constructor is found
    */
  @throws[ClassNotFoundException]
  @throws[ClassCastException]
  @throws[NoSuchMethodException]
  def loadAstTransformer(fullyQualifiedName: String, readerParameters: ReaderParameters): AstTransformer = {
    // There are 2 types of constructors supported. If there is a one that takes a ReaderParameters object - use it.
    // Otherwise, try the default constructor.
    val confCtor = Try[Constructor[_]](Class.forName(fullyQualifiedName).getConstructor(classOf[ReaderParameters]))

    confCtor
      .map(ctor => ctor.newInstance(readerParameters).asInstanceOf[AstTransformer])
      .recoverWith {
        case _: NoSuchMethodException =>
          val defCtor = Try[Constructor[_]](Class.forName(fullyQualifiedName).getConstructor())
          defCtor.map(ctor => ctor.newInstance().asInstanceOf[AstTransformer])
      }.get
  }
}
