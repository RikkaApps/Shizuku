package moe.shizuku.generator.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Path;
import java.util.Comparator;

import static com.github.javaparser.utils.Utils.assertNotNull;

/**
 * Created by rikka on 2017/9/21.
 */

public class SourceRootUtils {

    public static void save(CompilationUnit cu, SourceRoot sr, Path root) {
        cu.getImports().sort(Comparator.comparing(NodeWithName::getNameAsString));

        assertNotNull(cu);
        assertNotNull(root);
        Path path = CodeGenerationUtils.fileInPackageRelativePath(CompilationUnitUtils.getPackageName(cu), CompilationUnitUtils.getFileName(cu));
        path = root.resolve(path);
        cu.setStorage(path);
        //noinspection ConstantConditions
        cu.getStorage().get().save(sr.getPrinter());
        Log.trace("Saving %s", path);
    }
}
