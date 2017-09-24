package moe.shizuku.generator.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;

import java.util.stream.Collectors;

/**
 * Created by rikka on 2017/9/24.
 */

public class CompilationUnitUtils {

    public static String getFileName(CompilationUnit cu) {
        return cu.getTypes().stream().findFirst().get().getNameAsString() + ".java";
    }

    public static String getPackageName(CompilationUnit cu) {
        return cu.getPackageDeclaration().get().getNameAsString();
    }

    public static CompilationUnit addImport(CompilationUnit cu, String i) {
        for (ImportDeclaration im : cu.getImports()) {
            if (im.getNameAsString().equals(i)) {
                return cu;
            }
        }
        return cu.addImport(i);
    }

    public static CompilationUnit addImports(CompilationUnit cu, NodeList<ImportDeclaration> importDeclarations) {
        for (ImportDeclaration im : importDeclarations) {
            addImport(cu, im.getNameAsString());
        }
        return cu;
    }
}
