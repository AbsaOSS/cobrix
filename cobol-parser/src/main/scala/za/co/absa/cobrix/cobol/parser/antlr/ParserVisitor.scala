package za.co.absa.cobrix.cobol.parser.antlr

import za.co.absa.cobrix.cobol.parser.CopybookParser

import scala.util.matching.Regex
import scala.collection.JavaConverters._
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, BINARY, COMP, COMP0, COMP1, COMP2, COMP3, COMP4, COMP5, CobolType, DISPLAY, Decimal, Integral, Usage}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.ReservedWords
import za.co.absa.cobrix.cobol.parser.decoders.DecoderSelector
import za.co.absa.cobrix.cobol.parser.decoders.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.encoding.Encoding
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage
import za.co.absa.cobrix.cobol.parser.position.Position
import za.co.absa.cobrix.cobol.parser.position.Left
import za.co.absa.cobrix.cobol.parser.position.Right


import scala.collection.mutable
import scala.collection.mutable.Stack


sealed trait Expr


class ParserVisitor(enc: Encoding,
                    stringTrimmingPolicy: StringTrimmingPolicy,
                    ebcdicCodePage: CodePage) extends copybookParserBaseVisitor[Expr] {
  /* expressions */
  case class NoOpExpr()
  case class Field(value: Statement) extends Expr
  case class IdentifierExpr(value: String) extends Expr
  case class OccursExpr(m: Int, M: Option[Int], dep: Option[String]) extends Expr
  case class UsageExpr(value: Usage) extends Expr
  case class PicExpr(value: CobolType) extends Expr
  case class PrimitiveExpr(value: Primitive) extends Expr
  case class SepSignExpr(value: Char) extends Expr

  /* aux classes */
  case class Level(n: Int, el: Group)
  private var levels: Stack[Level] = Stack()
  var ast: CopybookAST = Group.root

  /* regex */
  def genericLengthRegex(char: Char, optional: Boolean= false): String = {
    val question = if (optional) "?" else ""
    s"($char\\(\\d+\\)|$char+)$question"
  }

  val lengthRegex: Regex = "([9XPZ])\\((\\d+)\\)|([9XPZ]+)".r
  val numericSPicRegexScaled: Regex = ("(S?)"
                                       + genericLengthRegex('9')
                                       + genericLengthRegex('P', optional = true)
                                       ).r
  val numericSPicRegexExplicitDot: Regex = ("(S?)"
                                            + genericLengthRegex('9')
                                            + "\\."
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
                                            + "\\."
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
    val lengthRegex(char, len, full) = text

    char match {
      case null => {
        (full.charAt(0).toString, full.length)
      }
      case _ => (char, len.toInt)
    }
  }

  def compact(usage: Usage): Option[Int] = {
    usage match {
      case COMP0() => Some(1)
      case COMP1() => Some(1)
      case COMP2() => Some(2)
      case COMP3() => Some(3)
      case COMP4() => Some(4)
      case COMP5() => Some(5)
      case COMP() | BINARY() => Some(4)
      case _ => None
    }
  }

  def usageFromText(text: String): Usage = {
    text match {
      case "COMP" | "COMPUTATIONAL" | "COMP-0" | "COMPUTATIONAL-0" => COMP()
      case "COMP-1" | "COMPUTATIONAL-1" => COMP1()
      case "COMP-2" | "COMPUTATIONAL-2" => COMP2()
      case "COMP-3" | "COMPUTATIONAL-3" | "PACKED-DECIMAL" => COMP3()
      case "COMP-4" | "COMPUTATIONAL-4" => COMP4()
      case "COMP-5" | "COMPUTATIONAL-5" => COMP5()
      case "DISPLAY" => DISPLAY()
      case "BINARY" => BINARY()
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


  def replaceUsage(pic: PicExpr, usage: Usage): PicExpr = {
    PicExpr(
      pic.value match {
        case x: Decimal => x.asInstanceOf[Decimal].copy(compact=compact(usage))
        case x: Integral => x.asInstanceOf[Integral].copy(compact=compact(usage))
      }
    )
  }

  def replaceSign(pic: PicExpr, side: Char, sign: Char): PicExpr = {
    val position: Option[Position] = side match {
      case 'L' => Some(Left)
      case 'T' => Some(Right)
    }

    val newPic = (if (side == 'L') sign else "") + pic.value.pic + (if (side == 'T') sign else "")

    PicExpr(
      pic.value match {
        case x: Decimal => x.copy(pic=newPic, signPosition=position, isSignSeparate=true)
        case x: Integral => x.copy(pic=newPic, signPosition=position, isSignSeparate=true)
      }
    )
  }

  def replaceDecimal0(pic: PicExpr): PicExpr = {
    pic.value match {
      case x: Decimal if x.scale == 0 => PicExpr(
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
    levels.top.el
  }

  def fromNumericSPicRegexDecimalScaled(s: String, nine1: String, scale: String, nine2: String): Decimal = {
    val (char_1, len_1) = nine1 match {
      case null => ('9', 0)
      case _ => length(nine1)
    }
    val (char_s, len_s) = scale match {
      case null => ('9', 0)
      case _ => length(scale)
    }
    val (char_2, len_2) = nine2 match {
      case null => ('9', 0)
      case _ => length(nine2)
    }

    val pic = (s
      + (if (len_1 > 0) char_1 + "(" + len_1.toString + ")" else "")
      + "V"
      + (if (len_s > 0) char_s + "(" + len_s.toString + ")" else "")
      + (if (len_2 > 0) char_2 + "(" + len_2.toString + ")" else "")
      )

    Decimal(
      pic,
      len_2,
      len_1 + len_2,
      explicitDecimal = false,
      if (s == "S") Some(Left) else None,
      isSignSeparate = false,
      None,
      None,
      Some(enc),
      None
    )
  }

  def fromNumericSPicRegexScaled(s: String, text: String, scale: String): Integral = {
    val (char, len) = length(text)
    Integral(
      s"$s$char(${len.toString})",
      len,
      if (s == "S") Some(Left) else None,
      isSignSeparate = false,
      None,
      None,
      Some(enc),
      None
    )
  }

  def fromNumericSPicRegexExplicitDot(s: String, nine1: String, nine2: String): Decimal = {
    val (char_1, len_1) = length(nine1)
    val (char_2, len_2) = length(nine2)
    val pic = s"$s$char_1(${len_1.toString}).$char_2(${len_2.toString})"
    Decimal(
      pic,
      len_2,
      len_1 + len_2,
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
    Decimal(
      s"$s$char(${len.toString})",
      0,
      len,
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
    val (char_z1, len_z1) = length(z1)
    val (char_n1, len_n1) = nine1 match {
      case null => ('9', 0)
      case _ => length(nine1)
    }
    val (char_n2, len_n2) = nine2 match {
      case null => ('9', 0)
      case _ => length(nine2)
    }
    val (char_z2, len_z2) = z2 match {
      case null => ('Z', 0)
      case _ => length(z2)
    }

    val pic = (s"Z(${len_z1.toString})"
      + (if (len_n1 > 0) char_n1 + "(" + len_n1.toString + ")" else "")
      + "."
      + (if (len_n2 > 0) char_n2 + "(" + len_n2.toString + ")" else "")
      + (if (len_z2 > 0) char_z2 + "(" + len_z2.toString + ")" else "")
      )
    Decimal(
      pic,
      len_n2 + len_z2,
      len_z1 + len_n1 + len_n2 + len_z2,
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
    val (char_z1, len_z1) = length(z1)
    val (char_n1, len_n1) = nine1 match {
      case null => ('9', 0)
      case _ => length(nine1)
    }
    val (char_s, len_s) = scale match {
      case null => ('P', 0)
      case _ => length(scale)
    }
    val (char_n2, len_n2) = nine2 match {
      case null => ('9', 0)
      case _ => length(nine2)
    }
    val (char_z2, len_z2) = z2 match {
      case null => ('Z', 0)
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
    val (char_z, len_z) = length(z)
    val (char_n, len_n) = nine match {
      case null => ('9', 0)
      case _ => length(nine)
    }
    val (char_s, len_s) = scale match {
      case null => ('P', 0)
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
    levels = Stack(Level(0, ast))

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
      case x => Some(x.getText.toInt)
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
      case x :: _ => Some(visitUsageGroup(x).value)
    }

    val grp = Group(
      section,
      identifier,
      ctx.start.getLine,
      mutable.ArrayBuffer(),
      redefines,
      isRedefined = false,
      isSegmentRedefine = false,
      if (occurs.isDefined) Some(occurs.get.m) else None,
      if (occurs.isDefined) occurs.get.M else None,
      if (occurs.isDefined) occurs.get.dep else None,
      identifier.toUpperCase() == ReservedWords.FILLER,
      usage
    )(Some(parent))

    parent.children.append(grp)
    levels.push(Level(section, grp))
    visitChildren(ctx)
  }

  override def visitPic(ctx: copybookParser.PicContext): PicExpr = {
    if (ctx.alphaX() != null) {
      visitAlphaX(ctx.alphaX())
    }
    else if (ctx.alphaX() != null) {
      visitAlphaX(ctx.alphaX())
    }
    else if (ctx.COMP_1() != null || ctx.COMP_2() != null) {
      PicExpr(
        Decimal(
          "9(16)V9(16)",
          16,
          32,
          explicitDecimal = false,
          None,
          isSignSeparate = false,
          None,
          Some(if(ctx.COMP_1() != null) 1 else 2),
          Some(enc),
          None
        )
      )
    }
    else {
      val numeric = replaceDecimal0(visit(ctx.signPrecision9()).asInstanceOf[PicExpr])
      ctx.usage() match {
        case null => numeric
        case x => replaceUsage(numeric, visitUsage(x).value)
      }
    }
  }

  override def visitAlphaX(ctx: copybookParser.AlphaXContext): PicExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    PicExpr(AlphaNumeric(char, len, None, Some(enc), Some(ctx.getText)))
  }

  override def visitAlphaA(ctx: copybookParser.AlphaAContext): PicExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    PicExpr(AlphaNumeric(char, len, None, Some(enc), Some(ctx.getText)))
  }

  override def visitTrailingSign(ctx: copybookParser.TrailingSignContext): PicExpr = {
    val prec = visit(ctx.precision9()).asInstanceOf[PicExpr]
    ctx.plusMinus() match {
      case null => prec
      case _ => replaceSign(prec, 'T', ctx.plusMinus().getText.charAt(0))
    }
  }

  override def visitLeadingSign(ctx: copybookParser.LeadingSignContext): PicExpr = {
    val prec = visit(ctx.precision9()).asInstanceOf[PicExpr]
    ctx.plusMinus() match {
      case null => prec
      case x => replaceSign(prec, 'L', ctx.plusMinus().getText.charAt(0))
    }
  }

  override def visitSeparateSign(ctx: copybookParser.SeparateSignContext): SepSignExpr = {
    if(ctx.LEADING() != null)
      SepSignExpr('L')
    else
      SepSignExpr('T')
  }

  override def visitPrecision9Nines(ctx: copybookParser.Precision9NinesContext): PicExpr = {
    val pic = ctx.getText
    PicExpr(
      Integral(
        pic,
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
    if (ctx.getText contains "P")
      throw new RuntimeException("Scaled numbers not supported yet")

    ctx.getText match {
      case numericZPicRegexExplicitDot(z1, nine1, nine2, z2) => PicExpr(
        fromNumericZPicRegexExplicitDot(z1, nine1, nine2, z2)
      )
      case x => x match {
          case numericSPicRegexDecimalScaled(s, nine1, scale, nine2) => PicExpr(
            fromNumericSPicRegexDecimalScaled(s, nine1, scale, nine2)
          )
          case y => y match {
              case numericZPicRegexScaled(z, nine, scale) => PicExpr(
                fromNumericZPicRegexScaled(z, nine, scale)
              )
              case _ => throw new RuntimeException("Error reading PIC " + y)
            }
        }
    }
  }

  override def visitPrecision9Zs(ctx: copybookParser.Precision9ZsContext): PicExpr = {
    if (ctx.getText contains "P")
      throw new RuntimeException("Scaled numbers not supported yet")

    ctx.getText match {
      case numericZPicRegexExplicitDot(s, nine, scale) => PicExpr(
        fromNumericSPicRegexScaled(s, nine, scale)
      )
      case x => x match {
        case numericSPicRegexDecimalScaled(s, nine1, scale, nine2) => PicExpr(
          fromNumericSPicRegexDecimalScaled(s, nine1, scale, nine2)
        )
        case y => y match {
          case numericSPicRegexDecimalScaledLead(s, scale, nine) => PicExpr(
            fromNumericSPicRegexDecimalScaledLead(s, scale, nine)
          )
          case _ => throw new RuntimeException("Error reading PIC " + y)
        }
      }
    }
  }

  override def visitPrecision9ExplicitDot(ctx: copybookParser.Precision9ExplicitDotContext): PicExpr = {
    val numericSPicRegexExplicitDot(s, nine1, nine2) = ctx.getText
    PicExpr(
      fromNumericSPicRegexExplicitDot(s, nine1, nine2).copy(originalPic = Some(ctx.getText))
    )
  }

  override def visitPrecision9DecimalScaled(ctx: copybookParser.Precision9DecimalScaledContext): PicExpr = {
    if (ctx.getText contains "P")
      throw new RuntimeException("Scaled numbers not supported yet")

    val numericSPicRegexDecimalScaled(s, nine1, scale, nine2) = ctx.getText
    PicExpr(
      fromNumericSPicRegexDecimalScaled(s, nine1, scale, nine2).copy(originalPic = Some(ctx.getText))
    )
  }

  override def visitPrecision9Scaled(ctx: copybookParser.Precision9ScaledContext): PicExpr = {
    if (ctx.getText contains "P")
      throw new RuntimeException("Scaled numbers not supported yet")

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
      case Nil => replaceUsage(pic, parent.groupUsage.get)
      case x :: _ => replaceUsage(pic, visitUsage(x).value)
    }

    pic = ctx.separateSign().asScala.toList match {
      case Nil => pic
      case x :: _ if !isSignSeparate(pic.value) => replaceSign(pic, visitSeparateSign(x).value, '-')
      case _ => throw new RuntimeException("Cannot mix explicit signs and SEPARATE clauses")
    }

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
      identifier.toUpperCase() == ReservedWords.FILLER,
      DecoderSelector.getDecoder(pic.value, stringTrimmingPolicy, ebcdicCodePage)
      ) (Some(parent))

    parent.children.append(prim)

    PrimitiveExpr(prim)
  }

  override def visitLevel66statement(ctx: copybookParser.Level66statementContext): Expr = {
    throw new RuntimeException("Renames not supported yet")
  }
}





