package zklibjs.java;

import java.net.DatagramChannel;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ZKLibUDP {
    private String ip;
    private int port;
    private int timeout;
    private DatagramChannel socket;
    private int sessionId;
    private int replyId;
    private int inport;

    public ZKLibUDP(String ip, int port, int timeout, int inport) {
        this.ip = ip;
        this.port = port;
        this.timeout = timeout;
        this.inport = inport;
    }

    public CompletableFuture<DatagramChannel> createSocket(Consumer<Throwable> cbError, Runnable cbClose) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                socket = DatagramChannel.open();
                socket.bind(new InetSocketAddress(inport));
                socket.configureBlocking(false);
                return socket;
            } catch (Throwable err) {
                cbError.accept(err);
                throw new RuntimeException(err);
            }
        });
    }

    public CompletableFuture<Boolean> connect() {
        return executeCmd(COMMANDS.CMD_CONNECT, "").thenApply(reply -> true)
                .exceptionally(err -> {
                    throw new RuntimeException("NO_REPLY_ON_CMD_CONNECT", err);
                });
    }

    public CompletableFuture<Boolean> closeSocket() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                socket.close();
                return true;
            } catch (Throwable err) {
                throw new RuntimeException(err);
            }
        });
    }

    public CompletableFuture<ByteBuffer> writeMessage(ByteBuffer msg, boolean connect) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ByteBuffer response = ByteBuffer.allocate(1024);
                socket.send(msg, new InetSocketAddress(ip, port));
                socket.receive(response);
                return response;
            } catch (Throwable err) {
                throw new RuntimeException(err);
            }
        });
    }

    public CompletableFuture<ByteBuffer> requestData(ByteBuffer msg) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ByteBuffer response = ByteBuffer.allocate(1024);
                socket.send(msg, new InetSocketAddress(ip, port));
                socket.receive(response);
                return response;
            } catch (Throwable err) {
                throw new RuntimeException(err);
            }
        });
    }

    public CompletableFuture<ByteBuffer> executeCmd(int command, ByteBuffer data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (command == COMMANDS.CMD_CONNECT) {
                    sessionId = 0;
                    replyId = 0;
                } else {
                    replyId++;
                }

                ByteBuffer buf = createUDPHeader(command, sessionId, replyId, data);
                ByteBuffer reply = writeMessage(buf, command == COMMANDS.CMD_CONNECT || command == COMMANDS.CMD_EXIT).get();

                if (reply != null && reply.remaining() >= 0) {
                    if (command == COMMANDS.CMD_CONNECT) {
                        sessionId = reply.getShort(4);
                    }
                }
                return reply;
            } catch (Throwable err) {
                throw new RuntimeException(err);
            }
        });
    }

    public void sendChunkRequest(int start, int size) {
        replyId++;
        ByteBuffer reqData = ByteBuffer.allocate(8);
        reqData.putInt(start);
        reqData.putInt(size);
        ByteBuffer buf = createUDPHeader(COMMANDS.CMD_DATA_RDY, sessionId, replyId, reqData);

        try {
            socket.send(buf, new InetSocketAddress(ip, port));
        } catch (Throwable err) {
            log("[UDP][SEND_CHUNK_REQUEST]" + err.toString());
        }
    }

    public CompletableFuture<DataResponse> readWithBuffer(ByteBuffer reqData, Consumer<Integer> cb) {
        return CompletableFuture.supplyAsync(() -> {
            replyId++;
            ByteBuffer buf = createUDPHeader(COMMANDS.CMD_DATA_WRRQ, sessionId, replyId, reqData);
            ByteBuffer reply = requestData(buf).get();

            Header header = decodeUDPHeader(reply.array(), 0);

            switch (header.commandId) {
                case COMMANDS.CMD_DATA:
                    return new DataResponse(reply.array(), 8, null);
                case COMMANDS.CMD_ACK_OK:
                case COMMANDS.CMD_PREPARE_DATA:
                    // Handle data preparation
                    break;
                default:
                    throw new RuntimeException("ERROR_IN_UNHANDLE_CMD " + exportErrorMessage(header.commandId));
            }
            return null;
        });
    }

    public CompletableFuture<UserResponse> getUsers() {
        if (socket != null) {
            try {
                freeData().get();
            } catch (Throwable err) {
                return CompletableFuture.failedFuture(err);
            }
        }

        return readWithBuffer(REQUEST_DATA.GET_USERS).thenApply(data -> {
            if (socket != null) {
                try {
                    freeData().get();
                } catch (Throwable err) {
                    return CompletableFuture.failedFuture(err);
                }
            }

            int USER_PACKET_SIZE = 28;
            ByteBuffer userData = ByteBuffer.wrap(data.data).position(4);
            List<User> users = new ArrayList<>();

            while (userData.remaining() >= USER_PACKET_SIZE) {
                User user = decodeUserData28(userData);
                users.add(user);
            }

            return new UserResponse(users, data.err);
        });
    }

    public CompletableFuture<AttendanceResponse> getAttendances(Consumer<Integer> callbackInProcess) {
        if (socket != null) {
            try {
                freeData().get();
            } catch (Throwable err) {
                return CompletableFuture.failedFuture(err);
            }
        }

        return readWithBuffer(REQUEST_DATA.GET_ATTENDANCE_LOGS, callbackInProcess).thenApply(data -> {
            if (socket != null) {
                try {
                    freeData().get();
                } catch (Throwable err) {
                    return CompletableFuture.failedFuture(err);
                }
            }

            int RECORD_PACKET_SIZE = data.mode ? 8 : 16;
            ByteBuffer recordData = ByteBuffer.wrap(data.data).position(4);
            List<Record> records = new ArrayList<>();

            while (recordData.remaining() >= RECORD_PACKET_SIZE) {
                Record record = decodeRecordData16(recordData);
                records.add(record);
            }

            return new AttendanceResponse(records, data.err);
        });
    }

    public CompletableFuture<Void> freeData() {
        return executeCmd(COMMANDS.CMD_FREE_DATA, ByteBuffer.allocate(0));
    }

    public CompletableFuture<Long> getTime() {
        ByteBuffer time = executeCmd(COMMANDS.CMD_GET_TIME, ByteBuffer.allocate(0)).get();
        return CompletableFuture.completedFuture(timeParser.decode(time.getInt(8)));
    }

    public CompletableFuture<InfoResponse> getInfo() {
        ByteBuffer data = executeCmd(COMMANDS.CMD_GET_FREE_SIZES, ByteBuffer.allocate(0)).get();
        return CompletableFuture.completedFuture(new InfoResponse(data.getInt(24), data.getInt(40), data.getInt(72)));
    }

    public CompletableFuture<Void> clearAttendanceLog() {
        return executeCmd(COMMANDS.CMD_CLEAR_ATTLOG, ByteBuffer.allocate(0));
    }

    public CompletableFuture<Void> disableDevice() {
        return executeCmd(COMMANDS.CMD_DISABLEDEVICE, REQUEST_DATA.DISABLE_DEVICE);
    }

    public CompletableFuture<Void> enableDevice() {
        return executeCmd(COMMANDS.CMD_ENABLEDEVICE, ByteBuffer.allocate(0));
    }

    public CompletableFuture<Void> disconnect() {
        return executeCmd(COMMANDS.CMD_EXIT, ByteBuffer.allocate(0)).thenCompose(aVoid -> closeSocket());
    }

    public void getRealTimeLogs(Consumer<Record> cb) {
        replyId++;
        ByteBuffer buf = createUDPHeader(COMMANDS.CMD_REG_EVENT, sessionId, replyId, REQUEST_DATA.GET_REAL_TIME_EVENT);

        try {
            socket.send(buf, new InetSocketAddress(ip, port));
        } catch (Throwable err) {
            // Handle error
        }

        if (socket.socket().getChannel().isOpen()) {
            socket.socket().getChannel().onMessage(data -> {
                if (!checkNotEventUDP(data)) return;
                if (data.remaining() == 18) {
                    cb.accept(decodeRecordRealTimeLog18(data));
                }
            });
        }
    }
}

