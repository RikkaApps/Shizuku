package moe.shizuku.generator.creator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.VoidType;

import java.io.IOException;
import java.util.EnumSet;

import moe.shizuku.generator.helper.IOBlockHelper;
import moe.shizuku.generator.utils.CompilationUnitUtils;
import moe.shizuku.generator.utils.MethodDeclarationUtils;

/**
 * Created by rikka on 2017/9/24.
 */

public class RequestHandlerClassCreator {

    private static CompilationUnit cu;
    private static SwitchStmt swichStmt;

    public static void clear() {
        cu = null;
    }

    public static CompilationUnit get() {
        return cu;
    }

    public static CompilationUnit createOrAdd(CompilationUnit binderCu) {
        ClassOrInterfaceDeclaration binderClass = (ClassOrInterfaceDeclaration) binderCu.getTypes().stream().findFirst().get();
        String pkg = "moe.shizuku.server.api";
        String binderName = binderClass.getNameAsString();
        String clsName = "RequestHandler";

        ClassOrInterfaceDeclaration cls;

        if (cu == null) {
            BlockStmt handleStmt = new BlockStmt().addStatement(
                    "switch (action) {\n" +
                            "    default:\n" +
                            "        os.writeException(new SecurityException(\"unknown action: \" + action));\n" +
                            "        break;\n" +
                            "}"
            );

            cls = new ClassOrInterfaceDeclaration(
                    EnumSet.of(Modifier.PUBLIC),
                    false,
                    clsName);

            cls.addMethod("handle", Modifier.PUBLIC)
                    .addParameter(String.class, "action")
                    .addParameter("ServerParcelInputStream", "is")
                    .addParameter("ServerParcelOutputStream", "os")
                    .addThrownException(new TypeParameter("IOException"))
                    .addThrownException(new TypeParameter("RemoteException"))
                    .setBody(handleStmt);

            swichStmt = (SwitchStmt) handleStmt.getStatement(0);

            cu = new CompilationUnit(pkg);
            cu.addType(cls)
                    .addImport(IOException.class)
                    .addImport("android.os.RemoteException")
                    .addImport("moe.shizuku.server.io.ServerParcelInputStream")
                    .addImport("moe.shizuku.server.io.ServerParcelOutputStream");
        } else {
            cls = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
        }

        CompilationUnitUtils.addImport(cu, DelegateClassCreator.PACKAGE + "." + binderName.substring(1) + DelegateClassCreator.SUFFIX);
        CompilationUnitUtils.addImports(cu, binderCu.getImports());

        binderClass.getMembers().stream()
                .filter(bodyDeclaration -> bodyDeclaration instanceof MethodDeclaration)
                .map(bodyDeclaration -> (MethodDeclaration) bodyDeclaration)
                .filter(RequestHandlerClassCreator::filterMethod)
                .forEach(method -> RequestHandlerClassCreator.addMethod(cls, binderName, method.clone()));

        return cu;
    }

    private static boolean filterMethod(MethodDeclaration method) {
        /*if (IOBlockHelper.isTypeBinderOrInterface(method.getType())) {
            return false;
        }

        for (Parameter parameter: method.getParameters()) {
            if (IOBlockHelper.isTypeBinderOrInterface(parameter.getType())) {
                return false;
            }
        }*/
        return true;
    }

    private static void addMethod(ClassOrInterfaceDeclaration cls, String binderName, MethodDeclaration source) {
        MethodDeclaration method = cls.addMethod(getMethodName(binderName, source), Modifier.PRIVATE, Modifier.STATIC)
                .addThrownException(new TypeParameter("IOException"))
                .addThrownException(new TypeParameter("RemoteException"))
                .addParameter("ServerParcelInputStream", "is")
                .addParameter("ServerParcelOutputStream", "os")
                .setBody(getBlock(binderName, source));

        swichStmt.addEntry(new SwitchEntryStmt(
                JavaParser.parseExpression("Actions." + ActionClassCreator.getActionName(binderName, source)),
                NodeList.nodeList(
                        JavaParser.parseStatement(MethodDeclarationUtils.toCallingStatementString(method, false) + ";"),
                        JavaParser.parseStatement("break;"))
        ));
    }

    private static String getMethodName(String binderName, MethodDeclaration method) {
        return binderName.substring(1) + "_" + method.getNameAsString();
    }

    private static BlockStmt getBlock(String binderName, MethodDeclaration method) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        method.getParameters().forEach(parameter -> sb.append(IOBlockHelper.getReadStatement(parameter.getNameAsString(), parameter.getType())));

        if ("IBinder".equals(method.getType().asString())
                || IOBlockHelper.isTypeInterface(method.getType())
                || "ParcelFileDescriptor".equals(method.getType().asString())) {
            sb.append("int clientUserId = is.readInt();");
        }
        sb.append("try{");
        if (!(method.getType() instanceof VoidType)) {
            String t = method.getType().asString();
            if (method.getType().asString().startsWith("ParceledListSlice")) {
                t = ((ClassOrInterfaceType) method.getType()).getTypeArguments().get().stream().findFirst().get().asString();
                t = JavaParser.parseClassOrInterfaceType("List<" + t + ">").asString();
            }

            sb.append(t).append(' ').append("result").append('=')
                    .append(DelegateClassCreator.getMethodCallingStatement(binderName, method));
            sb.append("os.writeNoException();");
            sb.append(IOBlockHelper.getWriteStatement(method.getType()));
        } else {
            sb.append(DelegateClassCreator.getMethodCallingStatement(binderName, method));
            sb.append("os.writeNoException();");
        }

        sb.append("}catch(Throwable tr){\n" +
                "if (!(tr instanceof IOException)) {\n" +
                "    os.writeException(tr);\n" +
                "}}")
                .append('}');
        //System.out.println(sb.toString());
        return JavaParser.parseBlock(sb.toString());
    }
}
