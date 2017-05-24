package moe.shizuku;

/**
 * Created by Rikka on 2017/5/11.
 */

public class Arg {
    public Type type;
    public String name;

    public Arg(String arg) {
        this.type = new Type(arg.split(" ")[0]);
        this.name = arg.split(" ")[1];
    }

    public Arg(String type, String name) {
        this.type = new Type(type);
        this.name = name;
    }

    @Override
    public String toString() {
        return type.toString() + " " + name;
    }

    public String toStringCall() {
        return name;
    }
}
