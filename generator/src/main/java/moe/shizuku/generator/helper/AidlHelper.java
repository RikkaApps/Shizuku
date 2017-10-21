package moe.shizuku.generator.helper;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.EnumSet;

import moe.shizuku.generator.utils.IOUtils;

/**
 * Created by rikka on 2017/9/24.
 */

public class AidlHelper {

    /**
     * Parse all aidl files in path, valid java file will be written.
     *
     * @param root Root path
     */
    public static void parseAidlInPath(Path root) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.aidl");

        try {
            if (Files.isDirectory(root)) {
                Files
                        .list(root)
                        .forEach(AidlHelper::parseAidlInPath);
            } else if (matcher.matches(root.getFileName())) {
                writeJava(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create valid java from aidl file.
     *
     * @param path aidl file path
     * @throws IOException
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void writeJava(Path path) throws IOException {
        File file = new File(path.toFile().getParentFile(), path.toFile().getName().replace(".aidl", ".java"));
        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file);
        String code = IOUtils.toString(new FileInputStream(path.toFile()))
                .replace("inout ", "")
                .replace("in ", "")
                .replace("out ", "")
                .replace("oneway ", "");

        fos.write(code.getBytes());
    }

    public static CompilationUnit create(CompilationUnit cu) {
        cu = cu.clone();

        ClassOrInterfaceDeclaration aidlClass = (ClassOrInterfaceDeclaration) cu.getTypes().stream().findFirst().get();
        aidlClass.addModifier(Modifier.PUBLIC);

        ClassOrInterfaceDeclaration stubClass = new ClassOrInterfaceDeclaration(
                EnumSet.noneOf(Modifier.class),
                false,
                "Stub"
        );

        NodeList<Parameter> parameters = new NodeList<>();
        parameters.add(new Parameter(new ClassOrInterfaceType(null, "IBinder"), "binder"));
        MethodDeclaration asInterfaceMethod = new MethodDeclaration(
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                "asInterface",
                new ClassOrInterfaceType(null, aidlClass.getNameAsString()),
                parameters
        );

        asInterfaceMethod.setBody(new BlockStmt().addStatement("throw new RuntimeException(\"Stub!\");"));

        stubClass.addMember(asInterfaceMethod);
        aidlClass.addMember(stubClass);

        return cu;
    }
}
