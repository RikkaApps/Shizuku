package moe.shizuku.generator.utils;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

/**
 * Created by rikka on 2017/9/21.
 */

public class MethodDeclarationUtils {

    /**
     *
     * @param method
     * @return
     */
    public static String toCallingStatementString(MethodDeclaration method, boolean parceledListSlice) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getNameAsString()).append('(');
        if (method.getParameters().isNonEmpty()) {
            method.getParameters()
                    .forEach(parameter -> {
                        if (parceledListSlice && parameter.getType().asString().startsWith("ParceledListSlice")) {
                            String t = ((ClassOrInterfaceType) parameter.getType()).getTypeArguments().get().stream().findFirst().get().asString();
                            sb.append("new ParceledListSlice<").append(t).append(">(").append(parameter.getName()).append(')');
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
