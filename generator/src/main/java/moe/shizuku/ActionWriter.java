package moe.shizuku;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by Rikka on 2017/5/11.
 */

public class ActionWriter extends Writer {

    private int index;

    @Override
    public void write(String module, String packageName, AidlParser parser) throws IOException {
        super.write(module, packageName, parser);

        File file = new File(filename);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }

        writer = new BufferedWriter(new FileWriter(file));

        start();
        writeClass("Actions");

        index = 0;
        String currentType = parser.types.get(0);
        for (int i = 0; i < parser.methods.size(); i++) {
            AidlMethod method = parser.methods.get(i);
            if (!parser.types.get(i).equals(currentType)) {
                currentType = parser.types.get(i);
                index = (index / 1000 + 1) * 1000;
            }
            writeMethod(method);
        }

        end();
    }

    @Override
    public Writer writeMethod(AidlMethod method) throws IOException {
        return writeLine(String.format(Locale.ENGLISH, "public static final int %s = %d;", method.displayName, index++));
    }
}
