package za.co.absa.cobrix.cobol.parser.antlr

import za.co.absa.cobrix.cobol.parser.CopybookParser

import scala.util.matching.Regex
import scala.collection.JavaConverters._
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, BINARY, COMP, COMP1, COMP2, COMP3, COMP4, COMP5, CobolType, DISPLAY, Decimal, Integral, Usage}
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
                    ebcdicCodePage: CodePage) extends copybook_parserBaseVisitor[Expr] {
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
  val numericZPicRegex: Regex = "".r


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
      case COMP1() => Some(1)
      case COMP2() => Some(2)
      case COMP3() => Some(3)
      case COMP() | BINARY() => Some(4)
      case _ => None
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

  def replaceSign(pic: PicExpr, char: Char): PicExpr = {
    val side: Option[Position] = char match {
      case 'L' => Some(Left)
      case 'T' => Some(Right)
    }

    val newPic = (if (char == 'L') "-" else "") + pic.value.pic + (if (char == 'T') "-" else "")

    PicExpr(
      pic.value match {
        case x: Decimal => x.copy(pic=newPic, signPosition=side, isSignSeparate=true)
        case x: Integral => x.copy(pic=newPic, signPosition=side, isSignSeparate=true)
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


  override def visitMain(ctx: copybook_parser.MainContext): Expr = {
    // initialize AST
    ast = Group.root.copy(children = mutable.ArrayBuffer())(None)
    levels = Stack(Level(0, ast))

    visitChildren(ctx)
  }

  override def visitIdentifier(ctx: copybook_parser.IdentifierContext): IdentifierExpr = {
    IdentifierExpr(
      CopybookParser.transformIdentifier(
        ctx.getText.replace("'", "").replace("\"", "")
      )
    )
  }

  override def visitOccurs(ctx: copybook_parser.OccursContext): OccursExpr = {
    val m = ctx.integerLiteral.getText.toInt
    val M: Option[Int] = ctx.occurs_to() match {
      case null => None
      case x => Some(x.getText.toInt)
    }
    val dep: Option[String] = ctx.depending_on() match {
      case null => None
      case x => Some(visitIdentifier(x.identifier()).value)
    }

    OccursExpr(m, M, dep)
  }

  override def visitUsage(ctx: copybook_parser.UsageContext): UsageExpr = {
    ctx.usageLiteral().getText match {
      case "COMP" | "COMPUTATIONAL" | "COMP-0" | "COMPUTATIONAL-0" => UsageExpr(COMP())
      case "COMP-1" | "COMPUTATIONAL-1" => UsageExpr(COMP1())
      case "COMP-2" | "COMPUTATIONAL-2" => UsageExpr(COMP2())
      case "COMP-3" | "COMPUTATIONAL-3" | "PACKED-DECIMAL" => UsageExpr(COMP3())
      case "COMP-4" | "COMPUTATIONAL-4" => UsageExpr(COMP4())
      case "COMP-5" | "COMPUTATIONAL-5" => UsageExpr(COMP5())
      case "DISPLAY" => UsageExpr(DISPLAY())
      case "BINARY" => UsageExpr(BINARY())
      case _ => throw new RuntimeException("Unknown Usage literal " + ctx.usageLiteral().getText)
    }
  }

  override def visitGroup(ctx: copybook_parser.GroupContext): Expr = {
    val section = ctx.section.getText.toInt
    val parent: Group = getParentFromLevel(section)

    assert(ctx.redefines.size() < 2)
    assert(ctx.usage.size() < 2)
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

    val usage: Option[Usage] = ctx.usage().asScala.toList match {
      case Nil => None
      case x :: _ => Some(visitUsage(x).value)
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

  override def visitPic(ctx: copybook_parser.PicContext): PicExpr = {
    if (ctx.alpha_x() != null) {
      visitAlpha_x(ctx.alpha_x())
    }
    else if (ctx.alpha_x() != null) {
      visitAlpha_x(ctx.alpha_x())
    }
    else {
      val numeric = replaceDecimal0(visit(ctx.sign_precision_9()).asInstanceOf[PicExpr])
      ctx.usage() match {
        case null => numeric
        case x => replaceUsage(numeric, visitUsage(x).value)
      }
    }
  }

  override def visitAlpha_x(ctx: copybook_parser.Alpha_xContext): PicExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    PicExpr(AlphaNumeric(char, len, None, Some(enc), Some(ctx.getText)))
  }

  override def visitAlpha_a(ctx: copybook_parser.Alpha_aContext): PicExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    PicExpr(AlphaNumeric(char, len, None, Some(enc), Some(ctx.getText)))
  }

  override def visitTrailing_sign(ctx: copybook_parser.Trailing_signContext): PicExpr = {
    val prec = visit(ctx.precision_9()).asInstanceOf[PicExpr]
    ctx.plus_minus() match {
      case null => prec
      case _ => replaceSign(prec, 'T')
    }
  }

  override def visitLeading_sign(ctx: copybook_parser.Leading_signContext): PicExpr = {
    val prec = visit(ctx.precision_9()).asInstanceOf[PicExpr]
    ctx.plus_minus() match {
      case null => prec
      case _ => replaceSign(prec, 'L')
    }
  }

  override def visitSeparate_sign(ctx: copybook_parser.Separate_signContext): SepSignExpr = {
    if(ctx.LEADING() != null)
      SepSignExpr('L')
    else
      SepSignExpr('T')
  }

  override def visitPrecision_9_nines(ctx: copybook_parser.Precision_9_ninesContext): PicExpr = {
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

  override def visitPrecision_9_ss(ctx: copybook_parser.Precision_9_ssContext): PicExpr = {
    if (ctx.getText contains "P")
      throw new RuntimeException("Scaled numbers not supported yet")

    ctx.getText match {
      case numericSPicRegexScaled(s, nine, scale) => PicExpr(
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

  override def visitPrecision_9_zs(ctx: copybook_parser.Precision_9_zsContext): PicExpr = {
    throw new RuntimeException("Zero numeric not supported yet")
  }

  override def visitPrecision_9_explicit_dot(ctx: copybook_parser.Precision_9_explicit_dotContext): PicExpr = {
    val numericSPicRegexExplicitDot(s, nine1, nine2) = ctx.getText
    PicExpr(
      fromNumericSPicRegexExplicitDot(s, nine1, nine2).copy(originalPic = Some(ctx.getText))
    )
  }

  override def visitPrecision_9_decimal_scaled(ctx: copybook_parser.Precision_9_decimal_scaledContext): PicExpr = {
    if (ctx.getText contains "P")
      throw new RuntimeException("Scaled numbers not supported yet")

    val numericSPicRegexDecimalScaled(s, nine1, scale, nine2) = ctx.getText
    PicExpr(
      fromNumericSPicRegexDecimalScaled(s, nine1, scale, nine2).copy(originalPic = Some(ctx.getText))
    )
  }

  override def visitPrecision_9_scaled(ctx: copybook_parser.Precision_9_scaledContext): PicExpr = {
    if (ctx.getText contains "P")
      throw new RuntimeException("Scaled numbers not supported yet")

    val numericSPicRegexScaled(s, text, scale) = ctx.getText
    PicExpr(
      fromNumericSPicRegexScaled(s, text, scale).copy(originalPic = Some(ctx.getText))
    )
  }

  override def visitPrecision_9_scaled_lead(ctx: copybook_parser.Precision_9_scaled_leadContext): PicExpr = {
    val numericSPicRegexDecimalScaledLead(s, scale, nine) = ctx.getText
    PicExpr(
      fromNumericSPicRegexDecimalScaledLead(s, scale, nine)
    )
  }

  override def visitPrecision_z_explicit_dot(ctx: copybook_parser.Precision_z_explicit_dotContext): PicExpr = {
    throw new RuntimeException("Explicit decimal not supported yet")
  }

  override def visitPrecision_z_decimal_scaled(ctx: copybook_parser.Precision_z_decimal_scaledContext): PicExpr = {
    throw new RuntimeException("Zero numeric not supported yet")
  }

  override def visitPrecision_z_scaled(ctx: copybook_parser.Precision_z_scaledContext): PicExpr = {
    throw new RuntimeException("Zero numeric not supported yet")
  }


  override def visitPrimitive(ctx: copybook_parser.PrimitiveContext): Expr = {
    val section = ctx.section.getText.toInt
    val parent: Group = getParentFromLevel(section)

    assert(ctx.redefines.size() < 2)
    assert(ctx.usage.size() < 2)
    assert(ctx.occurs.size() < 2)
    assert(ctx.separate_sign.size() < 2)
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

    pic = ctx.separate_sign().asScala.toList match {
      case Nil => pic
      case x :: _ if !isSignSeparate(pic.value) => replaceSign(pic, visitSeparate_sign(x).value)
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

  override def visitLevel66statement(ctx: copybook_parser.Level66statementContext): Expr = {
    throw new RuntimeException("Renames not supported yet")
  }
}





