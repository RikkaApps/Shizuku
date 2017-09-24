package moe.shizuku.generator;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import moe.shizuku.generator.helper.ActionClassHelper;
import moe.shizuku.generator.helper.AidlHelper;
import moe.shizuku.generator.helper.ApiClassHelper;
import moe.shizuku.generator.helper.DelegateClassHelper;
import moe.shizuku.generator.helper.RequestHandlerClassHelper;
import moe.shizuku.generator.utils.SourceRootUtils;

/**
 * Created by Rikka on 2017/5/10.
 */

public class Generator {

    public static void main(String[] args) throws IOException {
        Generator generator = new Generator(26);
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
    }

    private void generate() throws IOException {
        ApiClassHelper.setApiVersion(apiVersion);
        AidlHelper.parseAidlInPath(sourcePath);
        RequestHandlerClassHelper.clear();

        SourceRoot sr = new SourceRoot(sourcePath);
        sr.tryToParse().stream()
                .filter(ParseResult::isSuccessful)
                .map(ParseResult::getResult)
                .map(Optional::get)
                .forEach(compilationUnit -> {
                    generateServer(sr, compilationUnit);
                    generateApi(sr, compilationUnit);
                });

        SourceRootUtils.save(RequestHandlerClassHelper.get(), sr, serverPath);
        SourceRootUtils.save(ActionClassHelper.get(), sr, serverPath);
    }

    private void generateServer(SourceRoot sr, CompilationUnit cu) {
        // add imports
        cu.addImport("android.os.IBinder");
        cu.addImport("java.util.List");

        SourceRootUtils.save(DelegateClassHelper.create(cu), sr, serverPath);
        SourceRootUtils.save(AidlHelper.create(cu), sr, serverPath);
        RequestHandlerClassHelper.createOrAdd(cu);
        ActionClassHelper.createOrAdd(cu);
    }

    private void generateApi(SourceRoot sr, CompilationUnit cu) {

    }
}
