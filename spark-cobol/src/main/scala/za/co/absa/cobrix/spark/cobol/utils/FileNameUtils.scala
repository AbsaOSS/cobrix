package za.co.absa.cobrix.spark.cobol.utils

object FileNameUtils {
  private val LOCALFS_PREFIX = "file://"

  def getCopyBookFileName(fileNameURI: String):(Boolean, String) = {
    val isLocalFS = fileNameURI.toLowerCase.startsWith(LOCALFS_PREFIX)
    val copyBookFileName = if (isLocalFS)
      fileNameURI.drop(LOCALFS_PREFIX.length)
    else
      fileNameURI
    (isLocalFS, copyBookFileName)
  }

}
