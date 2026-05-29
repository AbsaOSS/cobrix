# Mainframe versus PC files

One important thing to start with is that historically the notion of a “file” evolved very differently in the PC/Unix 
world versus the classic mainframe world.

The difference is not just technical; it reflects different philosophies of computing:

* PC/Unix systems evolved around **streams of bytes**
* Mainframe systems evolved around **business records**

That is why on mainframes the natural abstraction is often a *dataset of records*, not an unstructured byte stream.

---

# 1. PC / Unix notion of a file

In Unix, Linux, Windows, DOS, and similar systems, a file is fundamentally:

> a sequence of bytes with no inherent structure

The operating system does not know:

* where lines begin
* where records begin
* what fields exist
* whether the content is text, database pages, images, or executable code

For example:

```text
48 65 6C 6C 6F 0A 41 42 43
```

The OS sees only bytes.

Applications impose structure themselves:

* text editors interpret `\n`
* databases interpret pages
* CSV readers interpret commas
* compilers interpret syntax

This is called a **byte-stream model**.

Unix strongly standardized this model:

> “everything is a stream of bytes”

Even devices, sockets, and pipes follow this philosophy.

---

# 2. Mainframe notion of a file

Classic mainframe operating systems were designed primarily for:

* banking
* payroll
* insurance
* census systems
* airline reservations
* batch processing

These workloads naturally operate on:

* fixed forms
* account records
* transaction records
* indexed business data

So the OS itself became record-oriented.

A mainframe “file” (often called a **dataset**) is commonly:

> a collection of records with structure known by the OS

The operating system may know:

* record length
* whether records are fixed or variable
* block size
* indexing method
* access method

This is radically different from Unix.

---

# 3. Example: fixed-length records

Suppose customer records are exactly 80 bytes.

Mainframe OS:

```text
Record 1 = 80 bytes
Record 2 = 80 bytes
Record 3 = 80 bytes
```

The OS understands record boundaries.

Applications read:

```cobol
READ CUSTOMER-FILE
```

and receive one logical record.

No parsing of delimiters is necessary.

---

# 4. Indexed files

Many mainframe systems support indexed datasets natively.

Example:

```text
KEY = customer number
VALUE = customer record
```

The OS or access method handles:

* indexes
* record lookup
* sequential scans
* keyed access

This existed decades before modern relational databases became dominant.

---

# 5. Mainframe access methods

Classic IBM mainframes introduced sophisticated storage abstractions.

Important IBM access methods include:

| Access Method | Meaning                          |
| ------------- | -------------------------------- |
| BSAM          | Basic Sequential Access Method   |
| QSAM          | Queued Sequential Access Method  |
| VSAM          | Virtual Storage Access Method    |
| ISAM          | Indexed Sequential Access Method |

Especially important:

## VSAM

On IBM mainframes, IBM z/OS VSAM datasets may be:

* sequential
* indexed
* relative-record

VSAM is not “just files”.
It is closer to a lightweight storage engine built into the OS.

---

# 6. Record-oriented vs byte-stream I/O

## Unix

```c
read(fd, buffer, 4096);
```

Reads arbitrary bytes.

Application decides meaning.

---

## Mainframe COBOL

```cobol
READ EMPLOYEE-FILE
```

Reads exactly one logical record.

The OS/access method handles:

* blocking
* buffering
* locating records
* decoding record structure

---

# 7. Blocking

Mainframes historically optimized heavily for tape and expensive disk I/O.

Records were grouped into physical blocks.

Example:

```text
Block
 ├── Record
 ├── Record
 ├── Record
```

The OS knew:

* logical record size
* physical block size

Unix generally hides this from applications.

---

# 8. Which operating systems are record-oriented?

## Strongly record-oriented

### IBM mainframe systems

* z/OS
* OS/360
* MVS
* OS/390

These are the canonical record-oriented systems.

Datasets are fundamental there.

---

### VMS / OpenVMS

OpenVMS also has rich record-oriented files.

Supports:

* fixed records
* variable records
* indexed files

Very different from Unix.

---

### IBM i (AS/400)

IBM i is especially interesting.

Historically:

* “files” were closer to database tables
* the system deeply integrated DB2

Traditional filesystem semantics were secondary.

---

# 9. Which systems are NOT record-oriented?

## Unix and Unix-like systems

* Linux
* Windows
* macOS
* BSD
* Solaris
* AIX
* HP-UX

All use byte-stream files.

Text lines are conventions, not OS concepts.

# 11. Why byte-stream files are more common

The byte-stream model proved more flexible.

Advantages:

* simpler abstraction
* language independence
* easier portability
* works for arbitrary binary data
* composable tools and pipes

The Unix philosophy:

> keep the OS simple, push structure to applications

became dominant.

---

# 12. Why mainframes kept records

Business workloads strongly benefit from record semantics:

* COBOL integration
* fixed forms
* indexed retrieval
* batch processing
* guaranteed structure

Mainframes optimized for:

* reliability
* transaction throughput
* massive batch jobs

not developer flexibility.

---

# 13. Modern reality

Even on IBM mainframes today:

* Unix environments exist
* POSIX APIs exist
* byte-stream files exist

For example, z/OS contains:

* traditional datasets
* Unix System Services (USS)

So modern mainframes support both worlds simultaneously.

---

# 14. Simplified mental model

## Unix/Linux/Windows

```text
File = bytes
```

Structure belongs to applications.

---

## Classical mainframe systems

```text
File = collection of records
```

Structure partially belongs to the OS and access methods.

---

# 15. A subtle but important consequence

Because mainframe files have structure, many properties become part of metadata:

* record size
* key fields
* blocking
* access mode

Copying a file incorrectly may lose semantic meaning.

On Unix:

```bash
cp file1 file2
```

is usually enough.

On mainframes, copying datasets historically required preserving dataset attributes as well.
