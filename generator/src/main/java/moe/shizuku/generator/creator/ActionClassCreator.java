package moe.shizuku.generator.creator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.type.PrimitiveType;

import java.util.EnumSet;

import moe.shizuku.generator.helper.BinderHelper;

/**
 * Created by rikka on 2017/9/24.
 */

public class ActionClassCreator {

    private static CompilationUnit cu;
    private static int count;

    public static void clear() {
        cu = null;
        count = 0;
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

        count = 0;

        binderClass.getMembers().stream()
                .filter(bodyDeclaration -> bodyDeclaration instanceof MethodDeclaration)
                .map(bodyDeclaration -> (MethodDeclaration) bodyDeclaration)
                .forEach(method -> ActionClassCreator.addFiled(cls, binderName, method.clone()));

        return cu;
    }

    private static void addFiled(ClassOrInterfaceDeclaration cls, String binderName, MethodDeclaration method) {
        String name = getActionName(binderName, method);
        int index = (BinderHelper.getIndex(binderName) + 1) * 10000 + count++;
        cls.addField(int.class, name, Modifier.STATIC, Modifier.FINAL, Modifier.PROTECTED)
                .setVariable(0, new VariableDeclarator(PrimitiveType.intType(), name, new IntegerLiteralExpr(index)));
    }

    public static String getActionName(String binderName, MethodDeclaration method) {
        return binderName.substring(1) + "_" + method.getNameAsString();
    }
}
