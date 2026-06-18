package student;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileManager {

    private static final String DATA_DIR = "data/";
    private static final String BACKUP_DIR = "data/backup/";
    private static final String TEXT_FILE = DATA_DIR + "students.txt";
    private static final String BINARY_FILE = DATA_DIR + "students.dat";
    private static final String OBJECT_FILE = DATA_DIR + "students.ser";
    private static final String BACKUP_FILE = BACKUP_DIR + "students_backup.txt";

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

    public static List<Student> loadFromTextFile() throws IOException {
        List<Student> students = new ArrayList<>();
        File file = new File(TEXT_FILE);

        if (!file.exists() || file.length() == 0) {
            return students;
        }

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

    public static void saveToTextFile(List<Student> students) throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new BufferedWriter(new FileWriter(TEXT_FILE)))) {

            for (Student s : students) {
                writer.println(s.toTextLine());
            }
        }
    }

    public static void saveToBinaryFile(List<Student> students) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(BINARY_FILE)))) {

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

        if (!file.exists() || file.length() == 0) {
            return students;
        }

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {

            int count = dis.readInt();

            for (int i = 0; i < count; i++) {
                String id = dis.readUTF();
                String name = dis.readUTF();
                String dept = dis.readUTF();
                double gpa = dis.readDouble();

                students.add(new Student(id, name, dept, gpa));
            }
        }

        return students;
    }

    public static void saveWithSerialization(List<Student> students) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(OBJECT_FILE)))) {

            oos.writeObject(students);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Student> loadWithSerialization()
            throws IOException, ClassNotFoundException {

        File file = new File(OBJECT_FILE);

        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {

            return (List<Student>) ois.readObject();
        }
    }

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

    public static String getTextFilePath() {
        return TEXT_FILE;
    }

    public static String getBinaryFilePath() {
        return BINARY_FILE;
    }

    public static String getObjectFilePath() {
        return OBJECT_FILE;
    }
}
