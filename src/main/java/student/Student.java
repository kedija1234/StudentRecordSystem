package student;

import java.io.Serializable;

public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    private String studentId;
    private String name;
    private String department;
    private double gpa;

    public Student(String studentId, String name, String department, double gpa) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.gpa = gpa;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public double getGpa() {
        return gpa;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public String toTextLine() {
        return studentId + "|" + name + "|" + department + "|" + gpa;
    }

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

    @Override
    public String toString() {
        return String.format(
                "ID: %-8s | Name: %-20s | Dept: %-20s | GPA: %.2f",
                studentId, name, department, gpa
        );
    }
}
