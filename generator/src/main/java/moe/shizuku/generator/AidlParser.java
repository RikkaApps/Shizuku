package moe.shizuku.generator;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;

import moe.shizuku.generator.helper.DelegateClassHelper;
import moe.shizuku.generator.helper.SourceRootHelper;
import moe.shizuku.generator.utils.IOUtils;

/**
 * Created by Rikka on 2017/5/10.
 */

@SuppressWarnings("ConstantConditions")
public class AidlParser {

    public static void main(String[] args) throws IOException {
        Path source = Paths.get("source-26");
        Path target = Paths.get("server-26/src/main/java");

        SourceRootHelper.setTargetPath(target);

        parseAidl(source);

        SourceRoot sr = new SourceRoot(source);
        sr.tryToParse().stream()
                .filter(ParseResult::isSuccessful)
                .map(ParseResult::getResult)
                .map(Optional::get)
                .forEach(compilationUnit -> parse(sr, compilationUnit));

        sr.saveAll(target);
    }

    private static void parseAidl(Path source) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.aidl");

        try {
            if (Files.isDirectory(source)) {
                Files
                        .list(source)
                        .forEach(AidlParser::parseAidl);
            } else if (matcher.matches(source.getFileName())) {
                writeJava(source);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void writeJava(Path source) throws IOException {
        File file = new File(source.toFile().getParentFile(), source.toFile().getName().replace(".aidl", ".java"));
        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(file);
        String code = IOUtils.toString(new FileInputStream(source.toFile()))
                .replace("inout ", "")
                .replace("in ", "")
                .replace("out ", "")
                .replace("oneway ", "");

        fos.write(code.getBytes());
    }

    private static void parse(SourceRoot sr, CompilationUnit cu) {
        // add imports
        cu.addImport("android.os.IBinder");
        cu.addImport("java.util.List");

        SourceRootHelper.add(sr, DelegateClassHelper.create(cu));

        toCompiledAidl(cu);
    }

    private static void toCompiledAidl(CompilationUnit cu) {
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
    }
}
