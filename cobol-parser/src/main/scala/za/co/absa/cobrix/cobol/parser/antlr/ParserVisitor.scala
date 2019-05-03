package za.co.absa.cobrix.cobol.parser.antlr

import scala.util.matching.Regex
import scala.collection.JavaConverters._
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, BINARY, COMP, COMP1, COMP2, COMP3, COMP4, COMP5, CobolType, DISPLAY, Decimal, Integral, Usage}
import za.co.absa.cobrix.cobol.parser.ast.{BinaryProperties, Group, Primitive, Statement}
import za.co.absa.cobrix.cobol.parser.common.ReservedWords
import za.co.absa.cobrix.cobol.parser.decoders.DecoderSelector
import za.co.absa.cobrix.cobol.parser.decoders.StringTrimmingPolicy.StringTrimmingPolicy
import za.co.absa.cobrix.cobol.parser.encoding.codepage.CodePage

import scala.collection.mutable
import scala.collection.mutable.Stack


sealed trait Expr


class ParserVisitor(stringTrimmingPolicy: StringTrimmingPolicy,
                    ebcdicCodePage: CodePage) extends copybook_parserBaseVisitor[Expr] {
  /* expressions */
  case class NoOpExpr()
  case class Field(value: Statement) extends Expr
  case class IdentifierExpr(value: String) extends Expr
  case class OccursExpr(m: Int, M: Option[Int], dep: Option[String]) extends Expr
  case class UsageExpr(value: Usage) extends Expr
  case class PicExpr(value: CobolType) extends Expr
//  case class AlphaExpr(value: AlphaNumeric) extends PicExpr
//  case class IntegerExpr(value: Integral) extends PicExpr
//  case class DecimalExpr(value: Decimal) extends PicExpr
  case class PrimitiveExpr(value: Primitive) extends Expr
  case class SepSignExpr(value: Char) extends Expr

  /* aux classes */
  case class Level(n: Int, el: Group)
  private var levels: Stack[Level] = Stack()
  var ast: CopybookAST = Group.root

  /* regex */
  val lengthRegex: Regex = "([9XPZ])\\((\\d+)\\)|([9XPZ]+)".r

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

  def getParentFromLevel(section: Int): Group = {
    while (section <= levels.top.n) {
      levels.pop
    }
    levels.top.el
  }


  override def visitMain(ctx: copybook_parser.MainContext): Expr = {
    // initialize AST
    ast = Group.root.copy()(None)
    levels = Stack(Level(0, ast))

    visitChildren(ctx)
  }

  override def visitIdentifier(ctx: copybook_parser.IdentifierContext): IdentifierExpr = {
    IdentifierExpr(ctx.getText.replace("'", "").replace("\"", ""))
  }

  override def visitOccurs(ctx: copybook_parser.OccursContext): OccursExpr = {
    val m = ctx.integerLiteral.getText.toInt

    OccursExpr(m, Some(0), None)
  }

  override def visitUsage(ctx: copybook_parser.UsageContext): UsageExpr = {
    ctx.usageLiteral().getText match {
      case "COMP" | "COMPUTATIONAL" => UsageExpr(COMP())
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
      val usage: Option[Usage] = ctx.usage() match {
        case null => None
        case x => Some(visitUsage(x).value)
      }

      // FIX THIS -- just to pass for now
      PicExpr(AlphaNumeric("X", 100))
    }
  }

  override def visitAlpha_x(ctx: copybook_parser.Alpha_xContext): PicExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    PicExpr(AlphaNumeric(char, len))
  }

  override def visitAlpha_a(ctx: copybook_parser.Alpha_aContext): PicExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    PicExpr(AlphaNumeric(char, len))
  }

  override def visitSign_precision_9(ctx: copybook_parser.Sign_precision_9Context): Expr = {
    visitChildren(ctx)
  }

  override def visitSeparate_sign(ctx: copybook_parser.Separate_signContext): SepSignExpr = {
    if(ctx.LEADING() != null)
      SepSignExpr('L')
    else
      SepSignExpr('T')
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

    val sepSign: Option[Char] = ctx.separate_sign().asScala.toList match {
      case Nil => None
      case x :: _ => Some(visitSeparate_sign(x).value)
    }

    val pic: CobolType = visitPic(ctx.pic(0)).value

    val prim = Primitive(
      section,
      identifier,
      ctx.start.getLine,
      pic,
      redefines,
      isRedefined = false,
      if (occurs.isDefined) Some(occurs.get.m) else None,
      if (occurs.isDefined) occurs.get.M else None,
      if (occurs.isDefined) occurs.get.dep else None,
      isDependee = false,
      identifier.toUpperCase() == ReservedWords.FILLER,
      DecoderSelector.getDecoder(pic, stringTrimmingPolicy, ebcdicCodePage)
      ) (Some(parent))

    parent.children.append(prim)

    PrimitiveExpr(prim)
  }


}





