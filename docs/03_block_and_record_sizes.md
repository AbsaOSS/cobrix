# Record and block size limitations on mainframes
Historically, maximum record and block sizes on mainframes depended heavily on:

* the operating system
* the access method
* the device type
* whether records were fixed or variable
* whether spanned records were allowed

The most important ecosystem is IBM mainframes, especially z/OS, because much of the terminology originates there.

The limits are surprisingly large for the era, but they are very different from Unix filesystems because:

* “record”
* “block”
* “control interval”
* “track”
* “extent”

all matter independently.

---

# 1. Logical records vs physical blocks

Mainframe systems distinguish:

| Concept        | Meaning                                          |
| -------------- | ------------------------------------------------ |
| Logical Record | Application-visible record                       |
| Block          | Physical I/O unit containing one or more records |

Example:

```text
Block
 ├── Record
 ├── Record
 ├── Record
```

Applications usually work with records.
Disks and tapes work with blocks.

---

# 2. IBM traditional datasets (QSAM/BSAM)

In classic IBM systems:

* Record size = LRECL (logical record length)
* Block size = BLKSIZE

---

# 3. Historical record size limits

## Fixed-length records

Classic QSAM sequential datasets often had:

```text
Max LRECL ≈ 32,760 bytes
```

This became a famous IBM limit.

Why not exactly 32768?
Because some bytes are used internally for control information.

---

## Variable-length records

Variable records include a Record Descriptor Word (RDW):

```text
4-byte RDW + data
```

So maximum user data is slightly smaller.

Typical maximum:

```text
32756 bytes of payload
```

or nearby depending on format.

---

# 4. Block size limits

Traditional IBM access methods commonly used:

```text
Maximum BLKSIZE = 32760 bytes
```

Again:

* near 32 KB
* slightly reduced for metadata/alignment

This limit existed for decades.

---

# 5. Why 32 KB?

Mostly historical architecture reasons:

* 16-bit fields
* channel hardware
* I/O control structures
* memory efficiency

Remember:
these systems were designed in the 1960s.

Yet they processed enormous workloads efficiently because:

* records were compact
* blocking reduced I/O operations
* sequential batch processing dominated

---

# 6. Spanned records

IBM later introduced:

* spanned variable records

Meaning:
a logical record may continue across blocks.

Without spanning:

```text
record <= block size
```

With spanning:

```text
record may span multiple blocks
```

This allowed larger logical records.

---

# 7. VSAM limits

VSAM in z/OS is more sophisticated.

Instead of plain blocks, VSAM uses:

* Control Intervals (CI)
* Control Areas (CA)

Typical CI sizes:

* 4 KB
* 8 KB
* 16 KB
* 32 KB

Historically maximum CI size:

```text
32 KB
```

Later extended in some configurations.

---

# 8. Modern large block interface (LBI)

Modern z/OS introduced:

* Large Block Interface

allowing blocks much larger than 32 KB.

Typical modern limits:

```text
BLKSIZE up to 1 MB+
```

depending on subsystem/access method.

This was important for:

* tape
* high-performance sequential I/O
* large buffers

---

# 9. Tape systems

Tape historically used huge blocks for efficiency.

Example:

* 64 KB
* 256 KB
* 1 MB

especially on newer tape drives.

Large blocks reduce:

* inter-record gaps
* tape motion overhead

---

# 10. OpenVMS record sizes

OpenVMS also supports record-oriented files.

Typical limits depended on:

* RMS (Record Management Services)
* record type

Variable records could often exceed:

* tens of KB
* sometimes much larger with segmentation

---

# 11. IBM i (AS/400)

IBM i historically treated files almost like database tables.

Record limits depended heavily on:

* DDS definitions
* DB2 implementation

Modern limits can be very large:

* many MB per row in some cases

because the system evolved toward integrated relational storage.

---

# 12. Comparison with Unix/Linux

Unix filesystems generally do NOT impose:

* logical record size
* block-visible limits

Applications define records themselves.

Filesystem block sizes:

* 4 KB
* 8 KB
* etc.

are mostly invisible to applications.

A Unix application can write:

```c
write(fd, huge_buffer, 10_MB);
```

and the kernel splits it internally.

In classical mainframe systems, the application often cared deeply about:

* block size
* record size
* blocking factor

because performance depended heavily on them.

---

# 13. Blocking factor

A major tuning parameter:

```text
blocking factor =
  records per block
```

Example:

```text
80-byte records
3200-byte block
=> 40 records/block
```

This was critical for performance.

COBOL programmers and JCL authors tuned these values carefully.

---

# 14. Card-image heritage

A famous historical artifact:

```text
80-byte records
```

Why?

Because IBM punch cards had:

* 80 columns

So many systems standardized on:

* 80-byte logical records

This legacy survived for decades.

---

# 15. Why records stayed relatively small

Mainframe workloads traditionally processed:

* transactions
* account entries
* payroll rows
* inventory items

These are naturally compact structured records.

Very large objects:

* images
* videos
* documents

were historically uncommon in enterprise computing.

So optimizing around:

* millions of small records

made sense.

---

# 16. Modern reality

Modern mainframes now support:

* POSIX files
* large byte streams
* huge datasets
* DBMS pages
* object storage interfaces

But classic record-oriented APIs still exist because:

* COBOL applications
* batch jobs
* banking systems

still depend on them.
