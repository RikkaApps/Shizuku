package moe.shizuku;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rikka on 2017/5/11.
 */

public class AidlMethod {
    public Type returnType;
    public String name;
    public String displayName;
    public List<Arg> args;
    public String extra;

    public AidlMethod(String returnType, String name, String args, String extra, List<AidlMethod> methods) {
        this.returnType = new Type(returnType);
        this.name = name;
        this.args = new ArrayList<>();
        this.extra = extra;

        int count = 0;
        for (AidlMethod m : methods) {
            if (m.name.equals(name)) {
                count++;
            }
        }

        if (count > 0) {
            displayName = name + "_" + count;
        } else {
            displayName = name;
        }

        if (args.trim().length() > 0) {
            for (String arg : args.split(",")) {
                this.args.add(new Arg(arg.trim().replace("in ", "").replace("out ", "").replace("inout ", "")));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Arg arg : args) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(arg.toString());
        }
        return returnType.toString() + ' ' + name +
                "(" + sb.toString() + ")";
    }

    public String toStringCall() {
        StringBuilder sb = new StringBuilder();
        for (Arg arg : args) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(arg.toStringCall());
        }
        return name +
                "(" + sb.toString() + ")";
    }

    public boolean useParceledListSlice() {
        return extra != null && extra.startsWith("ParceledListSlice");
    }
}
