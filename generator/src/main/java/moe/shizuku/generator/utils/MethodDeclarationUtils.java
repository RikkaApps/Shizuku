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
                    .forEach(parameter -> {
                        if (parameter.getType().asString().startsWith("ParceledListSlice")) {
                            sb.append("new ParceledListSlice<>(").append(parameter.getName()).append(')');
                        } else {
                            sb.append(parameter.getName());
                        }
                        sb.append(",");
                    });

            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.append(')').toString();
    }
}
