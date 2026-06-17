package student;

import java.io.Serializable;

/**
 * Student model class.
 * Implements Serializable so Java can convert the object
 * into a byte stream for binary/object file storage.
 */
public class Student implements Serializable {

    // Required for Serializable — acts as a version ID.
    // If you change the class structure later, update this number
    // so Java knows the saved file is outdated.
    private static final long serialVersionUID = 1L;

    private String studentId;
    private String name;
    private String department;
    private double gpa;

    // Constructor — called when creating a new Student object
    public Student(String studentId, String name, String department, double gpa) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.gpa = gpa;
    }

    // Getters — used to read values safely from outside the class
    public String getStudentId()  { return studentId; }
    public String getName()       { return name; }
    public String getDepartment() { return department; }
    public double getGpa()        { return gpa; }

    // Setters — used when updating a student's record
    public void setName(String name)             { this.name = name; }
    public void setDepartment(String department) { this.department = department; }
    public void setGpa(double gpa)               { this.gpa = gpa; }

    /**
     * Returns a pipe-delimited string.
     * We use | as a separator (not comma) because names/departments
     * could contain commas, which would break parsing.
     * Example: "S001|Alice Tesfaye|Computer Science|3.85"
     */
    public String toTextLine() {
        return studentId + "|" + name + "|" + department + "|" + gpa;
    }

    /**
     * Rebuilds a Student from a saved text line.
     * split("\\|", -1) — the -1 ensures empty trailing fields are kept.
     */
    public static Student fromTextLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Malformed record: " + line);
        }
        return new Student(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            Double.parseDouble(parts[3].trim())
        );
    }

    // Readable summary — printed when we display a student to the user
    @Override
    public String toString() {
        return String.format(
            "ID: %-8s | Name: %-20s | Dept: %-20s | GPA: %.2f",
            studentId, name, department, gpa
        );
    }
}
