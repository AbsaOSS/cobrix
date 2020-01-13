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

package za.co.absa.cobrix.cobol.parser.examples.generators.model

object CommonLists {

  val currencies: Seq[String] =
    Seq("ZAR", "ZAR", "ZAR", "ZAR", "ZAR", "ZAR", "ZAR", "ZAR", "USD", "EUR", "GBP", "CAD", "CHF", "CZK", "CYN")

  val companies: Seq[Company] = Seq(
    Company("ABCD Ltd.", "0039887123", "74 Lawn ave., New York"),
    Company("ECSRONO", "0039567812", "123/B Prome str., Denver"),
    Company("ZjkLPj", "0034412331", "5574, Tokyo"),
    Company("Eqartion Inc.", "0039003991", "871A Forest ave., Toronto"),
    Company("Test Bank", "0092317899", "1 Garden str., London"),
    Company("Pear GMBH.", "0002377771", "107 Labe str., Berlin"),
    Company("Beiereqweq.", "0123330087", "901 Ztt, Munich"),
    Company("Joan Q & Z", "0039887123", "10 Sandton, Johannesburg"),
    Company("Robotrd Inc.", "0039801988", "2 Park ave., Johannesburg"),
    Company("Beierbauh.", "0038903321", "2 G. str., Johannesburg"),
    Company("Delta Pivovar", "0021213441", "74 Staromestka., Prague"),
    Company("Xingzhoug", "8822278911", "74 Qing ave., Beijing")
  )

  val companiesWithNonPrintableCharacters: Seq[Company] = Seq(
    Company("\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008",     "0039887123", "74 Lawn ave., New York"),
    Company("\u0009\u000a\u000b\u000c\u000d\u000e\u000f\u0010",     "0039567812", "123/B Prome str., Denver"),
    Company("\u0009\u000a\u000b\u000c\u000d\u000e\u000f\u0010",     "0034412331", "5574, Tokyo"),
    Company("\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018",     "0092317899", "1 Garden str., London"),
    Company("\u0019\u001a\u001b\u001c\u001d\u001e\u001f\u0020",     "0002377771", "107 Labe str., Berlin"),
    Company("\u0021\"\u0023\u0024\u0025\u0026\u0027\u0028",     "0123330087", "901 Ztt, Munich"),
    Company("\u0029\u002a\u002b\u002c\u002d\u002e\u002f\u0030",     "0039887123", "10 Sandton, Johannesburg"),
    Company("\u0031\u0032\u0033\u0034\u0035\u0036\u0037\u0038",     "0039801988", "2 Park ave., Johannesburg"),
    Company("\u0039\u003a\u003b\u003c\u003d\u003e\u003f\u0040",     "0038903321", "2 G. str., Johannesburg")
  )

  val departments: Seq[String] = Seq(
    "Executive",
    "Finance",
    "Operations",
    "Development",
    "Sales",
    "Marketing",
    "Research",
    "Risk Management",
    "Production",
    "Logistics",
    "Transportation",
    "Planning",
    "Engineering",
    "Accounting",
    "Legal",
    "Compliance",
    "Creative"
  )


  val firstNames: Seq[String] = Seq(
    "Jene",
    "Maya",
    "Starr",
    "Lynell",
    "Eliana",
    "Tyesha",
    "Beatrice",
    "Otelia",
    "Timika",
    "Wilbert",
    "Mindy",
    "Sunday",
    "Tyson",
    "Cliff",
    "Mabelle",
    "Verdie",
    "Sulema",
    "Alona",
    "Suk",
    "Deandra",
    "Doretha",
    "Cassey",
    "Janiece",
    "Deshawn",
    "Willis",
    "Carrie",
    "Gabriele",
    "Inge",
    "Edyth",
    "Estelle"
  )

  val lastNames: Seq[String] = Seq(
    "Corle",
    "Mackinnon",
    "Mork",
    "Shapiro",
    "Boettcher",
    "Flatt",
    "Acuna",
    "Thorpe",
    "Riojas",
    "Lepe",
    "Maxim",
    "Gagliano",
    "Benally",
    "Ortego",
    "Winburn",
    "Sauve",
    "Concannon",
    "Newcombe",
    "Boehme",
    "Hisle",
    "Godfrey",
    "Wallingford",
    "Debow",
    "Bourke",
    "Deveau",
    "Batman",
    "Norgard",
    "Tumlin",
    "Celestin",
    "Brandis"
  )

  val roles: Seq[String] = Seq(
    "CEO",
    "CFO",
    "CTO",
    "COO",
    "VP of Sales",
    "VP of Operations",
    "VP of Marketing",
    "VP of Development",
    "VP of Legal",
    "VP of Accounting",
    "director",
    "managing director",
    "software developer",
    "software engineer",
    "big data engineer",
    "devops",
    "support",
    "project manager",
    "scrum master",
    "sales",
    "copyrightor",
    "accountant",
    "analytic",
    "legal",
    "assistant",
    "researcher",
    "specialist"
  )

  val contractStates: Seq[String] = Seq(
    "Unsigned",
    "Signed",
    "Progress",
    "Rejected",
    "Done",
    "Archived"
  )

}
