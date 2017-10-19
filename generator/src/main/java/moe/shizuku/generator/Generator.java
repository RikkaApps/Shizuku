package moe.shizuku.generator;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import moe.shizuku.generator.creator.ActionClassCreator;
import moe.shizuku.generator.helper.AidlHelper;
import moe.shizuku.generator.creator.ApiClassCreator;
import moe.shizuku.generator.creator.DelegateClassCreator;
import moe.shizuku.generator.creator.RequestHandlerClassCreator;
import moe.shizuku.generator.helper.IOBlockHelper;
import moe.shizuku.generator.utils.SourceRootUtils;

/**
 * java -jar generator/build/libs/generator.jar
 *
 * Created by Rikka on 2017/5/10.
 */

public class Generator {

    public static void main(String[] args) throws IOException {
        int apiVersion = 26;
        if (args.length == 1) {
            apiVersion = Integer.parseInt(args[0]);
        }
        Generator generator = new Generator(apiVersion);
        generator.generate();
    }

    private int  apiVersion;
    private Path sourcePath;
    private Path serverPath;
    private Path apiPath;

    public Generator(int apiVersion) {
        this.apiVersion = apiVersion;
        this.sourcePath = Paths.get("source-" + apiVersion);
        this.serverPath = Paths.get("server-" + apiVersion + "/src/main/java");
        this.apiPath = Paths.get("api-" + apiVersion + "/src/main/java");

        if (!sourcePath.toFile().exists()) {
            throw new RuntimeException("source-" + apiVersion + " not exists.");
        }

        File file = new File(sourcePath.toFile(), "parcelable.txt");
        if (file.exists()) {
            IOBlockHelper.readNoCreatorParcelables(file);
        }
    }

    private void generate() throws IOException {
        ApiClassCreator.setApiVersion(apiVersion);
        AidlHelper.parseAidlInPath(sourcePath);
        RequestHandlerClassCreator.clear();

        SourceRoot sr = new SourceRoot(sourcePath);
        sr.tryToParse().stream()
                .filter(ParseResult::isSuccessful)
                .map(ParseResult::getResult)
                .map(Optional::get)
                .forEach(compilationUnit -> {
                    generateServer(sr, compilationUnit);
                    generateApi(sr, compilationUnit);
                });

        SourceRootUtils.save(RequestHandlerClassCreator.get(), sr, serverPath);
        SourceRootUtils.save(ActionClassCreator.get(), sr, serverPath);

        ActionClassCreator.toApi(apiVersion);
        SourceRootUtils.save(ActionClassCreator.get(), sr, apiPath);
    }

    private void generateServer(SourceRoot sr, CompilationUnit cu) {
        // add imports
        cu.addImport("android.os.IBinder");
        cu.addImport("java.util.List");

        SourceRootUtils.save(DelegateClassCreator.create(cu), sr, serverPath);
        SourceRootUtils.save(AidlHelper.create(cu), sr, serverPath);
        RequestHandlerClassCreator.createOrAdd(cu);
        ActionClassCreator.createOrAdd(cu);
    }

    private void generateApi(SourceRoot sr, CompilationUnit cu) {
        SourceRootUtils.save(ApiClassCreator.create(cu), sr, apiPath);
    }
}
