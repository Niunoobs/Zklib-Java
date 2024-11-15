import java.nio.ByteBuffer;

public class Constants {
    public static final class COMMANDS {
        public static final int CMD_CONNECT = 1000;
        public static final int CMD_EXIT = 1001;
        public static final int CMD_ENABLEDEVICE = 1002;
        public static final int CMD_DISABLEDEVICE = 1003;
        public static final int CMD_RESTART = 1004;
        public static final int CMD_POWEROFF = 1005;
        public static final int CMD_SLEEP = 1006;
        public static final int CMD_RESUME = 1007;
        public static final int CMD_CAPTUREFINGER = 1009;
        public static final int CMD_TEST_TEMP = 1011;
        public static final int CMD_CAPTUREIMAGE = 1012;
        public static final int CMD_REFRESHDATA = 1013;
        public static final int CMD_REFRESHOPTION = 1014;
        public static final int CMD_TESTVOICE = 1017;
        public static final int CMD_GET_VERSION = 1100;
        public static final int CMD_CHANGE_SPEED = 1101;
        public static final int CMD_AUTH = 1102;
        public static final int CMD_PREPARE_DATA = 1500;
        public static final int CMD_DATA = 1501;
        public static final int CMD_FREE_DATA = 1502;
        public static final int CMD_DATA_WRRQ = 1503;
        public static final int CMD_DATA_RDY = 1504;
        public static final int CMD_DB_RRQ = 7;
        public static final int CMD_USER_WRQ = 8;
        public static final int CMD_USERTEMP_RRQ = 9;
        public static final int CMD_USERTEMP_WRQ = 10;
        public static final int CMD_OPTIONS_RRQ = 11;
        public static final int CMD_OPTIONS_WRQ = 12;
        public static final int CMD_ATTLOG_RRQ = 13;
        public static final int CMD_CLEAR_DATA = 14;
        public static final int CMD_CLEAR_ATTLOG = 15;
        public static final int CMD_DELETE_USER = 18;
        public static final int CMD_DELETE_USERTEMP = 19;
        public static final int CMD_CLEAR_ADMIN = 20;
        public static final int CMD_USERGRP_RRQ = 21;
        public static final int CMD_USERGRP_WRQ = 22;
        public static final int CMD_USERTZ_RRQ = 23;
        public static final int CMD_USERTZ_WRQ = 24;
        public static final int CMD_GRPTZ_RRQ = 25;
        public static final int CMD_GRPTZ_WRQ = 26;
        public static final int CMD_TZ_RRQ = 27;
        public static final int CMD_TZ_WRQ = 28;
        public static final int CMD_ULG_RRQ = 29;
        public static final int CMD_ULG_WRQ = 30;
        public static final int CMD_UNLOCK = 31;
        public static final int CMD_CLEAR_ACC = 32;
        public static final int CMD_CLEAR_OPLOG = 33;
        public static final int CMD_OPLOG_RRQ = 34;
        public static final int CMD_GET_FREE_SIZES = 50;
        public static final int CMD_ENABLE_CLOCK = 57;
        public static final int CMD_STARTVERIFY = 60;
        public static final int CMD_STARTENROLL = 61;
        public static final int CMD_CANCELCAPTURE = 62;
        public static final int CMD_STATE_RRQ = 64;
        public static final int CMD_WRITE_LCD = 66;
        public static final int CMD_CLEAR_LCD = 67;
        public static final int CMD_GET_PINWIDTH = 69;
        public static final int CMD_SMS_WRQ = 70;
        public static final int CMD_SMS_RRQ = 71;
        public static final int CMD_DELETE_SMS = 72;
        public static final int CMD_UDATA_WRQ = 73;
        public static final int CMD_DELETE_UDATA = 74;
        public static final int CMD_DOORSTATE_RRQ = 75;
        public static final int CMD_WRITE_MIFARE = 76;
        public static final int CMD_EMPTY_MIFARE = 78;
        public static final int CMD_VERIFY_WRQ = 79;
        public static final int CMD_VERIFY_RRQ = 80;
        public static final int CMD_TMP_WRITE = 87;
        public static final int CMD_CHECKSUM_BUFFER = 119;
        public static final int CMD_DEL_FPTMP = 134;
        public static final int CMD_GET_TIME = 201;
        public static final int CMD_SET_TIME = 202;
        public static final int CMD_REG_EVENT = 500;
        public static final int CMD_ACK_OK = 2000;
        public static final int CMD_ACK_ERROR = 2001;
        public static final int CMD_ACK_DATA = 2002;
        public static final int CMD_ACK_RETRY = 2003;
        public static final int CMD_ACK_REPEAT = 2004;
        public static final int CMD_ACK_UNAUTH = 2005;
        public static final int CMD_ACK_UNKNOWN = 65535;
        public static final int CMD_ACK_ERROR_CMD = 65533;
        public static final int CMD_ACK_ERROR_INIT = 65532;
        public static final int CMD_ACK_ERROR_DATA = 65531;
        public static final int EF_ATTLOG = 1;
        public static final int EF_FINGER = 2;
        public static final int EF_ENROLLUSER = 4;
        public static final int EF_ENROLLFINGER = 8;
        public static final int EF_BUTTON = 16;
        public static final int EF_UNLOCK = 32;
        public static final int EF_VERIFY = 128;
        public static final int EF_FPFTR = 256;
        public static final int EF_ALARM = 512;
    }

    public static final int USHRT_MAX = 65535;

    public static final int MAX_CHUNK = 65472;

    public static final class REQUEST_DATA {
        public static final ByteBuffer DISABLE_DEVICE = ByteBuffer.wrap(new byte[]{0, 0, 0, 0});
        public static final ByteBuffer GET_REAL_TIME_EVENT = ByteBuffer.wrap(new byte[]{0x01, 0x00, 0x00, 0x00});
        public static final ByteBuffer GET_ATTENDANCE_LOGS = ByteBuffer.wrap(new byte[]{0x01, 0x0d, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        public static final ByteBuffer GET_USERS = ByteBuffer.wrap(new byte[]{0x01, 0x09, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
    }
}

