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

package za.co.absa.cobrix.cobol.parser.antlr

import java.nio.charset.Charset

import org.antlr.v4.runtime.{ParserRuleContext, RuleContext}
import za.co.absa.cobrix.cobol.parser.CopybookParser
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, COMP1, COMP2, COMP3, COMP4, COMP5, CobolType, Decimal, Integral, Usage}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive}
import za.co.absa.cobrix.cobol.parser.common.Constants
import za.co.absa.cobrix.cobol.parser.decoders.DecoderSelector
import za.co.absa.cobrix.cobol.parser.decoders.FloatingPointFormat.FloatingPointFormat
import za.co.absa.cobrix.cobol.parser.encoding.Encoding
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.exceptions.SyntaxErrorException
import za.co.absa.cobrix.cobol.parser.policies.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.position.{Left, Position, Right}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.matching.Regex


sealed trait Expr


class ParserVisitor(enc: Encoding,
                    stringTrimmingPolicy: StringTrimmingPolicy,
                    ebcdicCodePage: CodePage,
                    asciiCharset: Charset,
                    floatingPointFormat: FloatingPointFormat) extends copybookParserBaseVisitor[Expr] {
  /* expressions */
  case class IdentifierExpr(value: String) extends Expr
  case class OccursExpr(m: Int, M: Option[Int], dep: Option[String]) extends Expr
  case class UsageExpr(value: Option[Usage]) extends Expr
  case class PicExpr(value: CobolType) extends Expr
  case class PrimitiveExpr(value: Primitive) extends Expr
  case class SepSignExpr(value: Char, separate: Boolean) extends Expr

  /* aux classes */
  case class Level(n: Int, el: Group, context: ParserRuleContext, children: Option[Int] = None)
  private var levels: mutable.Stack[Level] = mutable.Stack()
  var ast: CopybookAST = Group.root

  /* regex */
  def genericLengthRegex(char: Char, optional: Boolean= false): String = {
    val question = if (optional) "*" else "+"
    s"((?:$char\\(\\d+\\)|$char)$question)"
  }

  val lengthRegex: Regex = "([9XPZA])\\((\\d+)\\)|([9XPZA])".r
  val numericSPicRegexScaled: Regex = ("(S?)"
                                       + genericLengthRegex('9')
                                       + genericLengthRegex('P', optional = true)
                                       ).r
  val numericSPicRegexExplicitDot: Regex = ("(S?)"
                                            + genericLengthRegex('9', optional = true)
                                            + "."
                                            + genericLengthRegex('9')
                                            ).r
  val numericSPicRegexDecimalScaled: Regex = ("(S?)"
                                              + genericLengthRegex('9', optional = true)
                                              + "V"
                                              + genericLengthRegex('P', optional = true)
                                              + genericLengthRegex('9', optional = true)
                                              ).r
  val numericSPicRegexDecimalScaledLead: Regex = ("(S?)"
                                                  + genericLengthRegex('P')
                                                  + genericLengthRegex('9')
                                                  ).r
  val numericZPicRegexExplicitDot: Regex = (genericLengthRegex('Z')
                                            + genericLengthRegex('9', optional = true)
                                            + "."
                                            + genericLengthRegex('9', optional = true)
                                            + genericLengthRegex('Z', optional = true)
                                            ).r
  val numericZPicRegexDecimalScaled: Regex = (genericLengthRegex('Z')
                                              + genericLengthRegex('9', optional = true)
                                              + "V"
                                              + genericLengthRegex('P', optional = true)
                                              + genericLengthRegex('9', optional = true)
                                              + genericLengthRegex('Z', optional = true)
                                              ).r
  val numericZPicRegexScaled: Regex = (genericLengthRegex('Z')
                                       + genericLengthRegex('9', optional = true)
                                       + genericLengthRegex('P', optional = true)
                                       ).r

  /* aux methods */
  def length(text: String): (String, Int) = {
    var len = 0
    for(m <- lengthRegex.findAllIn(text).matchData) {
      len += (m.subgroups match {
        case List(_ , null, _) => 1
        case List(_ , d, _) => d.toInt
      })
    }
    (text.charAt(0).toString, len)
  }

  def usageFromText(text: String): Option[Usage] = {
    text match {
      case "COMP" | "COMPUTATIONAL" | "COMP-0" | "COMPUTATIONAL-0" => Some(COMP4())
      case "COMP-1" | "COMPUTATIONAL-1" => Some(COMP1())
      case "COMP-2" | "COMPUTATIONAL-2" => Some(COMP2())
      case "COMP-3" | "COMPUTATIONAL-3" | "PACKED-DECIMAL" => Some(COMP3())
      case "COMP-4" | "COMPUTATIONAL-4" => Some(COMP4())
      case "COMP-5" | "COMPUTATIONAL-5" => Some(COMP5())
      case "DISPLAY" => None
      case "BINARY" => Some(COMP4())
      case _ => throw new RuntimeException("Unknown Usage literal " + text)
    }
  }

  def isSignSeparate(pic: CobolType): Boolean ={
    pic match {
      case x: Decimal => x.isSignSeparate
      case x: Integral => x.isSignSeparate
      case _ => throw new RuntimeException("Bad test for sign.")
    }
  }


  def replaceUsage(ctx: ParserRuleContext, pic: PicExpr, usage: Option[Usage]): PicExpr = {
    usage match {
      case None => pic
      case Some(usageVal) =>
        PicExpr(
          pic.value match {
            case x: Decimal =>
              val dec = x.asInstanceOf[Decimal]
              if (dec.compact.isDefined && !dec.compact.contains(usageVal))
                throw  new SyntaxErrorException(ctx.start.getLine, "", s"Field USAGE (${dec.compact.get}) doesn't match group's USAGE ($usageVal).")
              dec.copy(compact=usage)
            case x: Integral =>
              val int = x.asInstanceOf[Integral]
              if (int.compact.isDefined && !int.compact.contains(usageVal))
                throw  new SyntaxErrorException(ctx.start.getLine, "", s"Field USAGE (${int.compact.get}) doesn't match group's USAGE ($usageVal).")
              int.copy(compact=usage)
          }
        )
    }
  }

  def replaceSign(pic: PicExpr, side: Char, sign: Char, separate: Boolean): PicExpr = {
    val position: Option[Position] = side match {
      case 'L' => Some(Left)
      case 'T' => Some(Right)
    }

    val newPic = (if (side == 'L') sign else "") + pic.value.pic + (if (side == 'T') sign else "")

    PicExpr(
      pic.value match {
        case x: Decimal => x.copy(pic=newPic, signPosition=position, isSignSeparate=separate)
        case x: Integral => x.copy(pic=newPic, signPosition=position, isSignSeparate=separate)
      }
    )
  }

  def replaceDecimal0(pic: PicExpr): PicExpr = {
    pic.value match {
      case x: Decimal if x.scale == 0 && x.scaleFactor == 0 => PicExpr(
        Integral(
          x.pic,
          x.precision,
          x.signPosition,
          x.isSignSeparate,
          x.wordAlligned,
          x.compact,
          x.enc,
          x.originalPic
        )
      )
      case _ => pic
    }
  }

  def getParentFromLevel(section: Int): Group = {
    while (section <= levels.top.n) {
      levels.pop
    }

    def addLevel(s: Int) = {
      val newTop = levels.top.copy(children = Some(s))
      levels.pop
      levels.push(newTop)
    }

    levels.top.children match {
      case Some(s) if s == section => { }
      case None => addLevel(section)
      case Some(s) if s > section => addLevel(section)
      case _ =>
        throw new SyntaxErrorException(levels.top.el.children.last.lineNumber, levels.top.el.children.last.name,
          s"The field is a leaf element and cannot contain nested fields.")
    }

    levels.top.el
  }

  def fromNumericSPicRegexDecimalScaled(s: String, nine1: String, scale: String, nine2: String): Decimal = {
    val (char_1, len_1) = nine1 match {
      case "" => ('9', 0)
      case _ => length(nine1)
    }
    val (char_s, len_s) = scale match {
      case "" => ('9', 0)
      case _ => length(scale)
    }
    val (char_2, len_2) = nine2 match {
      case "" => ('9', 0)
      case _ => length(nine2)
    }

    val pic = (s
      + (if (len_1 > 0) s"$char_1(${len_1.toString})" else "")
      + "V"
      + (if (len_s > 0) s"$char_s(${len_s.toString})" else "")
      + (if (len_2 > 0) s"$char_2(${len_2.toString})" else "")
      )

    Decimal(
      pic,
      len_2,
      len_1 + len_2,
      len_s,
      explicitDecimal = false,
      if (s == "S") Some(Left) else None,
      isSignSeparate = false,
      None,
      None,
      Some(enc),
      None
    )
  }

  def fromNumericSPicRegexScaled(s: String, text: String, scale: String): Decimal = {
    val (char, len) = length(text)
    val (char_s, len_s) = scale match {
      case "" => ('P', 0)
      case _ => length(scale)
    }

    val pic = (s
      + s"$char(${len.toString})"
      + (if (len_s > 0) s"$char_s(${len_s.toString})" else "")
      )

    Decimal(
      pic,
      0,
      len,
      len_s,
      explicitDecimal = false,
      if (s == "S") Some(Left) else None,
      isSignSeparate = false,
      None,
      None,
      Some(enc),
      None
    )
  }

  def fromNumericSPicRegexExplicitDot(s: String, nine1: String, nine2: String): Decimal = {
    val (char_1, len_1) = nine1 match {
      case "" => ('9', 0)
      case _ => length(nine1)
    }
    val (char_2, len_2) = length(nine2)
    val pic = (s
      + (if (len_1 > 0) s"$char_1(${len_1.toString})" else "")
      + "."
      + s"$char_2(${len_2.toString})"
      )
    Decimal(
      pic,
      len_2,
      len_1 + len_2,
      scaleFactor=0,
      explicitDecimal = true,
      if (s == "S") Some(Left) else None,
      isSignSeparate = false,
      None,
      None,
      Some(enc),
      None
    )
  }

  def fromNumericSPicRegexDecimalScaledLead(s: String, scale: String, nine: String): Decimal = {
    val (char, len) = length(nine)
    val (char_s, len_s) = length(scale)

    val pic = (s
      + (if (len_s > 0) s"$char_s(${len_s.toString})" else "")
      + s"$char(${len.toString})"
      )

    Decimal(
      pic,
      0,
      len,
      -len_s,
      explicitDecimal= false,
      if (s == "S") Some(Left) else None,
      isSignSeparate = false,
      None,
      None,
      Some(enc),
      None
    )
  }

  def fromNumericZPicRegexExplicitDot(z1: String, nine1: String, nine2: String, z2: String): Decimal = {
    val (_, len_z1) = length(z1)
    val (char_n1, len_n1) = nine1 match {
      case "" => ('9', 0)
      case _ => length(nine1)
    }
    val (char_n2, len_n2) = nine2 match {
      case "" => ('9', 0)
      case _ => length(nine2)
    }
    val (char_z2, len_z2) = z2 match {
      case "" => ('Z', 0)
      case _ => length(z2)
    }

    val pic = (s"(${len_z1.toString})"
      + (if (len_n1 > 0) char_n1 + "(" + len_n1.toString + ")" else "")
      + "."
      + (if (len_n2 > 0) char_n2 + "(" + len_n2.toString + ")" else "")
      + (if (len_z2 > 0) char_z2 + "(" + len_z2.toString + ")" else "")
      )
    Decimal(
      pic,
      len_n2 + len_z2,
      len_z1 + len_n1 + len_n2 + len_z2,
      scaleFactor=0,
      explicitDecimal = true,
      None,
      isSignSeparate = false,
      None,
      None,
      Some(enc),
      None
    )
  }

  def fromNumericZPicRegexDecimalScaled(z1: String, nine1: String, scale: String, nine2: String, z2: String): Decimal = {
    val (_, len_z1) = length(z1)
    val (char_n1, len_n1) = nine1 match {
      case "" => ('9', 0)
      case _ => length(nine1)
    }
    val (char_s, len_s) = scale match {
      case "" => ('P', 0)
      case _ => length(scale)
    }
    val (char_n2, len_n2) = nine2 match {
      case "" => ('9', 0)
      case _ => length(nine2)
    }
    val (char_z2, len_z2) = z2 match {
      case "" => ('Z', 0)
      case _ => length(z2)
    }

    val pic = (s"Z(${len_z1.toString})"
      + (if (len_n1 > 0) char_n1 + "(" + len_n1.toString + ")" else "")
      + "V"
      + (if (len_s > 0) char_s + "(" + len_s.toString + ")" else "")
      + (if (len_n2 > 0) char_n2 + "(" + len_n2.toString + ")" else "")
      + (if (len_z2 > 0) char_z2 + "(" + len_z2.toString + ")" else "")
      )

    Decimal(
      pic,
      len_n2 + len_z2,
      len_z1 + len_n1 + len_n2 + len_z2,
      -len_s,
      explicitDecimal = false,
      None,
      isSignSeparate = false,
      None,
      None,
      Some(enc),
      None
    )
  }

  def fromNumericZPicRegexScaled(z: String, nine: String, scale: String): Decimal = {
    val (_, len_z) = length(z)
    val (char_n, len_n) = nine match {
      case "" => ('9', 0)
      case _ => length(nine)
    }
    val (char_s, len_s) = scale match {
      case "" => ('P', 0)
      case _ => length(scale)
    }

    val pic = (s"Z(${len_z.toString})"
      + (if (len_n > 0) char_n + "(" + len_n.toString + ")" else "")
      + (if (len_s > 0) char_s + "(" + len_s.toString + ")" else "")
      )

    Decimal(
      pic,
      0,
      len_z + len_n,
      len_s,
      explicitDecimal = false,
      None,
      isSignSeparate = false,
      None,
      None,
      Some(enc),
      None
    )
  }

  override def visitMain(ctx: copybookParser.MainContext): Expr = {
    // initialize AST
    ast = Group.root.copy(children = mutable.ArrayBuffer())(None)
    levels = mutable.Stack(Level(0, ast, ctx))

    visitChildren(ctx)
  }

  override def visitIdentifier(ctx: copybookParser.IdentifierContext): IdentifierExpr = {
    IdentifierExpr(
      CopybookParser.transformIdentifier(
        ctx.getText.replace("'", "").replace("\"", "")
      )
    )
  }

  override def visitOccurs(ctx: copybookParser.OccursContext): OccursExpr = {
    val m = ctx.integerLiteral.getText.toInt
    val M: Option[Int] = ctx.occursTo() match {
      case null => None
      case x => Some(x.integerLiteral.getText.toInt)
    }
    val dep: Option[String] = ctx.dependingOn() match {
      case null => None
      case x => Some(visitIdentifier(x.identifier()).value)
    }

    OccursExpr(m, M, dep)
  }

  override def visitUsage(ctx: copybookParser.UsageContext): UsageExpr = {
    UsageExpr(
      usageFromText(ctx.usageLiteral().getText)
    )
  }

  override def visitUsageGroup(ctx: copybookParser.UsageGroupContext): UsageExpr = {
    UsageExpr(
      usageFromText(ctx.groupUsageLiteral().getText)
    )
  }

  override def visitGroup(ctx: copybookParser.GroupContext): Expr = {
    val section = ctx.section.getText.toInt
    val parent: Group = getParentFromLevel(section)

    assert(ctx.redefines.size() < 2)
    assert(ctx.usageGroup.size() < 2)
    assert(ctx.occurs.size() < 2)
    assert(ctx.values.size() < 2)

    val identifier = visitIdentifier(ctx.identifier()).value

    val redefines: Option[String] = ctx.redefines().asScala.toList match {
      case Nil => None
      case x :: _ => Some(visitIdentifier(x.identifier).value)
    }

    val occurs: Option[OccursExpr] = ctx.occurs().asScala.toList match {
      case Nil => None
      case x :: _ => Some(visitOccurs(x))
    }

    val usage: Option[Usage] = ctx.usageGroup.asScala.toList match {
      case Nil => None
      case x :: _ => visitUsageGroup(x).value
    }

    val grp = Group(
      section,
      identifier,
      ctx.start.getLine,
      mutable.ArrayBuffer(),
      redefines,
      isRedefined = false,
      isSegmentRedefine = false,
      parentSegment = None,
      if (occurs.isDefined) Some(occurs.get.m) else None,
      if (occurs.isDefined) occurs.get.M else None,
      if (occurs.isDefined) occurs.get.dep else None,
      identifier.toUpperCase() == Constants.FILLER,
      usage
    )(Some(parent))

    parent.children.append(grp)
    levels.push(Level(section, grp, ctx))
    visitChildren(ctx)
  }

  def getIdentifier(ctx: RuleContext): String = {
    ctx match {
      case x: copybookParser.PrimitiveContext => visitIdentifier(x.identifier()).value
      case x: copybookParser.GroupContext => visitIdentifier(x.identifier()).value
      case _ => throw new Exception()
    }
  }

  def checkBounds(ctx: ParserRuleContext, expr: PicExpr): PicExpr = {
    expr.value match {
      case x: Decimal =>
        if (x.isSignSeparate && x.compact.isDefined)
          throw new SyntaxErrorException(ctx.start.getLine, getIdentifier(ctx.parent),
            s"SIGN SEPARATE clause is not supported for ${x.compact.get}. It is only supported for DISPLAY formatted fields.")
        if(x.scale > Constants.maxDecimalScale)
          throw new SyntaxErrorException(ctx.start.getLine, getIdentifier(ctx.parent),
            s"Decimal numbers with scale bigger than ${Constants.maxDecimalScale} are not supported.")
        if(x.precision > Constants.maxDecimalPrecision)
          throw new SyntaxErrorException(ctx.start.getLine, getIdentifier(ctx.parent),
            s"Decimal numbers with precision bigger than ${Constants.maxDecimalPrecision} are not supported.")
        if (x.compact.isDefined && x.explicitDecimal)
          throw new SyntaxErrorException(ctx.start.getLine, getIdentifier(ctx.parent),
            s"Explicit decimal point in 'PIC ${expr.value.originalPic.get}' is not supported for ${x.compact.get}. It is only supported for DISPLAY formatted fields.")
      case x: Integral =>
        if (x.isSignSeparate && x.compact.isDefined) {
          throw new SyntaxErrorException(ctx.start.getLine, getIdentifier(ctx.parent),
            s"SIGN SEPARATE clause is not supported for ${x.compact.get}. It is only supported for DISPLAY formatted fields.")
        }
        if (x.precision > Constants.maxBinIntPrecision && x.compact.contains(COMP4())) {
          throw new SyntaxErrorException(ctx.start.getLine, getIdentifier(ctx.parent),
            s"BINARY-encoded integers with precision bigger than ${Constants.maxBinIntPrecision} are not supported.")
        }
        if (x.precision < 1 || x.precision >= Constants.maxFieldLength)
          throw new SyntaxErrorException(ctx.start.getLine, getIdentifier(ctx.parent),
            s"Incorrect field size of ${x.precision} for PIC ${expr.value.originalPic.get}. Supported size is in range from 1 to ${Constants.maxFieldLength}.")
      case x: AlphaNumeric =>
        if (x.length < 1 || x.length >= Constants.maxFieldLength)
          throw new SyntaxErrorException(ctx.start.getLine, getIdentifier(ctx.parent),
            s"Incorrect field size of ${x.length} for PIC ${expr.value.originalPic.get}. Supported size is in range from 1 to ${Constants.maxFieldLength}.")
    }
    expr
  }

  override def visitPic(ctx: copybookParser.PicContext): PicExpr = {
    if (ctx.alphaX() != null) {
      visitAlphaX(ctx.alphaX())
    }
    else if (ctx.alphaA() != null) {
      visitAlphaA(ctx.alphaA())
    }
    else if (ctx.COMP_1() != null || ctx.COMP_2() != null) {
      PicExpr(
        Decimal(
          "9(16)V9(16)",
          16,
          32,
          scaleFactor=0,
          explicitDecimal = false,
          None,
          isSignSeparate = false,
          None,
          Some(if(ctx.COMP_1() != null) COMP1() else COMP2()),
          Some(enc),
          None
        )
      )
    }
    else {
      val numeric = replaceDecimal0(visit(ctx.signPrecision9()).asInstanceOf[PicExpr])
      ctx.usage() match {
        case null => numeric
        case x => replaceUsage(ctx, numeric, visitUsage(x).value)
      }
    }
  }

  override def visitAlphaX(ctx: copybookParser.AlphaXContext): PicExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    PicExpr(AlphaNumeric(s"$char($len)", len, None, Some(enc), Some(ctx.getText)))
  }

  override def visitAlphaA(ctx: copybookParser.AlphaAContext): PicExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    PicExpr(AlphaNumeric(s"$char($len)", len, None, Some(enc), Some(ctx.getText)))
  }

  override def visitTrailingSign(ctx: copybookParser.TrailingSignContext): PicExpr = {
    val prec = visit(ctx.precision9()).asInstanceOf[PicExpr]
    ctx.plusMinus() match {
      case null => prec
      case _ => replaceSign(prec, 'T', ctx.plusMinus().getText.charAt(0), ctx.plusMinus() != null)
    }
  }

  override def visitLeadingSign(ctx: copybookParser.LeadingSignContext): PicExpr = {
    val prec = visit(ctx.precision9()).asInstanceOf[PicExpr]
    ctx.plusMinus() match {
      case null => prec
      case _ => replaceSign(prec, 'L', ctx.plusMinus().getText.charAt(0), ctx.plusMinus() != null)
    }
  }

  override def visitSeparateSign(ctx: copybookParser.SeparateSignContext): SepSignExpr = {
    val separate = ctx.SEPARATE() != null
    if(ctx.LEADING() != null)
      SepSignExpr('L', separate)
    else
      SepSignExpr('T', separate)
  }

  override def visitPrecision9Nines(ctx: copybookParser.Precision9NinesContext): PicExpr = {
    val pic = ctx.getText
    PicExpr(
      Integral(
        s"9(${pic.length})",
        pic.length,
        None,
        isSignSeparate = false,
        None,
        None,
        Some(enc),
        Some(pic)
      )
    )
  }

  override def visitPrecision9Ss(ctx: copybookParser.Precision9SsContext): PicExpr = {
    ctx.getText match {
      case numericSPicRegexDecimalScaled(s, nine1, scale, nine2) => PicExpr(
        fromNumericSPicRegexDecimalScaled(s, nine1, scale, nine2)
      )
      case numericSPicRegexScaled(z, nine, scale) => PicExpr(
        fromNumericSPicRegexScaled(z, nine, scale)
      )
      case numericSPicRegexDecimalScaledLead(s, scale, nine) => PicExpr(
        fromNumericSPicRegexDecimalScaledLead(s, scale, nine)
      )
      case _ => throw new RuntimeException("Error reading PIC " + ctx.getText)
    }
  }

  override def visitPrecision9Ps(ctx: copybookParser.Precision9PsContext): PicExpr = {
    val numericSPicRegexDecimalScaledLead(s, scale, nine) = ctx.getText
    PicExpr(
      fromNumericSPicRegexDecimalScaledLead(s, scale, nine)
    )
  }

  override def visitPrecision9Vs(ctx: copybookParser.Precision9VsContext): PicExpr = {
    ctx.getText match {
      case numericSPicRegexDecimalScaled(s, nine1, scale, nine2) => PicExpr(
        fromNumericSPicRegexDecimalScaled(s, nine1, scale, nine2)
      )
      case _ => throw new RuntimeException("Error reading PIC " + ctx.getText)
    }
  }

  override def visitPrecision9Zs(ctx: copybookParser.Precision9ZsContext): PicExpr = {
    ctx.getText match {
      case numericZPicRegexDecimalScaled(z1, nine1, scale, nine2, z2) => PicExpr(
        fromNumericZPicRegexDecimalScaled(z1, nine1, scale, nine2, z2)
      )
      case numericZPicRegexScaled(z, nine, scale) => PicExpr(
        fromNumericZPicRegexScaled(z, nine, scale)
      )
      case _ => throw new RuntimeException("Error reading PIC " + ctx.getText)
    }
  }

  override def visitPrecision9ExplicitDot(ctx: copybookParser.Precision9ExplicitDotContext): PicExpr = {
    val numericSPicRegexExplicitDot(s, nine1, nine2) = ctx.getText
    PicExpr(
      fromNumericSPicRegexExplicitDot(s, nine1, nine2).copy(originalPic = Some(ctx.getText))
    )
  }

  override def visitPrecision9DecimalScaled(ctx: copybookParser.Precision9DecimalScaledContext): PicExpr = {
    val numericSPicRegexDecimalScaled(s, nine1, scale, nine2) = ctx.getText
    PicExpr(
      fromNumericSPicRegexDecimalScaled(s, nine1, scale, nine2).copy(originalPic = Some(ctx.getText))
    )
  }

  override def visitPrecision9Scaled(ctx: copybookParser.Precision9ScaledContext): PicExpr = {
    val numericSPicRegexScaled(s, text, scale) = ctx.getText
    PicExpr(
      fromNumericSPicRegexScaled(s, text, scale).copy(originalPic = Some(ctx.getText))
    )
  }

  override def visitPrecision9ScaledLead(ctx: copybookParser.Precision9ScaledLeadContext): PicExpr = {
    val numericSPicRegexDecimalScaledLead(s, scale, nine) = ctx.getText
    PicExpr(
      fromNumericSPicRegexDecimalScaledLead(s, scale, nine)
    )
  }

  override def visitPrecisionZExplicitDot(ctx: copybookParser.PrecisionZExplicitDotContext): PicExpr = {
    val numericZPicRegexExplicitDot(z1, nine1, nine2, z2) = ctx.getText
    PicExpr(
      fromNumericZPicRegexExplicitDot(z1, nine1, nine2, z2).copy(originalPic = Some(ctx.getText))
    )
  }

  override def visitPrecisionZDecimalScaled(ctx: copybookParser.PrecisionZDecimalScaledContext): PicExpr = {
    val numericZPicRegexDecimalScaled(z1, nine1, scale, nine2, z2) = ctx.getText
    PicExpr(
      fromNumericZPicRegexDecimalScaled(z1, nine1, scale, nine2, z2)
    )
  }

  override def visitPrecisionZScaled(ctx: copybookParser.PrecisionZScaledContext): PicExpr = {
    val numericZPicRegexScaled(z, nine, scale) = ctx.getText
    PicExpr(
      fromNumericZPicRegexScaled(z, nine, scale)
    )
  }


  override def visitPrimitive(ctx: copybookParser.PrimitiveContext): Expr = {
    val section = ctx.section.getText.toInt
    val parent: Group = getParentFromLevel(section)

    assert(ctx.redefines.size() < 2)
    assert(ctx.usage.size() < 2)
    assert(ctx.occurs.size() < 2)
    assert(ctx.separateSign.size() < 2)
    assert(ctx.pic.size() == 1)

    val identifier = visitIdentifier(ctx.identifier()).value

    var pic: PicExpr = visitPic(ctx.pic(0))

    val redefines: Option[String] = ctx.redefines().asScala.toList match {
      case Nil => None
      case x :: _ => Some(visitIdentifier(x.identifier).value)
    }

    val occurs: Option[OccursExpr] = ctx.occurs().asScala.toList match {
      case Nil => None
      case x :: _ => Some(visitOccurs(x))
    }

    pic = ctx.usage().asScala.toList match {
      case Nil if parent.groupUsage.isEmpty => pic
      case Nil => replaceUsage(ctx, pic, Some(parent.groupUsage.get))
      case x :: _ => replaceUsage(ctx, pic, visitUsage(x).value)
    }

    pic = ctx.separateSign().asScala.toList match {
      case Nil => pic
      case x :: _ if !isSignSeparate(pic.value) => {
        val signExpr = visitSeparateSign(x)
        replaceSign(pic, signExpr.value, '-', signExpr.separate)
      }
      case _ => throw new RuntimeException("Cannot mix explicit signs and SEPARATE clauses")
    }

    checkBounds(ctx.pic(0), pic)

    val prim = Primitive(
      section,
      identifier,
      ctx.start.getLine,
      pic.value,
      redefines,
      isRedefined = false,
      if (occurs.isDefined) Some(occurs.get.m) else None,
      if (occurs.isDefined) occurs.get.M else None,
      if (occurs.isDefined) occurs.get.dep else None,
      isDependee = false,
      identifier.toUpperCase() == Constants.FILLER,
      DecoderSelector.getDecoder(pic.value, stringTrimmingPolicy, ebcdicCodePage, asciiCharset, floatingPointFormat)
      ) (Some(parent))

    parent.children.append(prim)

    PrimitiveExpr(prim)
  }

  override def visitLevel66statement(ctx: copybookParser.Level66statementContext): Expr = {
    throw new RuntimeException("Renames not supported yet")
  }
}





