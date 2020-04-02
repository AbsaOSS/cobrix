package za.co.absa.cobrix.cobol.reader.common

/** The object contains various constants related to the Cobol readers */
object Constants {

  // The Record_Id field is used to preserve the order or records in the original data.
  // Each new record has this field incremented by 1. Because binary files are processed
  // in parallel the exact increment for record ids between files is unknown. For this
  // reason the file increment is used. The first record id for each file will be
  // incremented by this value. The value of 2^32 makes it possible to process 2^31 files
  // having 2^32-1 records.
  val defaultFileRecordIdIncrement = 4294967296L  // 2^32

  // Tuning parameters
  val megabyte = 1048576

  // Minimum number of records in each file split
  val recordsPerIndexEntry = 50000

  // Default index entry size in MB
  val defaultIndexEntrySizeMB = 100

  // The maximum number of partitions for splitting a multisegment file
  val maxNumPartitions = 2048

  // Default number of partitions
  val defaultStreamBufferInMB = 30
}
