package za.co.absa.cobrix.cobol.parser.common

import scodec.bits.BitVector
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.{CBTree, Group, Statement}

import scala.collection.mutable.ArrayBuffer

object DataExtractors {

  def extractValues(ast: CopybookAST, bytes: Array[Byte], offset: Int = 0): Seq[Any] = {

    val dataBits: BitVector = BitVector(bytes)
    val dependFields = scala.collection.mutable.HashMap.empty[String, Int]

    // Todo Extract common features and combine with BinaryDataRowIterator as it does almost the same

    def extractArray(field: CBTree, useOffset: Long): IndexedSeq[Any] = {
      val from = 0
      val arraySize = field.arrayMaxSize
      val actualSize = field.dependingOn match {
        case None => arraySize
        case Some(dependingOn) =>
          val dependValue = dependFields.getOrElse(dependingOn, arraySize)
          if (dependValue >= field.arrayMinSize && dependValue <= arraySize)
            dependValue
          else
            arraySize
      }

      var offset = useOffset
      field match {
        case grp: Group =>
          val groupValues = for (_ <- Range(from, actualSize)) yield {
            val value = getGroupValues(offset, grp)
            offset += grp.binaryProperties.dataSize
            value
          }
          groupValues
        case s: Statement =>
          val values = for (_ <- Range(from, actualSize)) yield {
            val value = s.decodeTypeValue(offset, dataBits)
            offset += s.binaryProperties.dataSize
            value
          }
          values
      }
    }

    def extractValue(field: CBTree, useOffset: Long): Any = {
      field match {
        case grp: Group =>
          getGroupValues(useOffset, grp)
        case st: Statement =>
          val value = st.decodeTypeValue(useOffset, dataBits)
          if (value != null && st.isDependee) {
            val intVal: Int = value match {
              case v: Int => v
              case v: Number => v.intValue()
              case v => throw new IllegalStateException(s"Field ${st.name} is an a DEPENDING ON field of an OCCURS, should be integral, found ${v.getClass}.")
            }
            dependFields += st.name -> intVal
          }
          value
      }
    }

    def getGroupValues(offset: Long, group: Group): Seq[Any] = {
      var bitOffset = offset
      val fields = new ArrayBuffer[Any]()

      for (field <- group.children) {
        val fieldValue = if (field.isArray) {
          extractArray(field, bitOffset)
        } else {
          extractValue(field, bitOffset)
        }
        if (!field.isRedefined) {
          bitOffset += field.binaryProperties.actualSize
        }
        if (field.name.toUpperCase != ReservedWords.FILLER) {
          fields += fieldValue
        }
      }
      fields
    }

    var nextOffset = offset * 8
    val records = for (record <- ast) yield {
      val values = getGroupValues(nextOffset, record)
      nextOffset += record.binaryProperties.actualSize
      values
    }
    records
  }

}
