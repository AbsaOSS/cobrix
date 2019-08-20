package za.co.absa.cobrix.cobol.parser.policies

case class CommentPolicy(
                          truncateComments: Boolean = true,
                          commentsUpToChar: Int = 6,
                          commentsAfterChar: Int = 72
                        )
