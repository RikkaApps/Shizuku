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

    public static void main(String[] args) throws IOException {
        FileInputStream in = new FileInputStream("../IPackageManager.aidl");
        String code = toString(in)
                .replace("interface ", "public class ")
                .replace("inout ", " ")
                .replace("in ", " ")
                .replace("out ", " ")
                .replace("oneway ", " ");

        // parse the file
        CompilationUnit cu = JavaParser.parse(code);

        // prints the resulting compilation unit to default system output
        System.out.println(cu.toString());
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
