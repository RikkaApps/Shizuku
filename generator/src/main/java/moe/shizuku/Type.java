package moe.shizuku;

/**
 * Created by Rikka on 2017/5/11.
 */

public class Type {

    public String name;

    public Type(String name) {
        this.name = name;
    }

    public boolean isArray() {
        return name.endsWith("[]");
    }

    public boolean isList() {
        return name.startsWith("List<");
    }

    public boolean isVoid() {
        return name.equals("void");
    }

    public boolean isParcelable() {
        return name.charAt(0) < 'a' && !name.equals("String");
    }

    public String getTypeInList() {
        return name.substring("List<".length(), name.length() - 1);
    }

    public String getDataSteamType() {
        String t = name.substring(0, 1).toUpperCase() + name.substring(1);
        t = t.replace("[]", "");
        /*switch (t) {
            case "String":
                t = "UTF";
        }*/
        return t;
    }

    public String getDefaultValue() {
        if (isList()) {
            return "new ArrayList<>()";
        } else if (name.equals("boolean")) {
            return "false";
        } else if (!isParcelable()) {
            return "-1";
        }
        return "null";
    }

    @Override
    public String toString() {
        return name;
    }

    public String toStringWithoutArray() {
        return name.replace("[]", "");
    }

    public String toStringParceledListSlice(String extra) {
        return name + "<" + extra + ">";
    }

    public String toStringParceledListSlice2(String extra) {
        return "List<" + extra + ">";
    }
}
