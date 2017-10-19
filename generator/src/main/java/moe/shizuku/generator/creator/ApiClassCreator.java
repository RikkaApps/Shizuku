package moe.shizuku.generator.creator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.VoidType;

import java.util.EnumSet;

import moe.shizuku.generator.helper.IOBlockHelper;

/**
 * Created by rikka on 2017/9/24.
 */

public class ApiClassCreator {

    private static final String PACKAGE = "moe.shizuku.api";
    private static final String PREFIX = "Shizuku";
    private static String SUFFIX;

    public static void setApiVersion(int apiVersion) {
        ApiClassCreator.SUFFIX = "V" + apiVersion;
    }

    public static CompilationUnit create(CompilationUnit cu) {
        ClassOrInterfaceDeclaration iBinder = (ClassOrInterfaceDeclaration) cu.getTypes().stream().findFirst().get();
        String pkg = PACKAGE;
        String binderName = iBinder.getNameAsString();
        String clsName = PREFIX + binderName.substring(1) + SUFFIX;

        ClassOrInterfaceDeclaration delegateClass = new ClassOrInterfaceDeclaration(
                EnumSet.of(Modifier.PUBLIC),
                false,
                clsName);

        CompilationUnit delegate = new CompilationUnit(pkg);
        delegate.addType(delegateClass)
                .addImport("java.io.IOException")
                .addImport("java.net.Socket")
                .addImport("android.os.IInterface")
                .addImport("moe.shizuku.ShizukuConstants")
                .addImport("moe.shizuku.io.ParcelInputStream")
                .addImport("moe.shizuku.io.ParcelOutputStream")
                .addImport("moe.shizuku.lang.ShizukuRemoteException");

        delegate.getImports().addAll(cu.getImports());

        iBinder.getMembers().stream()
                .filter(bodyDeclaration -> bodyDeclaration instanceof MethodDeclaration)
                .map(bodyDeclaration -> (MethodDeclaration) bodyDeclaration)
                .filter(ApiClassCreator::filterMethod)
                .forEach(method -> addMethod(delegateClass, iBinder, method.clone()));

        return delegate;
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

    private static void addMethod(ClassOrInterfaceDeclaration cls, ClassOrInterfaceDeclaration binder, MethodDeclaration method) {
        //NodeList<Parameter> nodeList = new NodeList<>();
        method.getParameters()
                .stream()
                /*.filter(parameter -> !IOBlockHelper.isTypeBinderOrInterface(parameter.getType()))
                .forEach(nodeList::add);*/
                .forEach(parameter -> {
                    if (IOBlockHelper.isTypeBinderOrInterface(parameter.getType())) {
                        if (!"IBinder".equals(parameter.getNameAsString())) {
                            parameter.setType("IInterface");
                        }
                    }
                });
        //method.setParameters(nodeList);
        cls.addMember(method
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .addThrownException(new TypeParameter("ShizukuRemoteException"))
                .setBody(getBlock(binder.getNameAsString(), method)));
    }

    private static BlockStmt getBlock(String binderName, MethodDeclaration method) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("try{")
                .append("Socket client = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);")
                .append("client.setSoTimeout(ShizukuConstants.TIMEOUT);")
                .append("ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());")
                .append("ParcelInputStream is = new ParcelInputStream(client.getInputStream());")
                .append("os.writeInt(Actions").append(SUFFIX).append('.').append(ActionClassCreator.getActionName(binderName, method)).append(");")
                .append("os.writeLong(ShizukuClient.getToken().getMostSignificantBits());")
                .append("os.writeLong(ShizukuClient.getToken().getLeastSignificantBits());");

        method.getParameters().forEach(parameter ->
                sb.append(IOBlockHelper.getWriteStatement(parameter.getNameAsString(), parameter.getType())));

        sb.append("is.readException();");

        if (!(method.getType() instanceof VoidType)) {
            sb.append(IOBlockHelper.getReadStatement("_result", method.getType()));
            sb.append("return _result;");
        } else {
        }

        sb.append("}catch(IOException e){\n" +
                "throw new ShizukuRemoteException(\"Problem connect to shizuku server.\", e);" +
                "}")
                .append('}');

        return JavaParser.parseBlock(sb.toString());
    }
}
