package moe.shizuku;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rikka on 2017/5/10.
 */

public class AidlParser {

    public String packageName;
    public String interfaceName;

    public List<String> imports = new ArrayList<>();
    public List<AidlMethod> methods = new ArrayList<>();
    public List<String> types = new ArrayList<>();

    private void parse(File file) throws IOException {
        imports.clear();
        methods.clear();
        types.clear();

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        String currentType = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0
                    || line.equals("}")) {
                continue;
            }

            line = line.replace(";", "").replace(" }", "");

            if (line.startsWith("//")) {
                currentType = line.substring(2).trim();
                continue;
            }

            if (line.startsWith("package ")) {
                packageName = line.substring("package ".length());
            } else if (line.startsWith("import ")) {
                imports.add(line.substring("import ".length()));
            } else if (line.startsWith("interface ")) {
                interfaceName = line.substring("interface ".length());
            } else {
                String returnType = line.substring(0, line.indexOf(' '));
                String name = line.substring(line.indexOf(' ') + 1, line.indexOf('('));
                String args = line.substring(line.indexOf('(') + 1, line.indexOf(')')).replace(")", "");
                String comment = line.contains("//") ? line.substring(line.indexOf("//") + 3).trim() : null;

                types.add(currentType);
                methods.add(new AidlMethod(returnType, name, args, comment, methods));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        FileInputStream in = new FileInputStream("../IPackageManager.aidl");
        String code = toString(in)
                .replace("interface ", "public class ")
                .replace("(inout ", "(")
                .replace("(in ", "(")
                .replace("(out ", "(");

        // parse the file
        CompilationUnit cu = JavaParser.parse(code);

        // prints the resulting compilation unit to default system output
        System.out.println(cu.toString());

        /*AidlParser parser = new AidlParser();

        parser.parse(new File("../apis.aidl"));*/
    }

    public static String toString(FileInputStream fis) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }
}
