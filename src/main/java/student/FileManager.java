package student;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * FileManager handles ALL file I/O for the system.
 *
 * Three storage strategies are used — all storing the same data
 * in different formats — to demonstrate the three Java I/O approaches:
 *
 *   1. TEXT FILE    — human-readable, pipe-delimited lines
 *   2. BINARY FILE  — primitive types written as raw bytes (not readable in a text editor)
 *   3. OBJECT FILE  — entire Java objects serialized (requires the same class to deserialize)
 *
 * Buffered streams wrap raw streams everywhere to reduce disk access
 * (data is collected in a memory buffer first, then flushed in one go).
 */
public class FileManager {

    // All paths live here so they're easy to change in one place
    private static final String DATA_DIR    = "data/";
    private static final String BACKUP_DIR  = "data/backup/";
    private static final String TEXT_FILE   = DATA_DIR + "students.txt";
    private static final String BINARY_FILE = DATA_DIR + "students.dat";
    private static final String OBJECT_FILE = DATA_DIR + "students.ser";
    private static final String BACKUP_FILE = BACKUP_DIR + "students_backup.txt";

    // ─── INITIALISATION ──────────────────────────────────────────────────────

    /**
     * Called once at startup.
     * File.mkdirs() creates the directory AND any missing parent directories.
     * createNewFile() only creates the file if it doesn't already exist.
     */
    public static void initializeFiles() throws IOException {
        new File(DATA_DIR).mkdirs();
        new File(BACKUP_DIR).mkdirs();

        for (String path : new String[]{TEXT_FILE, BINARY_FILE, OBJECT_FILE, BACKUP_FILE}) {
            File f = new File(path);
            if (f.createNewFile()) {
                System.out.println("Created: " + f.getAbsolutePath());
            }
        }
    }

    // ─── TEXT FILE (Scanner + PrintWriter) ───────────────────────────────────

    /**
     * Reads every line from the text file and converts each back to a Student.
     * Scanner is used here because it's great for line-by-line text parsing.
     * BufferedReader wraps the FileReader to make reads faster.
     */
    public static List<Student> loadFromTextFile() throws IOException {
        List<Student> students = new ArrayList<>();
        File file = new File(TEXT_FILE);
        if (!file.exists() || file.length() == 0) return students;

        // try-with-resources: auto-closes the scanner when the block ends,
        // even if an exception is thrown — no need for a finally block.
        try (Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    students.add(Student.fromTextLine(line));
                }
            }
        }
        return students;
    }

    /**
     * Overwrites the text file with the current list.
     * PrintWriter gives us println() which is convenient for text output.
     * BufferedWriter reduces flush calls by buffering writes in memory.
     */
    public static void saveToTextFile(List<Student> students) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(TEXT_FILE)))) {
            for (Student s : students) {
                writer.println(s.toTextLine());
            }
        }
    }

    // ─── BINARY FILE (DataInputStream + DataOutputStream) ────────────────────

    /**
     * Binary format stores each field as a raw type:
     *   writeUTF()    → writes a String as a 2-byte length + UTF bytes
     *   writeDouble() → writes 8 bytes for the double value
     * Advantage: smaller file size, faster to parse.
     * Drawback: unreadable without the matching read code.
     */
    public static void saveToBinaryFile(List<Student> students) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(BINARY_FILE)))) {

            // Write the count first — so we know how many records to read back
            dos.writeInt(students.size());

            for (Student s : students) {
                dos.writeUTF(s.getStudentId());
                dos.writeUTF(s.getName());
                dos.writeUTF(s.getDepartment());
                dos.writeDouble(s.getGpa());
            }
        }
    }

    public static List<Student> loadFromBinaryFile() throws IOException {
        List<Student> students = new ArrayList<>();
        File file = new File(BINARY_FILE);
        if (!file.exists() || file.length() == 0) return students;

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {

            int count = dis.readInt();          // how many records were saved
            for (int i = 0; i < count; i++) {
                String id   = dis.readUTF();
                String name = dis.readUTF();
                String dept = dis.readUTF();
                double gpa  = dis.readDouble();
                students.add(new Student(id, name, dept, gpa));
            }
        }
        return students;
    }

    // ─── OBJECT SERIALIZATION (ObjectOutputStream + ObjectInputStream) ────────

    /**
     * Serialization converts the entire ArrayList of Student objects into bytes.
     * This is the simplest approach for saving complex objects — Java handles
     * everything automatically, as long as Student implements Serializable.
     * Drawback: tightly coupled to the class — if Student changes, old .ser
     * files may become incompatible (that's what serialVersionUID guards against).
     */
    public static void saveWithSerialization(List<Student> students) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(OBJECT_FILE)))) {
            oos.writeObject(students);   // writes the whole list in one call
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Student> loadWithSerialization() throws IOException, ClassNotFoundException {
        File file = new File(OBJECT_FILE);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            // readObject() returns Object — cast is safe because we know what we wrote
            return (List<Student>) ois.readObject();
        }
    }

    // ─── BUFFERED BACKUP ─────────────────────────────────────────────────────

    /**
     * Creates a timestamped backup of the text file.
     * We read the source with BufferedReader and write to destination
     * with BufferedWriter — this is the classic "buffered copy" pattern.
     */
    public static void createBackup(List<Student> students) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupPath = BACKUP_DIR + "backup_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupPath))) {
            writer.write("=== BACKUP CREATED: " + new Date() + " ===");
            writer.newLine();
            for (Student s : students) {
                writer.write(s.toTextLine());
                writer.newLine();
            }
        }
        System.out.println("Backup saved → " + backupPath);
    }

    // ─── FILE PROPERTIES ─────────────────────────────────────────────────────

    /**
     * Uses the File class API to display metadata — not the file contents,
     * just information ABOUT the file (size, path, dates, permissions).
     */
    public static void displayFileProperties() {
        String[] paths = {TEXT_FILE, BINARY_FILE, OBJECT_FILE};
        String[] labels = {"Text File", "Binary File", "Object File"};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println("\n╔══════════════════ FILE PROPERTIES ══════════════════╗");
        for (int i = 0; i < paths.length; i++) {
            File f = new File(paths[i]);
            System.out.println("  ► " + labels[i]);
            System.out.println("    Name       : " + f.getName());
            System.out.println("    Path       : " + f.getAbsolutePath());
            System.out.println("    Size       : " + f.length() + " bytes");
            System.out.println("    Readable   : " + f.canRead());
            System.out.println("    Writable   : " + f.canWrite());
            System.out.println("    Modified   : " + sdf.format(new Date(f.lastModified())));
            System.out.println();
        }
        System.out.println("╚═════════════════════════════════════════════════════╝");
    }

    // Expose paths so other classes can reference them if needed
    public static String getTextFilePath()   { return TEXT_FILE; }
    public static String getBinaryFilePath() { return BINARY_FILE; }
    public static String getObjectFilePath() { return OBJECT_FILE; }
}
