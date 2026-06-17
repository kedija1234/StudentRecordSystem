# Student Record Management System
### Java File I/O — OOP Home Test

---

## System Design Overview

The system is built around **three layers**, each with a single, clear job:

```
┌─────────────────────────────────────────────────┐
│             Main.java  (UI Layer)               │
│   Handles menu, user input, output formatting   │
├─────────────────────────────────────────────────┤
│         StudentManager.java  (Logic Layer)      │
│   CRUD operations, validation, GPA reports      │
├─────────────────────────────────────────────────┤
│          FileManager.java  (Storage Layer)      │
│   All File I/O: text, binary, object files      │
└─────────────────────────────────────────────────┘
                    │ uses │
             Student.java (Model)
         Data fields + serialization logic
```

This separation means: if you want to swap a console UI for a GUI, only `Main.java` changes. If you want to switch from files to a database, only `FileManager.java` changes.

---

## Class Responsibilities

### `Student.java`
- Plain data object (ID, Name, Department, GPA)
- Implements `Serializable` — required for object serialization
- `toTextLine()` / `fromTextLine()` handle conversion to/from pipe-delimited strings
- Uses `|` as separator (not `,`) because department names may contain commas

### `FileManager.java`
All I/O is here. Three storage formats are used:

| Format | Classes Used | File | Human Readable? |
|--------|-------------|------|----------------|
| Text | `Scanner`, `PrintWriter`, `BufferedReader/Writer` | `students.txt` | ✅ Yes |
| Binary | `DataInputStream`, `DataOutputStream`, `Buffered*` | `students.dat` | ❌ No |
| Object | `ObjectInputStream`, `ObjectOutputStream`, `Buffered*` | `students.ser` | ❌ No |

**Why three formats?**
They demonstrate three completely different Java I/O philosophies:
- **Text**: portable, editable by hand, but slower to parse
- **Binary**: compact, fast, but tightly coupled to the write order
- **Object**: easiest to code (Java handles everything), but tightly coupled to the class version

**Buffered streams** are used throughout. Without them, every `write()` call hits the disk directly — buffers collect data in memory first and flush in one go, dramatically reducing disk operations.

### `StudentManager.java`
- Holds the **in-memory `List<Student>`** — the single source of truth during a session
- Every mutating operation (add/update/delete) calls `syncAllFiles()` immediately so all three file formats stay consistent
- Uses **Java Streams** for the GPA report (`mapToDouble` + `DoubleSummaryStatistics`) — one pass to compute min, max, average, and count simultaneously

### `Main.java`
- Menu loop runs until the user exits
- **Every menu action has its own try-catch** so one error doesn't crash the session
- Three exception types are handled differently:
  - `IllegalArgumentException` → business rule violation (bad input, duplicate ID)
  - `IOException` → file system problem (disk full, permission denied)
  - `ClassNotFoundException` → deserialization failure (class version mismatch)

---

## File Structure

```
StudentRecordSystem/
├── src/main/java/student/
│   ├── Student.java          ← Model
│   ├── FileManager.java      ← Storage layer
│   ├── StudentManager.java   ← Business logic
│   └── Main.java             ← Entry point / UI
│
├── data/
│   ├── students.txt          ← Primary text file (human-readable)
│   ├── students.dat          ← Binary file
│   ├── students.ser          ← Serialized object file
│   └── backup/
│       └── backup_YYYYMMDD_HHmmss.txt  ← Timestamped backups
│
└── README.md
```

---

## How to Compile and Run

```bash
# From the project root
javac -d out src/main/java/student/*.java

# Run
java -cp out student.Main
```

---

## Key Java Concepts Demonstrated

| Concept | Where |
|---------|-------|
| `Serializable` interface | `Student.java` |
| `Scanner` + `PrintWriter` | `FileManager.loadFromTextFile/saveToTextFile` |
| `DataInputStream/OutputStream` | `FileManager.loadFrom/saveToBinaryFile` |
| `ObjectInputStream/OutputStream` | `FileManager.loadWith/saveWithSerialization` |
| Buffered streams wrapping raw streams | All I/O methods in `FileManager` |
| `File` class properties | `FileManager.displayFileProperties` |
| `File.mkdirs()` auto-create directories | `FileManager.initializeFiles` |
| `try-with-resources` (auto-close) | All stream usage |
| Java Streams + `DoubleSummaryStatistics` | `StudentManager.generateReport` |
| Layered exception handling | `Main.java` switch block |

---

## Design Decisions

**Why sync all three files on every change?**
Simplicity. For a small student dataset, the cost is negligible, and it guarantees all three formats are always consistent. A production system would use a proper database with transactions instead.

**Why `|` as a delimiter in the text file?**
Commas are common in names and department titles. Pipe `|` is almost never used in natural text, making it a safer delimiter. A more robust approach would be CSV quoting, but `|` keeps the parser simple.

**Why `serialVersionUID = 1L`?**
This is a version stamp for the serialized class. If you add/remove fields in `Student.java` and try to read an old `.ser` file, Java will throw an `InvalidClassException`. Keeping track of this number makes you intentional about breaking changes.
