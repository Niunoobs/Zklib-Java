package zklibjs.java;

import java.util.Date;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class Utils {
    private static final int USHRT_MAX = 65535;
    private static final Map<String, Integer> COMMANDS = new HashMap<>(); // Assuming COMMANDS is defined elsewhere

    private static Date parseTimeToDate(int time) {
        int second = time % 60;
        time = (time - second) / 60;
        int minute = time % 60;
        time = (time - minute) / 60;
        int hour = time % 24;
        time = (time - hour) / 24;
        int day = time % 31 + 1;
        time = (time - (day - 1)) / 31;
        int month = time % 12;
        time = (time - month) / 12;
        int year = time + 2000;
        
        return new Date(year - 1900, month, day, hour, minute, second);
    }

    private static Date parseHexToTime(byte[] hex) {
        int year = Byte.toUnsignedInt(hex[0]);
        int month = Byte.toUnsignedInt(hex[1]);
        int date = Byte.toUnsignedInt(hex[2]);
        int hour = Byte.toUnsignedInt(hex[3]);
        int minute = Byte.toUnsignedInt(hex[4]);
        int second = Byte.toUnsignedInt(hex[5]);
        
        return new Date(2000 + year - 1900, month - 1, date, hour, minute, second);
    }

    private static int createChkSum(byte[] buf) {
        int chksum = 0;
        for (int i = 0; i < buf.length; i += 2) {
            if (i == buf.length - 1) {
                chksum += Byte.toUnsignedInt(buf[i]);
            } else {
                chksum += ByteBuffer.wrap(buf, i, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
            }
            chksum %= USHRT_MAX;
        }
        chksum = USHRT_MAX - chksum - 1;
        
        return chksum;
    }

    public static byte[] createUDPHeader(int command, int sessionId, int replyId, byte[] data) {
        ByteBuffer buf = ByteBuffer.allocate(8 + data.length).order(ByteOrder.LITTLE_ENDIAN);
        
        buf.putShort((short) command);
        buf.putShort((short) 0);
        buf.putShort((short) sessionId);
        buf.putShort((short) replyId);
        buf.put(data);
        
        int chksum2 = createChkSum(buf.array());
        buf.putShort(2, (short) chksum2);
        
        replyId = (replyId + 1) % USHRT_MAX;
        buf.putShort(6, (short) replyId);
        
        return buf.array();
    }

    public static byte[] createTCPHeader(int command, int sessionId, int replyId, byte[] data) {
        ByteBuffer buf = ByteBuffer.allocate(8 + data.length).order(ByteOrder.LITTLE_ENDIAN);
        
        buf.putShort((short) command);
        buf.putShort((short) 0);
        buf.putShort((short) sessionId);
        buf.putShort((short) replyId);
        buf.put(data);
        
        int chksum2 = createChkSum(buf.array());
        buf.putShort(2, (short) chksum2);
        
        replyId = (replyId + 1) % USHRT_MAX;
        buf.putShort(6, (short) replyId);
        
        byte[] prefixBuf = {0x50, 0x50, (byte) 0x82, 0x7d, 0x13, 0x00, 0x00, 0x00};
        ByteBuffer.wrap(prefixBuf, 4, 2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) buf.array().length);
        
        byte[] result = new byte[prefixBuf.length + buf.array().length];
        System.arraycopy(prefixBuf, 0, result, 0, prefixBuf.length);
        System.arraycopy(buf.array(), 0, result, prefixBuf.length, buf.array().length);
        
        return result;
    }

    public static byte[] removeTcpHeader(byte[] buf) {
        if (buf.length < 8) {
            return buf;
        }
        
        byte[] header = {0x50, 0x50, (byte) 0x82, 0x7d};
        if (!Arrays.equals(Arrays.copyOfRange(buf, 0, 4), header)) {
            return buf;
        }
        
        return Arrays.copyOfRange(buf, 8, buf.length);
    }

    // Other methods would be implemented similarly...

    public static String exportErrorMessage(int commandValue) {
        for (Map.Entry<String, Integer> entry : COMMANDS.entrySet()) {
            if (entry.getValue() == commandValue) {
                return entry.getKey();
            }
        }
        return "AN UNKNOWN ERROR";
    }

    public static boolean checkNotEventTCP(byte[] data) {
        try {
            data = removeTcpHeader(data);
            int commandId = ByteBuffer.wrap(data, 0, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
            int event = ByteBuffer.wrap(data, 4, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
            return event == COMMANDS.get("EF_ATTLOG") && commandId == COMMANDS.get("CMD_REG_EVENT");
        } catch (Exception e) {
            // log("[228] : " + e.toString() + " ," + bytesToHex(data));
            return false;
        }
    }

    public static boolean checkNotEventUDP(byte[] data) {
        int commandId = decodeUDPHeader(Arrays.copyOfRange(data, 0, 8)).commandId;
        return commandId == COMMANDS.get("CMD_REG_EVENT");
    }

    // Helper method to convert bytes to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Other methods and nested classes would be implemented here...
}

