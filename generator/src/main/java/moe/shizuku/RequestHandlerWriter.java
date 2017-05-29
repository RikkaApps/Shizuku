package moe.shizuku;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by Rikka on 2017/5/11.
 */

public class RequestHandlerWriter extends Writer {

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

        writeImport("android.os.RemoteException")

                .writeImport("java.io.IOException")
                .writeImport("java.net.Socket")
                .writeImport("java.util.Arrays")
                .writeImport("java.util.List")
                .writeImport("java.util.UUID")

                .writeImport("moe.shizuku.server.io.ParcelInputStream")
                .writeImport("moe.shizuku.server.io.ParcelOutputStream")
                .writeImport("moe.shizuku.server.util.ServerLog")

                .writeLine("class RequestHandler {");

        writeLine("private Impl impl;");
        writeLine("RequestHandler(Impl impl) {\n" +
                "this.impl = impl;\n" +
                "}");

        writeInterface();
        writerHandleMethod();

        for (AidlMethod method : parser.methods) {
            writeMethod(method);
        }

        end();
    }

    private RequestHandlerWriter writeInterface() throws IOException {
        writeLine("interface Impl {");
        writeLine("boolean requireAuthorization(int action);");
        writeLine("boolean handleUnknownAction(int action, ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException;");
        for (AidlMethod method : parser.methods) {
            writeLine(method.toString() + " throws RemoteException;");
        }
        writeLine("}");
        return this;
    }

    private RequestHandlerWriter writerHandleMethod() throws IOException {
        writeLine("boolean handle(Socket socket, UUID token) throws IOException, RemoteException {\n" +
                "ParcelInputStream is = new ParcelInputStream(socket.getInputStream());\n" +
                "ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());\n" +
                "int action = is.readInt();\n" +
                "if (impl.requireAuthorization(action)) {\n" +
                "long most = is.readLong();\n" +
                "long least = is.readLong();\n" +
                "if (most != token.getMostSignificantBits()\n" +
                "&& least != token.getLeastSignificantBits()) {\n" +
                "os.writeException(new SecurityException(\"unauthorized\"));\n" +
                "is.close();\n" +
                "os.flush();\n" +
                "os.close();\n" +
                "return true;\n" +
                "}\n" +
                "}");

        writeLine("switch (action) {");

        for (AidlMethod method : parser.methods) {
            writeLine("case Actions." + method.name + ":");
            writeLine(method.name + "(is, os);");
            writeLine("break;");
        }
        writeLine("default:\nreturn impl.handleUnknownAction(action, is, os);");
        writeLine("}\n" +
                "is.close();\n" +
                "os.flush();\n" +
                "os.close();\n" +
                "return true;\n" +
                "}");
        return this;
    }

    @Override
    public Writer writeMethod(AidlMethod method) throws IOException {
        writeLine("private void " + method.name + "(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {");

        if (!method.args.isEmpty()) {
            for (Arg arg : method.args) {
                if (!arg.type.isArray()) {
                    if (!arg.type.isParcelable()) {
                        writeLine(String.format(Locale.ENGLISH, "%s = is.read%s();", arg.toString(), arg.type.getDataSteamType()));
                    } else {
                        writeLine(String.format(Locale.ENGLISH, "%s = is.readParcelable(%s.CREATOR);", arg.toString(), arg.type.name));
                    }
                } else {
                    writeLine(String.format(Locale.ENGLISH, "int %sLength = is.readInt();", arg.name));
                    writeLine(String.format(Locale.ENGLISH, "%s = null;", arg.toString()));
                    writeLine(String.format(Locale.ENGLISH, "if (%sLength > 0) {", arg.name));
                    writeLine(String.format(Locale.ENGLISH, "%s = new %s[%sLength];", arg.name, arg.type.toStringWithoutArray(), arg.name));
                    writeLine(String.format(Locale.ENGLISH, "for (int i = 0; i < %s.length; i++) {", arg.name));
                    writeLine(String.format(Locale.ENGLISH, "%s[i] = is.read%s();\n}\n}", arg.name, arg.type.getDataSteamType()));
                }
            }
        }

        if (method.returnType.isVoid()) {
            writeLine("try {");
            writeLine("impl." + method.toStringCall() + ";");
            writeLine("os.writeNoException();");
        } else {
            writeLine("try {");
            writeLine(method.returnType.name + " result = impl." + method.toStringCall() + ";");

            writeLine("os.writeNoException();");
            if (method.returnType.name.equals("Bitmap")) {
                writeLine("os.writeBitmap(result);");
            } else if (method.returnType.isList()
                    && method.returnType.isParcelable()) {
                writeLine("os.writeParcelableList(result);");
            } else if (method.returnType.isParcelable()) {
                writeLine("os.writeParcelable(result);");
            } else {
                writeLine("os.write" + method.returnType.getDataSteamType() + "(result);");
            }
        }
        writeLine("} catch (Exception e) {\n" +
                "if (!(e instanceof IOException)) {\n"+
                "os.writeException(e);");

        write("ServerLog.eStack(\"error when call " + method.name + "(\" ");
        for (Arg arg: method.args) {
            write("+ ");
            if (arg.type.isArray()) {
                write("Arrays.toString(" + arg.name + ")");
            } else {
                write(arg.name);
            }

            if (!arg.name.equals(method.args.get(method.args.size() - 1).name)) {
                write(" + \", \"");
            }
        }
        writeLine(" + \")\", e);");
        writeLine("}\n"+
                "}" );

        writeLine("}");

        return this;
    }
}
