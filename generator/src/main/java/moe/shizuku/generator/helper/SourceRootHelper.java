package moe.shizuku.generator.helper;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by rikka on 2017/9/21.
 */

public class SourceRootHelper {

    private static final String PUBLIC_BASE = "android/src/main/java";
    private static Path TARGET;

    public static void setTargetPath(Path target) {
        TARGET = target;
    }

    public static void addCompilationUnitAndCreateStubClassIfNotExits(SourceRoot sr, CompilationUnit cu) {
        addStubClassesFromImportIfNotExists(sr, cu);
    }

    public static void addStubClassesFromImportIfNotExists(SourceRoot sr, CompilationUnit cu) {
        cu.getImports()
                .stream()
                .filter(importDeclaration -> !importDeclaration.getName().asString().startsWith("java"))
                .forEach(importDeclaration -> {
                    String pkg = importDeclaration.getName().getQualifier().get().asString();
                    String cls = importDeclaration.getName().getIdentifier();

                    createStubClassIfNotExists(sr, pkg, cls);
                });

        sr.add(cu.getPackageDeclaration().get().getNameAsString(), cu.getTypes().get(0).getNameAsString() + ".java", cu);
    }

    private static void createStubClassIfNotExists(SourceRoot sr, String pkg, String cls) {
        String path = pkg.replace('.', '\\') + '\\' + cls + ".java";
        if (!new File(TARGET.toFile(), path).exists()
                && !new File(PUBLIC_BASE, path).exists()) {

            CompilationUnit stub = new CompilationUnit(pkg);
            stub.addClass(cls);

            sr.add(pkg, cls + ".java", stub);
        }
    }
}
