package moe.shizuku.generator.creator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.VoidType;

import java.util.EnumSet;

import moe.shizuku.generator.helper.BinderHelper;
import moe.shizuku.generator.utils.MethodDeclarationUtils;

/**
 * Created by rikka on 2017/9/21.
 */

public class DelegateClassCreator {

    static final String PACKAGE = "moe.shizuku.server.delegate";
    static final String SUFFIX = "Delegate";

    public static CompilationUnit create(CompilationUnit cu) {
        ClassOrInterfaceDeclaration iBinder = (ClassOrInterfaceDeclaration) cu.getTypes().stream().findFirst().get();
        String pkg = PACKAGE;
        String binderName = iBinder.getNameAsString();
        String clsName = binderName.substring(1) + SUFFIX;

        ClassOrInterfaceDeclaration delegateClass = new ClassOrInterfaceDeclaration(
                EnumSet.of(Modifier.PUBLIC),
                false,
                clsName);

        CompilationUnit delegate = new CompilationUnit(pkg);
        delegate.addType(delegateClass)
                .addImport("android.os.RemoteException")
                .addImport("android.os.ServiceManager")
                .addImport(cu.getPackageDeclaration().get().getNameAsString() + "." + binderName);

        delegate.getImports().addAll(cu.getImports());

        addSingletonFiled(delegate, delegateClass, binderName);

        iBinder.getMembers().stream()
                .filter(bodyDeclaration -> bodyDeclaration instanceof MethodDeclaration)
                .map(bodyDeclaration -> (MethodDeclaration) bodyDeclaration)
                .forEach(method -> addMethod(delegateClass, iBinder, method.clone()));

        return delegate;
    }

    private static void addSingletonFiled(CompilationUnit delegate, ClassOrInterfaceDeclaration delegateClass, String binderName) {
        MethodDeclaration methodDeclaration = new MethodDeclaration(
                EnumSet.of(Modifier.PROTECTED),
                JavaParser.parseClassOrInterfaceType(binderName),
                "create");
        methodDeclaration.addAndGetAnnotation(Override.class);

        if ("IActivityManager".equals(binderName)) {
            delegate.addImport("android.app.ActivityManagerNative");

            methodDeclaration.setBody(new BlockStmt().addStatement("return ActivityManagerNative.getDefault();"));
        } else {
            methodDeclaration.setBody(new BlockStmt().addStatement(
                    String.format("return %s.Stub.asInterface(ServiceManager.getService(\"%s\"));"
                            , binderName, BinderHelper.getServiceName(binderName))));
        }

        NodeList<BodyDeclaration<?>> anonymousClassBody = new NodeList<>();
        anonymousClassBody.add(methodDeclaration);

        delegate.addImport("android.util.Singleton");

        delegateClass.addMember(new FieldDeclaration(
                EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                new VariableDeclarator(
                        JavaParser.parseClassOrInterfaceType("Singleton<" + binderName + ">"),
                        binderName + "Singleton",
                        new ObjectCreationExpr(
                                null,
                                JavaParser.parseClassOrInterfaceType("Singleton<" + binderName + ">"),
                                null,
                                new NodeList<>(),
                                anonymousClassBody
                        ))
        ));
    }

    private static void addMethod(ClassOrInterfaceDeclaration cls, ClassOrInterfaceDeclaration binder, MethodDeclaration method) {
        String statement = getMethodReturningStatement(binder.getNameAsString(), method);

        if (method.getType().asString().startsWith("ParceledListSlice")) {
            String t = ((ClassOrInterfaceType) method.getType()).getTypeArguments().get().stream().findFirst().get().asString();
            method.setType(JavaParser.parseClassOrInterfaceType("List<" + t + ">").asString());
        }

        cls.addMember(method
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .addThrownException(new TypeParameter("RemoteException"))
                .setBody(new BlockStmt().addStatement(statement)));
    }

    /** return IAppOpsServiceSingleton.get().checkOperation(code, uid, packageName);
     *
     * @param binderName
     * @param method
     * @return
     */
    private static String getMethodReturningStatement(String binderName, MethodDeclaration method) {
        String format = "%sSingleton.get().%s;";
        if (!(method.getType() instanceof VoidType)) {
            format = "return " + format;
        }

        String call = MethodDeclarationUtils.toCallingStatementString(method, false);
        if (method.getType().asString().startsWith("ParceledListSlice")) {
            call += ".getList()";
        }

        return String.format(format, binderName, call);
    }

    /** return IAppOpsServiceSingleton.get().checkOperation(code, uid, packageName);
     *
     * @param binderName
     * @param method
     * @return
     */
    public static String getMethodCallingStatement(String binderName, MethodDeclaration method) {
        String format = "%sDelegate.%s;";

        return String.format(format, binderName.substring(1), MethodDeclarationUtils.toCallingStatementString(method, true));
    }
}
