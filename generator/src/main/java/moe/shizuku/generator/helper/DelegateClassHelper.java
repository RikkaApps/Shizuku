package moe.shizuku.generator.helper;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.VoidType;

import java.util.EnumSet;

import moe.shizuku.generator.utils.MethodDeclarationUtils;

/**
 * Created by rikka on 2017/9/21.
 */

public class DelegateClassHelper {

    public static CompilationUnit create(CompilationUnit cu) {
        ClassOrInterfaceDeclaration iBinder = (ClassOrInterfaceDeclaration) cu.getTypes().stream().findFirst().get();
        String pkg = "moe.shizuku.server.delegate";
        String binderName = iBinder.getNameAsString();
        String clsName = binderName.substring(1);

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

        cu.getTypes().stream().findFirst().get().getMembers().stream()
                .filter(bodyDeclaration -> bodyDeclaration instanceof MethodDeclaration)
                .map(bodyDeclaration -> (MethodDeclaration) bodyDeclaration)
                .forEach(method -> DelegateClassHelper.addMethod(delegateClass, iBinder, method.clone()));

        return delegate;
    }

    public static void addMethod(ClassOrInterfaceDeclaration cls, ClassOrInterfaceDeclaration binder, MethodDeclaration method) {
        cls.addMember(method
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .addThrownException(new TypeParameter("RemoteException"))
                .setBody(new BlockStmt().addStatement(getMethodReturningStatement(binder.getNameAsString(), method))));
    }

    public static String getMethodReturningStatement(String name, MethodDeclaration method) {
        String format = "%s.Stub.asInterface(ServiceManager.getService(\"%s\"))." +
                "%s;";
        if (!(method.getType() instanceof VoidType)) {
            format = "return " + format;
        }

        return String.format(format, name, "package", MethodDeclarationUtils.toCallingStatementString(method));
    }
}
