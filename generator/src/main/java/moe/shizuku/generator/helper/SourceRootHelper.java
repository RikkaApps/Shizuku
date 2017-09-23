package moe.shizuku.generator.helper;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Created by rikka on 2017/9/21.
 */

public class SourceRootHelper {

    private static final String PUBLIC_BASE = "android/src/main/java";
    private static Path TARGET;

    public static void setTargetPath(Path target) {
        TARGET = target;
    }

    public static void add(SourceRoot sr, CompilationUnit cu) {
        cu.getImports().sort(Comparator.comparing(NodeWithName::getNameAsString));
        sr.add(cu.getPackageDeclaration().get().getNameAsString(), cu.getTypes().get(0).getNameAsString() + ".java", cu);
    }
}
