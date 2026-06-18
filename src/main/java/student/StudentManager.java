package student;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StudentManager {

    private List<Student> students = new ArrayList<>();

    public void loadStudents() throws IOException {
        students = FileManager.loadFromTextFile();
        System.out.println("Loaded " + students.size() + " student(s) from file.");
    }

    private void syncAllFiles() throws IOException {
        FileManager.saveToTextFile(students);
        FileManager.saveToBinaryFile(students);
        FileManager.saveWithSerialization(students);
    }

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

    public Student findById(String id) {
        return students.stream()
            .filter(s -> s.getStudentId().equalsIgnoreCase(id.trim()))
            .findFirst()
            .orElse(null);
    }

    public void updateStudent(String id, String newName, String newDept, Double newGpa)
            throws IOException {

        Student s = findById(id);
        if (s == null) {
            throw new IllegalArgumentException("No student found with ID: " + id);
        }

        if (newName != null && !newName.trim().isEmpty()) {
            s.setName(newName.trim());
        }

        if (newDept != null && !newDept.trim().isEmpty()) {
            s.setDepartment(newDept.trim());
        }

        if (newGpa != null) {
            if (newGpa < 0.0 || newGpa > 4.0) {
                throw new IllegalArgumentException("GPA must be between 0.0 and 4.0.");
            }
            s.setGpa(newGpa);
        }

        syncAllFiles();
        System.out.println("✔ Student updated: " + id);
    }

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

    public void generateReport() {
        if (students.isEmpty()) {
            System.out.println("No data to generate a report.");
            return;
        }

        DoubleSummaryStatistics stats = students.stream()
            .mapToDouble(Student::getGpa)
            .summaryStatistics();

        Student top = students.stream()
            .max(Comparator.comparingDouble(Student::getGpa))
            .orElse(null);

        Student bottom = students.stream()
            .min(Comparator.comparingDouble(Student::getGpa))
            .orElse(null);

        Map<String, Long> byDept = students.stream()
            .collect(Collectors.groupingBy(
                Student::getDepartment,
                Collectors.counting()
            ));

        System.out.println("\n╔══════════════════════ GPA REPORT ═══════════════════════════╗");
        System.out.printf("  %-22s: %d%n", "Total Students", stats.getCount());
        System.out.printf("  %-22s: %.2f%n", "Average GPA", stats.getAverage());
        System.out.printf("  %-22s: %.2f  (%s)%n",
                "Highest GPA",
                stats.getMax(),
                top != null ? top.getName() : "-");
        System.out.printf("  %-22s: %.2f  (%s)%n",
                "Lowest GPA",
                stats.getMin(),
                bottom != null ? bottom.getName() : "-");

        System.out.println("\n  Students by Department:");
        byDept.forEach((dept, count) ->
            System.out.printf("    %-25s: %d%n", dept, count));

        System.out.println("╚═════════════════════════════════════════════════════════════╝\n");
    }

    public void backup() throws IOException {
        FileManager.createBackup(students);
    }

    public int getCount() {
        return students.size();
    }
}
