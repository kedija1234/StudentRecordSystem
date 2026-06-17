package student;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * StudentManager is the "brain" of the system.
 *
 * It holds the in-memory list of students and provides all
 * CRUD operations and report generation.
 *
 * DESIGN DECISION:
 *   We work with an in-memory List<Student> during a session.
 *   Every mutating operation (add/update/delete) immediately
 *   syncs changes to ALL three file formats so they stay consistent.
 *   This is simpler than lazy-writing but fine for a small dataset.
 */
public class StudentManager {

    // The "master" list — all operations read from / write to this
    private List<Student> students = new ArrayList<>();

    // ─── LOAD ─────────────────────────────────────────────────────────────────

    /**
     * On startup, load from the text file (our primary source of truth).
     * If the text file is empty but the others aren't, we could fall back —
     * but for this project, we keep it simple: text file is primary.
     */
    public void loadStudents() throws IOException {
        students = FileManager.loadFromTextFile();
        System.out.println("Loaded " + students.size() + " student(s) from file.");
    }

    // ─── SAVE (sync all formats) ───────────────────────────────────────────────

    /**
     * After any change, call this to persist to all three formats.
     * ClassNotFoundException is caught here because binary/object files
     * don't throw it on WRITE — only text/binary writes are done here.
     */
    private void syncAllFiles() throws IOException {
        FileManager.saveToTextFile(students);
        FileManager.saveToBinaryFile(students);
        FileManager.saveWithSerialization(students);
    }

    // ─── ADD ──────────────────────────────────────────────────────────────────

    /**
     * Validates that:
     *   - The ID isn't blank or already taken
     *   - GPA is in the valid range [0.0, 4.0]
     * Then adds to the list and syncs to files.
     */
    public void addStudent(String id, String name, String dept, double gpa) throws IOException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (findById(id) != null) {
            throw new IllegalArgumentException("A student with ID '" + id + "' already exists.");
        }
        if (gpa < 0.0 || gpa > 4.0) {
            throw new IllegalArgumentException("GPA must be between 0.0 and 4.0.");
        }

        students.add(new Student(id.trim(), name.trim(), dept.trim(), gpa));
        syncAllFiles();
        System.out.println("✔ Student added: " + id);
    }

    // ─── SEARCH ───────────────────────────────────────────────────────────────

    /**
     * Linear search through the list by ID (case-insensitive).
     * Returns null if not found — callers check for null.
     */
    public Student findById(String id) {
        return students.stream()
            .filter(s -> s.getStudentId().equalsIgnoreCase(id.trim()))
            .findFirst()
            .orElse(null);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    /**
     * Finds the student by ID, then applies only the fields
     * that the caller provided (non-null, non-empty).
     * Passing null for a field means "don't change it".
     */
    public void updateStudent(String id, String newName, String newDept, Double newGpa)
            throws IOException {

        Student s = findById(id);
        if (s == null) {
            throw new IllegalArgumentException("No student found with ID: " + id);
        }

        if (newName != null && !newName.trim().isEmpty()) s.setName(newName.trim());
        if (newDept != null && !newDept.trim().isEmpty()) s.setDepartment(newDept.trim());
        if (newGpa != null) {
            if (newGpa < 0.0 || newGpa > 4.0)
                throw new IllegalArgumentException("GPA must be between 0.0 and 4.0.");
            s.setGpa(newGpa);
        }

        syncAllFiles();
        System.out.println("✔ Student updated: " + id);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    /**
     * removeIf() is a clean, modern way to filter a list in-place.
     * It removes every element that matches the predicate.
     * Returns true if anything was actually removed.
     */
    public void deleteStudent(String id) throws IOException {
        boolean removed = students.removeIf(
            s -> s.getStudentId().equalsIgnoreCase(id.trim())
        );
        if (!removed) {
            throw new IllegalArgumentException("No student found with ID: " + id);
        }
        syncAllFiles();
        System.out.println("✔ Student deleted: " + id);
    }

    // ─── DISPLAY ──────────────────────────────────────────────────────────────

    public void displayAll() {
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        System.out.println("\n╔══════════════════════ STUDENT RECORDS ══════════════════════╗");
        students.forEach(s -> System.out.println("  " + s));
        System.out.println("╚═════════════════════════════════════════════════════════════╝");
        System.out.println("  Total: " + students.size() + " student(s)\n");
    }

    // ─── REPORT ───────────────────────────────────────────────────────────────

    /**
     * Uses Java Streams for concise aggregation:
     *   - mapToDouble extracts the GPA from each student as a primitive double stream
     *   - DoubleSummaryStatistics computes min, max, average, count in one pass
     *
     * Much cleaner than writing four separate loops.
     */
    public void generateReport() {
        if (students.isEmpty()) {
            System.out.println("No data to generate a report.");
            return;
        }

        DoubleSummaryStatistics stats = students.stream()
            .mapToDouble(Student::getGpa)
            .summaryStatistics();

        // Find the actual students with the highest and lowest GPA
        Student top = students.stream()
            .max(Comparator.comparingDouble(Student::getGpa))
            .orElse(null);

        Student bottom = students.stream()
            .min(Comparator.comparingDouble(Student::getGpa))
            .orElse(null);

        // Group count by department for a breakdown summary
        Map<String, Long> byDept = students.stream()
            .collect(Collectors.groupingBy(Student::getDepartment, Collectors.counting()));

        System.out.println("\n╔══════════════════════ GPA REPORT ═══════════════════════════╗");
        System.out.printf("  %-22s: %d%n",  "Total Students",   stats.getCount());
        System.out.printf("  %-22s: %.2f%n", "Average GPA",     stats.getAverage());
        System.out.printf("  %-22s: %.2f  (%s)%n", "Highest GPA", stats.getMax(),
                          top != null ? top.getName() : "-");
        System.out.printf("  %-22s: %.2f  (%s)%n", "Lowest GPA",  stats.getMin(),
                          bottom != null ? bottom.getName() : "-");
        System.out.println("\n  Students by Department:");
        byDept.forEach((dept, count) ->
            System.out.printf("    %-25s: %d%n", dept, count));
        System.out.println("╚═════════════════════════════════════════════════════════════╝\n");
    }

    // ─── BACKUP ───────────────────────────────────────────────────────────────

    public void backup() throws IOException {
        FileManager.createBackup(students);
    }

    // Expose the list size for convenience
    public int getCount() { return students.size(); }
}
