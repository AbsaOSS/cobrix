package za.co.absa.cobrix.cobol.parser.antlr

import scala.util.matching.Regex
import scala.collection.JavaConverters._
import za.co.absa.cobrix.cobol.parser.CopybookParser.CopybookAST
import za.co.absa.cobrix.cobol.parser.ast.datatype.{AlphaNumeric, Usage, Integral, Decimal}
import za.co.absa.cobrix.cobol.parser.ast.{Group, Statement}
import za.co.absa.cobrix.cobol.parser.common.ReservedWords

import scala.collection.mutable
import scala.collection.mutable.Stack


sealed trait Expr


class ParserVisitor() extends copybook_parserBaseVisitor[Expr] {
  /* expressions */
  case class NoOpExpr()
  case class Field(value: Statement) extends Expr
  case class IdentifierExpr(value: String) extends Expr
  case class OccursExpr(m: Int, M: Option[Int], dep: Option[String]) extends Expr
  case class UsageExpr(value: Usage.Value) extends Expr
  case class AlphaExpr(value: AlphaNumeric) extends Expr
  case class IntegerExpr(value: Integral) extends Expr
  case class DecimalExpr(value: Decimal) extends Expr

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
      case "COMP" | "COMPUTATIONAL" => UsageExpr(Usage.COMP)
      case "COMP-1" | "COMPUTATIONAL-1" => UsageExpr(Usage.COMP1)
      case "COMP-2" | "COMPUTATIONAL-2" => UsageExpr(Usage.COMP2)
      case "COMP-3" | "COMPUTATIONAL-3" | "PACKED-DECIMAL" => UsageExpr(Usage.COMP3)
      case "COMP-4" | "COMPUTATIONAL-4" => UsageExpr(Usage.COMP4)
      case "COMP-5" | "COMPUTATIONAL-5" => UsageExpr(Usage.COMP5)
      case "DISPLAY" => UsageExpr(Usage.DISPLAY)
      case "BINARY" => UsageExpr(Usage.BINARY)
      case _ => throw new RuntimeException("Unknown Usage literal " + ctx.usageLiteral().getText)
    }
  }

  override def visitGroup(ctx: copybook_parser.GroupContext): Expr = {
    // remove levels as needed
    val section = ctx.section.getText.toInt
    while (section <= levels.top.n) {
      levels.pop
    }
    val parent: Group = levels.top.el

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

    val usage: Option[Usage.Value] = ctx.usage().asScala.toList match {
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

  override def visitPic(ctx: copybook_parser.PicContext): Expr = {
    if (ctx.alpha_x() != null) {
      visitAlpha_x(ctx.alpha_x())
    }
    else if (ctx.alpha_x() != null) {
      visitAlpha_x(ctx.alpha_x())
    }
    else {
      val usage: Option[Usage.Value] = ctx.usage() match {
        case null => None
        case x => Some(visitUsage(x).value)
      }



      visitChildren(ctx)
    }
  }

  override def visitAlpha_x(ctx: copybook_parser.Alpha_xContext): AlphaExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    AlphaExpr(AlphaNumeric(char, len))
  }

  override def visitAlpha_a(ctx: copybook_parser.Alpha_aContext): AlphaExpr = {
    val text = ctx.getText
    val (char, len) = length(text)
    AlphaExpr(AlphaNumeric(char, len))
  }

  override def visitSign_precision_9(ctx: copybook_parser.Sign_precision_9Context): Expr = {

    visitChildren(ctx)
  }

}





