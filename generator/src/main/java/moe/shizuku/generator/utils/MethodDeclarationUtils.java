package moe.shizuku.generator.utils;

import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Created by rikka on 2017/9/21.
 */

public class MethodDeclarationUtils {

    /**
     *
     * @param method
     * @return
     */
    public static String toCallingStatementString(MethodDeclaration method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getNameAsString()).append('(');
        if (method.getParameters().isNonEmpty()) {
            method.getParameters()
                    .forEach(parameter -> sb.append(parameter.getName()).append(","));

            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.append(')').toString();
    }
}
