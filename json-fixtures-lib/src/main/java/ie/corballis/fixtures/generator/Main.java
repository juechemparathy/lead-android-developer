package ie.corballis.fixtures.generator;

import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static Class clazz;
    private static String folder;
    private static String fileNamePrefix;
    private static String fixtureName;
    private static boolean append;

    public static void main(String[] args) throws Exception {
        in();
        out();
    }

    private static void in() throws ClassNotFoundException {
        System.out.println("Class to serialize (fully qualified name):");
        String className = scanner.nextLine();
        clazz = Class.forName(className);
        // e.g. "ie.corballis.fixtures.generator.SampleClassCollections"

        System.out.println("Absolute path of folder for generating the new fixture file:");
        folder = scanner.nextLine();

        System.out.println("Prefix of file name to generate the new fixture file with):");
        fileNamePrefix = scanner.nextLine();
        // e.g. "cities" -> the file name will be "cities.fixtures.json"

        System.out.println("Fixture name to generate the fixture with:");
        fixtureName = scanner.nextLine();
        // e.g. "fixture1"

        System.out.println("Should the new fixture be appended if the file already exists?");
        append = scanner.nextBoolean();
        // if false, nothing will be executed if the file already exists,
        // if true, the new fixture will be appended to the end of the file

        scanner.close();
    }

    private static void out() throws Exception {
        Map<String, Object> objectAsMap = new DefaultFixtureGenerator().generateMapFromBeanDirectly(clazz);
        new DefaultFileSystemWriter().writeOut(folder, fileNamePrefix, fixtureName, objectAsMap, append);
    }
}