package moe.shizuku.generator.creator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.EnumSet;

/**
 * Created by rikka on 2017/9/24.
 */

public class ActionClassCreator {

    private static CompilationUnit cu;

    public static void clear() {
        cu = null;
    }

    public static CompilationUnit get() {
        return cu;
    }

    public static void toApi(int apiVersion) {
        ActionClassCreator.get().setPackageDeclaration("moe.shizuku.api");

        ClassOrInterfaceDeclaration cls = (ClassOrInterfaceDeclaration) ActionClassCreator.get().getTypes().get(0);
        cls.setName("ActionsV" + apiVersion);
        cls.setModifiers(EnumSet.noneOf(Modifier.class));
    }

    public static CompilationUnit createOrAdd(CompilationUnit binderCu) {
        ClassOrInterfaceDeclaration binderClass = (ClassOrInterfaceDeclaration) binderCu.getTypes().stream().findFirst().get();
        String pkg = "moe.shizuku.server.api";
        String binderName = binderClass.getNameAsString();
        String clsName = "Actions";

        ClassOrInterfaceDeclaration cls;

        if (cu == null) {
            cls = new ClassOrInterfaceDeclaration(
                    EnumSet.of(Modifier.PUBLIC),
                    false,
                    clsName);

            cu = new CompilationUnit(pkg);
            cu.addType(cls);
        } else {
            cls = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
        }

        binderClass.getMembers().stream()
                .filter(bodyDeclaration -> bodyDeclaration instanceof MethodDeclaration)
                .map(bodyDeclaration -> (MethodDeclaration) bodyDeclaration)
                .forEach(method -> ActionClassCreator.addFiled(cls, binderName, method.clone()));

        return cu;
    }

    private static void addFiled(ClassOrInterfaceDeclaration cls, String binderName, MethodDeclaration method) {
        String name = getActionName(binderName, method);
        cls.addField(String.class, name, Modifier.STATIC, Modifier.FINAL, Modifier.PROTECTED)
                .setVariable(0, new VariableDeclarator(JavaParser.parseClassOrInterfaceType("String"), name, new StringLiteralExpr(name)));
    }

    public static String getActionName(String binderName, MethodDeclaration method) {
        return binderName.substring(1) + "_" + method.getNameAsString();
    }
}
