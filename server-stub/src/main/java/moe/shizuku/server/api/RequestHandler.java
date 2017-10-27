package moe.shizuku.server.api;

import moe.shizuku.server.io.ServerParcelInputStream;
import moe.shizuku.server.io.ServerParcelOutputStream;

public class RequestHandler {

    public void handle(String action, ServerParcelInputStream is, ServerParcelOutputStream os) {
        throw new RuntimeException("STUB");
    }
}
