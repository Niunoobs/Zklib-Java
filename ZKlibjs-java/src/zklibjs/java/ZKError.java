package zklibjs.java;

import java.util.HashMap;
import java.util.Map;

public class ZKError {

    public static final Map<String, String> ERROR_TYPES = new HashMap<String, String>() {{
        put("ECONNRESET", "ECONNRESET");
        put("ECONNREFUSED", "ECONNREFUSED");
        put("EADDRINUSE", "EADDRINUSE");
        put("ETIMEDOUT", "ETIMEDOUT");
    }};

    public static class ZKError {
        private Exception err;
        private String ip;
        private String command;

        public ZKError(Exception err, String command, String ip) {
            this.err = err;
            this.ip = ip;
            this.command = command;
        }

        public String toast() {
            if (err instanceof java.net.SocketException && 
                err.getMessage().contains(ERROR_TYPES.get("ECONNRESET"))) {
                return "Another device is connecting to the device so the connection is interrupted";
            } else if (err instanceof java.net.ConnectException && 
                       err.getMessage().contains(ERROR_TYPES.get("ECONNREFUSED"))) {
                return "IP of the device is refused";
            } else {
                return err.getMessage();
            }
        }

        public Map<String, Object> getError() {
            Map<String, Object> errorMap = new HashMap<>();
            Map<String, String> errDetails = new HashMap<>();
            errDetails.put("message", err.getMessage());
            errDetails.put("code", err.getClass().getSimpleName());
            errorMap.put("err", errDetails);
            errorMap.put("ip", ip);
            errorMap.put("command", command);
            return errorMap;
        }
    }
}

