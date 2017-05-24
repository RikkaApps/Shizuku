package moe.shizuku;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by Rikka on 2017/5/11.
 */

public abstract class Writer {

    protected BufferedWriter writer;
    protected AidlParser parser;
    protected String filename;
    protected String packageName;
    protected String module;

    public void write(String module, String packageName, AidlParser parser) throws IOException {
        this.module = module;
        this.packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        this.parser = parser;
        this.filename = module + "/src/main/java/" + packageName.replace('.', '/') + ".java";
    }

    public Writer write(String line) throws IOException {
        writer.write(line);
        return this;
    }

    public Writer writeLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
        return this;
    }

    public Writer start() throws IOException {
        return writeLine("package " + packageName + ";");
    }

    public Writer writeClass(String className, String parent) throws IOException {
        return writeLine(String.format(Locale.ENGLISH, "public class %s extends %s {", className, parent));
    }

    public Writer writeClass(String className) throws IOException {
        return writeLine(String.format(Locale.ENGLISH, "public class %s {", className));
    }

    public Writer writeImport(String i) throws IOException {
        return writeLine("import " + i + ";");
    }

    public Writer writeMethod(AidlMethod method) throws IOException {
        return this;
    }

    public void end() throws IOException {
        writeLine("}");
        writer.flush();
    }
}
