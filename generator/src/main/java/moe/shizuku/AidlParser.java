package moe.shizuku;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rikka on 2017/5/10.
 */

public class AidlParser {

    public String packageName;
    public String interfaceName;

    public List<String> imports = new ArrayList<>();
    public List<AidlMethod> methods = new ArrayList<>();
    public List<String> types = new ArrayList<>();

    private void parse(File file) throws IOException {
        imports.clear();
        methods.clear();
        types.clear();

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        String currentType = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0
                    || line.equals("}")) {
                continue;
            }

            line = line.replace(";", "").replace(" }", "");

            if (line.startsWith("//")) {
                currentType = line.substring(2).trim();
                continue;
            }

            if (line.startsWith("package ")) {
                packageName = line.substring("package ".length());
            } else if (line.startsWith("import ")) {
                imports.add(line.substring("import ".length()));
            } else if (line.startsWith("interface ")) {
                interfaceName = line.substring("interface ".length());
            } else {
                String returnType = line.substring(0, line.indexOf(' '));
                String name = line.substring(line.indexOf(' ') + 1, line.indexOf('('));
                String args = line.substring(line.indexOf('(') + 1, line.indexOf(')')).replace(")", "");
                String comment = line.contains("//") ? line.substring(line.indexOf("//") + 3).trim() : null;

                types.add(currentType);
                methods.add(new AidlMethod(returnType, name, args, comment));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        AidlParser parser = new AidlParser();

        parser.parse(new File("../apis.aidl"));
        if (args[0].equals("api")) {
            new ActionWriter().write("../api", "moe.shizuku.server.Actions", parser);
            new AbstractPrivilegedAPIsWtiter().write("../api", "moe.shizuku.privileged.api.AbstractPrivilegedAPIs", parser);
        } else if (args[0].equals("server")) {
            new ActionWriter().write("../server", "moe.shizuku.server.Actions", parser);
            new RequestHandlerWriter().write("../server", "moe.shizuku.server.RequestHandler", parser);
        }
    }
}
