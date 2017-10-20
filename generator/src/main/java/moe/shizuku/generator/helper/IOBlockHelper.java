package moe.shizuku.generator.helper;

import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rikka on 2017/9/24.
 */

public class IOBlockHelper {

    private static List<String> noCreatorParcelables = new ArrayList<>();

    public static void clear() {
        noCreatorParcelables.clear();
    }

    public static void readNoCreatorParcelables(File file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String line = br.readLine();
            while (line != null) {
                noCreatorParcelables.add(line.trim());

                line = br.readLine();
            }
        } catch (IOException ignored) {
        }
    }

    private static String getStreamTypeName(Type type) {
        if (type instanceof PrimitiveType) {
            return (type.asString().substring(0, 1).toUpperCase()
                    + type.asString().substring(1));
        } else if (type instanceof ArrayType) {
            Type componentType = ((ArrayType) type).getComponentType();
            if (isTypeParcelable(componentType)) {
                return "ParcelableArray";
            } else if (isTypeBinderOrInterface(type)) {
                return componentType.asString().equals("IBinder") ? "Binder" : "Interface";
            } else {
                return (componentType.asString().substring(0, 1).toUpperCase()
                        + componentType.asString().substring(1)) + "Array";
            }
        } else if (type instanceof ClassOrInterfaceType) {
            switch (type.asString()) {
                case "String":
                case "Bitmap":
                case "CharSequence":
                    return type.asString();
                default:
                    Type inner = isTypeList(type);

                    if (inner != null) {
                        switch (inner.asString()) {
                            case "String":
                                return "StringList";
                            default:
                                if (isTypeBinderOrInterface(inner)) {
                                    return inner.asString().equals("IBinder") ? "BinderList" : "InterfaceList";
                                }
                                return "ParcelableList";
                        }
                    } else {
                        if (isTypeBinderOrInterface(type)) {
                            return type.asString().equals("IBinder") ? "Binder" : "Interface";
                        }
                        return "Parcelable";
                    }
            }
        }
        throw new UnsupportedOperationException("unknown type " + type);
    }

    private static String getReadParameter(Type type) {
        if (isTypeList(type) != null) {
            return getReadParameter(isTypeList(type));
        }

        if (type instanceof PrimitiveType) {
            return "";
        } else if (type instanceof ArrayType) {
            Type componentType = ((ArrayType) type).getComponentType();
            if (isTypeParcelable(componentType)) {
                if (noCreatorParcelables.contains(componentType.asString())) {
                    return componentType.asString() + ".class";
                } else {
                    return componentType.asString() + ".CREATOR";
                }
            } else {
                return "";
            }
        }
        switch (type.asString()) {
            case "String":
            case "Bitmap":
            case "CharSequence":
                return "";
            default:
                if (isTypeBinderOrInterface(type)) {
                    return "";
                } else {
                    if (noCreatorParcelables.contains(type.asString())) {
                        return type.asString() + ".class";
                    } else {
                        return type.asString() + ".CREATOR";
                    }
                }
        }
    }

    public static String getReadStatement(String name, Type type) {
        StringBuilder sb = new StringBuilder();
        sb.append(type.asString()).append(' ').append(name).append('=').append(getReadStatement(type));
        return sb.toString();
    }

    public static String getReadStatement(Type type) {
        StringBuilder sb = new StringBuilder();
        if (isTypeBinderOrInterface(type)) {
            sb.append('(').append(type.asString()).append(')');
        }
        sb.append("is.read").append(getStreamTypeName(type)).append('(').append(getReadParameter(type)).append(')').append(';');
        return sb.toString();
    }

    public static String getWriteStatement(Type type) {
        StringBuilder sb = new StringBuilder();
        sb.append("os.write").append(getStreamTypeName(type)).append("(result);");
        return sb.toString();
    }

    public static String getWriteStatement(String name, Type type) {
        StringBuilder sb = new StringBuilder();
        sb.append("os.write").append(getStreamTypeName(type)).append("(").append(name).append(");");
        return sb.toString();
    }

    public static Type isTypeList(Type type) {
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType cls = (ClassOrInterfaceType) type;
            if (cls.getName().toString().equals("List")) {
                return cls.getTypeArguments().get().stream().findFirst().get();
            }
        }

        return null;
    }

    public static boolean isTypeParcelable(Type type) {
        String typeName = type.asString();
        return type instanceof ClassOrInterfaceType
                && !typeName.equals("String")
                && !typeName.equals("Bitmap");
    }

    public static boolean isTypeBinderOrInterface(Type type) {
        if (type instanceof ArrayType) {
            return isTypeBinderOrInterface(((ArrayType) type).getComponentType());
        } else {
            Type inner = isTypeList(type);

            if (inner != null) {
                return isTypeBinderOrInterface(inner);
            } else {
                String typeName = type.asString();
                return typeName.charAt(0) == 'I'
                        && Character.isUpperCase(typeName.charAt(1));
            }
        }
    }
}
