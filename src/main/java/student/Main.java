package student;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final StudentManager manager = new StudentManager();

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   STUDENT RECORD MANAGEMENT SYSTEM           ║");
        System.out.println("║   Java File I/O — OOP Assignment             ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        try {
            FileManager.initializeFiles();
        } catch (IOException e) {
            System.err.println("ERROR: Could not initialize files — " + e.getMessage());
            System.exit(1);
        }

        try {
            manager.loadStudents();
        } catch (IOException e) {
            System.err.println("WARNING: Could not load existing records — " + e.getMessage());
        }

        boolean running = true;

        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addStudent();
                    case "2" -> searchStudent();
                    case "3" -> updateStudent();
                    case "4" -> deleteStudent();
                    case "5" -> manager.displayAll();
                    case "6" -> manager.generateReport();
                    case "7" -> manager.backup();
                    case "8" -> loadFromAllFormats();
                    case "9" -> FileManager.displayFileProperties();
                    case "0" -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid option. Please enter 0–9.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("⚠ " + e.getMessage());
            } catch (IOException e) {
                System.out.println("✖ File error: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println("✖ Serialization error: " + e.getMessage());
            }
        }

        sc.close();
    }

    private static void printMenu() {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│           MAIN MENU                  │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  1. Add Student                      │");
        System.out.println("│  2. Search Student by ID             │");
        System.out.println("│  3. Update Student                   │");
        System.out.println("│  4. Delete Student                   │");
        System.out.println("│  5. Display All Students             │");
        System.out.println("│  6. Generate GPA Report              │");
        System.out.println("│  7. Create Backup                    │");
        System.out.println("│  8. Demo: Load from Binary/Object    │");
        System.out.println("│  9. Show File Properties             │");
        System.out.println("│  0. Exit                             │");
        System.out.println("└─────────────────────────────────────┘");
        System.out.print("  Choice: ");
    }

    private static String prompt(String label) {
        System.out.print("  " + label + ": ");
        return sc.nextLine().trim();
    }

    private static double promptGpa() {
        while (true) {
            try {
                double gpa = Double.parseDouble(prompt("GPA (0.0 – 4.0)"));

                if (gpa < 0.0 || gpa > 4.0) {
                    throw new NumberFormatException();
                }

                return gpa;
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a number between 0.0 and 4.0.");
            }
        }
    }

    private static void addStudent() throws IOException {
        System.out.println("\n── Add New Student ──");

        String id = prompt("Student ID");
        String name = prompt("Full Name");
        String dept = prompt("Department");
        double gpa = promptGpa();

        manager.addStudent(id, name, dept, gpa);
    }

    private static void searchStudent() {
        String id = prompt("\nEnter Student ID to search");

        Student found = manager.findById(id);

        if (found != null) {
            System.out.println("\n  Found: " + found);
        } else {
            System.out.println("  No student found with ID: " + id);
        }
    }

    private static void updateStudent() throws IOException {
        System.out.println("\n── Update Student (press Enter to skip a field) ──");

        String id = prompt("Student ID to update");
        Student existing = manager.findById(id);

        if (existing == null) {
            System.out.println("  No student found with ID: " + id);
            return;
        }

        System.out.println("  Current record: " + existing);

        String newName = prompt("New Name");
        String newDept = prompt("New Department");
        String gpaStr = prompt("New GPA");

        Double newGpa = gpaStr.isEmpty() ? null : Double.parseDouble(gpaStr);

        manager.updateStudent(
                id,
                newName.isEmpty() ? null : newName,
                newDept.isEmpty() ? null : newDept,
                newGpa
        );
    }

    private static void deleteStudent() throws IOException {
        String id = prompt("\nEnter Student ID to delete");

        System.out.print("  Confirm delete '" + id + "'? (yes/no): ");

        if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
            manager.deleteStudent(id);
        } else {
            System.out.println("  Deletion cancelled.");
        }
    }

    private static void loadFromAllFormats()
            throws IOException, ClassNotFoundException {

        System.out.println("\n── Reading from Binary File ──");
        FileManager.loadFromBinaryFile()
                .forEach(s -> System.out.println("  " + s));

        System.out.println("\n── Reading from Object (Serialized) File ──");
        FileManager.loadWithSerialization()
                .forEach(s -> System.out.println("  " + s));
    }
}
