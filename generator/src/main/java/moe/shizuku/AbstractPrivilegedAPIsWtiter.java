package moe.shizuku;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by Rikka on 2017/5/12.
 */

public class AbstractPrivilegedAPIsWtiter extends Writer {

    @Override
    public void write(String module, String packageName, AidlParser parser) throws IOException {
        super.write(module, packageName, parser);

        File file = new File(filename);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }

        writer = new BufferedWriter(new FileWriter(file));

        start();
        for (String i : parser.imports) {
            writeImport(i);
        }
        writeImport("android.support.annotation.CallSuper")

                .writeImport("java.util.ArrayList")
                .writeImport("java.util.List")

                .writeImport("java.io.IOException")
                .writeImport("java.net.Socket")
                .writeImport("java.util.UUID")

                .writeImport("moe.shizuku.server.io.ParcelInputStream")
                .writeImport("moe.shizuku.server.io.ParcelOutputStream")

                .writeImport("moe.shizuku.server.Actions")

                .writeLine("abstract class AbstractPrivilegedAPIs {")
                .writeLine("private static final int TIMEOUT = 5000;")
                .writeLine("protected UUID token;");

        for (AidlMethod method : parser.methods) {
            writeMethod(method);
        }

        end();
    }

    @Override
    public Writer writeMethod(AidlMethod method) throws IOException {
        /*if (method.useParceledListSlice()) {
            writeLine("@SuppressWarnings(\"unchecked\")");
            writeLine(String.format(Locale.ENGLISH,
                    "public static %s %s throws RemoteException {",
                    "ParceledListSlice",
                    method.toString().substring(method.toString().indexOf(' ') + 1)));
        } else */{
            writeLine("@CallSuper");
            writeLine(String.format(Locale.ENGLISH, "public %s {", method.toString()));
        }

        writeLine("try {\n" +
                "Socket client = new Socket(Protocol.HOST, Protocol.PORT);\n" +
                "client.setSoTimeout(TIMEOUT);\n" +
                "ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());\n" +
                "ParcelInputStream is = new ParcelInputStream(client.getInputStream());");
        writeLine("os.writeInt(Actions." + method.name + ");");

        if (!"version".equals(method.name)
                && !"authorize".equals(method.name)) {
            writeLine("os.writeLong(token.getMostSignificantBits());\n" +
                    "os.writeLong(token.getLeastSignificantBits());");
        }

        for (Arg arg : method.args) {
            if (!arg.type.isArray()) {
                if (!arg.type.isParcelable()) {
                    writeLine(String.format(Locale.ENGLISH, "os.write%s(%s);", arg.type.getDataSteamType(), arg.name));
                } else {
                    writeLine(String.format(Locale.ENGLISH, "os.writeParcelable(%s);", arg.name));
                }
            } else {
                writeLine(String.format(Locale.ENGLISH, "if (%s == null) {", arg.name));
                writeLine("os.writeInt(-1);");
                writeLine("} else {");
                writeLine(String.format(Locale.ENGLISH, "os.writeInt(%s.length);", arg.name));
                writeLine(String.format(Locale.ENGLISH, "for (int arg : %s) {", arg.name));
                writeLine(String.format(Locale.ENGLISH, "os.write%s(%s);", arg.type.getDataSteamType(), "arg"));
                writeLine("}");
                writeLine("}");
            }
        }

        //writeLine("os.flush();");
        //writeLine("os.close();");
        writeLine("is.readException();");

        if (method.returnType.isList()) {
            writeLine(String.format(Locale.ENGLISH,
                    "%s _result = is.readParcelableList(%s.CREATOR);",
                    method.returnType.name,
                    method.returnType.getTypeInList()));
        } else if (method.returnType.name.equals("Bitmap")) {
            writeLine("Bitmap _result = is.readBitmap();");
        } else if (method.returnType.isParcelable()) {
            writeLine(String.format(Locale.ENGLISH,
                    "%s _result = is.readParcelable(%s.CREATOR);",
                    method.returnType.name,
                    method.returnType));
        } else if (method.returnType.isArray()) {
            // TODO
        } else if (!method.returnType.isVoid()) {
            writeLine(String.format(Locale.ENGLISH,
                    "%s _result = is.read%s();",
                    method.returnType.name,
                    method.returnType.getDataSteamType()));
        }
        //writeLine("is.close();");
        //writeLine("client.close();");
        if (!method.returnType.isVoid()) {
            writeLine("return _result;");
        }
        writeLine("} catch (IOException ignored) {\n" +
                "}");
        if (!method.returnType.isVoid()) {
            writeLine("return " + method.returnType.getDefaultValue() + ";");
        }
        writeLine("}");

        return this;
    }
}
