import java.util.Vector;
//1. Дано прямокутну матрицю розмірності . Утворити вектор, кожен елемент якого дорівнює максимуму із суми цифр елементів відповідного рядка матриці.
//2. Дано послідовність слів, розділених комами. Видрукувати слова, попередньо перетворивши кожне із них за правилом: замінити кожну літеру 'g' на послідовність літер 'th'.
//3. Створити абстрактний базовий клас навчальний заклад (назва, адрес, рік заснування) та похідні класи - СШ (номер, к-сть учнів) і ВУЗ (рівень акредитації, к-сть факультетів).
//Дано масив посилань на об’єкти навчальних закладів.
//    • Посортувати його за роком заснування.
//    • Знайти школу з мінімальною к-стю учнів.
//   • Вивести ВУЗи вказаного рівня акредитації.
public class Main {
    public static void main(String[] args) {
        System.out.println("Task 1");
        int[][] matrix = GenerateMatrix(3, 3);
        PrintMatrix(matrix);
        Vector<Integer> result = maxSumDigits(matrix);
        for (Integer integer : result) {
            System.out.println(integer);
        }

        System.out.println("\nTask 2");
        String word = "ginger, frog, sponge, dragon, penguin, giraffe, spaghetti, guitar, magic, glove";
        System.out.println(replaceG(word));

        System.out.println("\nTask 3");
        SchoolChild schoolChild1 = new SchoolChild("School1", "Address1", 2000, 1, 100);
        SchoolChild schoolChild2 = new SchoolChild("School2", "Address2", 2005, 2, 200);
        SchoolChild schoolChild3 = new SchoolChild("School3", "Address3", 2006, 3, 300);
        University university1 = new University("University1", "Address4", 2006, 1, 10);
        University university2 = new University("University2", "Address5", 2004, 1, 20);
        University university3 = new University("University3", "Address6", 2001, 3, 30);
        School[] schools = {schoolChild1, schoolChild2, schoolChild3, university1, university2, university3};

        System.out.println("Sort by year");
        for (School school : schools) {
            System.out.println(school);
        }
        SortByYear(schools);
        System.out.println("\nAfter sort");
        for (School school : schools) {
            System.out.println(school);
        }

        System.out.println("\nSchool with min students");
        SchoolWithMinStudents(schools);

        System.out.println("\nEnter accreditation level");
        int accreditationLevel = System.console().readLine().charAt(0) - '0';
        for (School school : schools) {
            if (school instanceof University) {
                University university = (University) school;
                if (university.getAccreditationLevel() == accreditationLevel) {
                    System.out.println(university);
                }
            }
        }

    }

    public static Vector<Integer> maxSumDigits(int[][] matrix) {
        Vector<Integer> result = new Vector<>();
        for (int i = 0; i < matrix.length; i++) {
            int maxSum = 0;
            for (int j = 0; j < matrix[i].length; j++) {
                int sum = 0;
                int number = matrix[i][j];
                while (number != 0) {
                    sum += number % 10;
                    number /= 10;
                }
                if (sum > maxSum) {
                    maxSum = sum;
                }
            }
            result.add(maxSum);
        }
        return result;
    }
    public static int[][] GenerateMatrix(int n, int m) {
        int[][] matrix = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = (int) (Math.random() * 100);
            }
        }
        return matrix;
    }
    public static void PrintMatrix(int[][] matrix) {
        for (int[] ints : matrix) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println();
        }
    }

    public static String replaceG(String word) {
        return word.replace("g", "th");
    }

    public static void SortByYear(School[] schools) {
        for (int i = 0; i < schools.length; i++) {
            for (int j = 0; j < schools.length - 1; j++) {
                if (schools[j].getYear() > schools[j + 1].getYear()) {
                    School temp = schools[j];
                    schools[j] = schools[j + 1];
                    schools[j + 1] = temp;
                }
            }
        }
    }

    public static void SchoolWithMinStudents(School[] schools) {
        SchoolChild schoolChild = (SchoolChild) schools[0];
        for (int i = 1; i < schools.length; i++) {
            if (schools[i] instanceof SchoolChild) {
                SchoolChild temp = (SchoolChild) schools[i];
                if (temp.GetStudents() < schoolChild.GetStudents()) {
                    schoolChild = temp;
                }
            }
        }
        System.out.println(schoolChild);
    }
}

abstract class School {
    private String name;
    private String address;
    private int year;

    public int getYear() {
        return year;
    }

    public School(String name, String address, int year) {
        this.name = name;
        this.address = address;
        this.year = year;
    }

    @Override
    public String toString() {
        return "School{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", year=" + year +
                '}';
    }
}

class SchoolChild extends School {
    private int number;
    private int students;

    public int GetStudents() {
        return students;
    }
    public SchoolChild(String name, String address, int year, int number, int students) {
        super(name, address, year);
        this.number = number;
        this.students = students;
    }

    @Override
    public String toString() {
        return "SchoolChild{" +
                "number=" + number +
                ", students=" + students +
                "} " + super.toString();
    }
}

class University extends School {
    private int accreditationLevel;
    private int faculties;

    public int getAccreditationLevel() {
        return accreditationLevel;
    }
    public University(String name, String address, int year, int accreditationLevel, int faculties) {
        super(name, address, year);
        this.accreditationLevel = accreditationLevel;
        this.faculties = faculties;
    }

    @Override
    public String toString() {
        return "University{" +
                "accreditationLevel=" + accreditationLevel +
                ", faculties=" + faculties +
                "} " + super.toString();
    }
}