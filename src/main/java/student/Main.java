package student;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main entry point.
 *
 * Responsibilities:
 *   1. Initialize files/directories on first run
 *   2. Load existing data
 *   3. Present an interactive menu
 *   4. Route user input to the correct manager method
 *   5. Handle exceptions gracefully (never let the app crash on bad input)
 *
 * We separate UI (this class) from business logic (StudentManager)
 * and storage (FileManager) — this is the core idea behind layered design.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final StudentManager manager = new StudentManager();

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   STUDENT RECORD MANAGEMENT SYSTEM           ║");
        System.out.println("║   Java File I/O — OOP Assignment             ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        // Step 1: create directories and files if they don't exist yet
        try {
            FileManager.initializeFiles();
        } catch (IOException e) {
            System.err.println("ERROR: Could not initialize files — " + e.getMessage());
            System.exit(1);   // can't run without file access
        }

        // Step 2: load saved records into memory
        try {
            manager.loadStudents();
        } catch (IOException e) {
            System.err.println("WARNING: Could not load existing records — " + e.getMessage());
            // non-fatal: we can still add new students this session
        }

        // Step 3: main loop — runs until user chooses to exit
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();

            // Each case is wrapped in its own try-catch so one bad action
            // doesn't kick the user out of the loop
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
                    default  -> System.out.println("Invalid option. Please enter 0–9.");
                }
            } catch (IllegalArgumentException e) {
                // Business-rule violations (duplicate ID, invalid GPA, not found…)
                System.out.println("⚠ " + e.getMessage());
            } catch (IOException e) {
                // File system problems (disk full, permission denied…)
                System.out.println("✖ File error: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                // Only happens during deserialization if Student.class is missing
                System.out.println("✖ Serialization error: " + e.getMessage());
            }
        }
        sc.close();
    }

    // ─── MENU ─────────────────────────────────────────────────────────────────

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

    // ─── INPUT HELPERS ────────────────────────────────────────────────────────

    private static String prompt(String label) {
        System.out.print("  " + label + ": ");
        return sc.nextLine().trim();
    }

    // Keeps asking until the user enters a valid double in [0.0, 4.0]
    private static double promptGpa() {
        while (true) {
            try {
                double gpa = Double.parseDouble(prompt("GPA (0.0 – 4.0)"));
                if (gpa < 0.0 || gpa > 4.0) throw new NumberFormatException();
                return gpa;
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a number between 0.0 and 4.0.");
            }
        }
    }

    // ─── MENU ACTIONS ─────────────────────────────────────────────────────────

    private static void addStudent() throws IOException {
        System.out.println("\n── Add New Student ──");
        String id   = prompt("Student ID");
        String name = prompt("Full Name");
        String dept = prompt("Department");
        double gpa  = promptGpa();
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

        // Confirm the record exists before asking for new values
        Student existing = manager.findById(id);
        if (existing == null) {
            System.out.println("  No student found with ID: " + id);
            return;
        }
        System.out.println("  Current record: " + existing);

        String newName = prompt("New Name");
        String newDept = prompt("New Department");
        String gpaStr  = prompt("New GPA");

        // null means "don't change" — we pass null if the field was left blank
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

    /**
     * Demonstrates reading from binary and object files to show
     * they were correctly written and can be read back independently.
     */
    private static void loadFromAllFormats() throws IOException, ClassNotFoundException {
        System.out.println("\n── Reading from Binary File ──");
        FileManager.loadFromBinaryFile().forEach(s -> System.out.println("  " + s));

        System.out.println("\n── Reading from Object (Serialized) File ──");
        FileManager.loadWithSerialization().forEach(s -> System.out.println("  " + s));
    }
}
