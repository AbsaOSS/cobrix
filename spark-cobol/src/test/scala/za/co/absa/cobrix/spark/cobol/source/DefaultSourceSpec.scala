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

package za.co.absa.cobrix.spark.cobol.source

import java.io.{File, FileNotFoundException}
import java.security.InvalidParameterException

import org.apache.commons.io.FileUtils
import za.co.absa.cobrix.spark.cobol.source.base.SparkCobolTestBase
import za.co.absa.cobrix.spark.cobol.source.utils.SourceTestUtils

class DefaultSourceSpec extends SparkCobolTestBase {
   
  import SourceTestUtils._
  import za.co.absa.cobrix.spark.cobol.source.parameters.CobolParametersParser._
  
  private var defaultSource: DefaultSource = _
    
  private var testDir: File = _
    
  before {    
    defaultSource = new DefaultSource()
    testDir = SourceTestUtils.getRandomEmptyDir
  }

  after {    
    defaultSource = null
    FileUtils.deleteDirectory(testDir)
  }   
  
  behavior of "DefaultSource"
  
  it must s"always return '$SHORT_NAME' as the short name" in {   
    assert(defaultSource.shortName() == SHORT_NAME, "Invalid short name") 
  }  
  
  it must "throw NullPointerException if parameter map is null" in {
     intercept[NullPointerException] {       
       defaultSource.createRelation(sqlContext, null)
     }
  }
  
  it must "throw IllegalStateException if path to source dir is not defined" in {
     intercept[IllegalStateException] {       
       defaultSource.createRelation(sqlContext, Map[String,String](PARAM_COPYBOOK_PATH -> "a_copybook"))
     }    
  }
  
  it must "throw IllegalStateException if path to Copybook is not defined" in {
     intercept[IllegalStateException] {       
       defaultSource.createRelation(sqlContext, Map[String,String](PARAM_SOURCE_PATH -> "any_path"))
     }    
  }  
  
  it must "throw FileNotFoundException if Copybook file does not exist" in {
     intercept[FileNotFoundException] {       
       defaultSource.createRelation(sqlContext, Map[String,String](PARAM_COPYBOOK_PATH -> "any_path", PARAM_SOURCE_PATH -> "any_path"))
     }    
  }    
  
  it must "throw FileNotFoundException if data source dir does not exist" in {
     intercept[FileNotFoundException] {       
       defaultSource.createRelation(sqlContext, Map[String,String](PARAM_COPYBOOK_PATH -> "any_path", PARAM_SOURCE_PATH -> "any_path"))
     }    
  }      
  
  it must "throw InvalidParameterException if path to Copybook does not point at a file" in {         
     intercept[InvalidParameterException] {       
       defaultSource.createRelation(sqlContext, Map[String,String](
           PARAM_COPYBOOK_PATH -> testDir.getAbsolutePath, 
           PARAM_SOURCE_PATH -> testDir.getAbsolutePath
       ))
     }    
  }     
  
  it should "be created nicely if all parameters are right" in {        
    val ds = defaultSource.createRelation(sqlContext, Map[String,String](
        PARAM_COPYBOOK_PATH -> createFile(testDir, "copybook", sampleCopybook).getAbsolutePath,
        PARAM_SOURCE_PATH -> testDir.getAbsolutePath
    ))
    ds shouldBe a [CobolRelation]
  }
}