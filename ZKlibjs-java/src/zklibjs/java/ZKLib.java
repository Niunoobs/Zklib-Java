package zklibjs.java;


import java.net.Socket;
import java.net.DatagramSocket;
import java.util.concurrent.CompletableFuture;

class ZKLib {
    private String connectionType;
    private ZKLibTCP zklibTcp;
    private ZKLibUDP zklibUdp;
    private Timer timer;
    private boolean isBusy;
    private String ip;

    public ZKLib(String ip, int port, int timeout, int inport) {
        this.connectionType = null;
        this.zklibTcp = new ZKLibTCP(ip, port, timeout);
        this.zklibUdp = new ZKLibUDP(ip, port, timeout, inport);
        this.timer = null;
        this.isBusy = false;
        this.ip = ip;
    }

    private <T> CompletableFuture<T> functionWrapper(Supplier<CompletableFuture<T>> tcpCallback,
                                                     Supplier<CompletableFuture<T>> udpCallback,
                                                     String command) {
        CompletableFuture<T> future = new CompletableFuture<>();

        switch (connectionType) {
            case "tcp":
                if (zklibTcp.getSocket() != null) {
                    tcpCallback.get()
                            .thenAccept(future::complete)
                            .exceptionally(err -> {
                                future.completeExceptionally(new ZKError(err, "[TCP] " + command, ip));
                                return null;
                            });
                } else {
                    future.completeExceptionally(new ZKError(new Exception("Socket isn't connected!"), "[TCP]", ip));
                }
                break;
            case "udp":
                if (zklibUdp.getSocket() != null) {
                    udpCallback.get()
                            .thenAccept(future::complete)
                            .exceptionally(err -> {
                                future.completeExceptionally(new ZKError(err, "[UDP] " + command, ip));
                                return null;
                            });
                } else {
                    future.completeExceptionally(new ZKError(new Exception("Socket isn't connected!"), "[UDP]", ip));
                }
                break;
            default:
                future.completeExceptionally(new ZKError(new Exception("Socket isn't connected!"), "", ip));
        }

        return future;
    }

    public CompletableFuture<Void> createSocket(Consumer<Exception> cbErr, Runnable cbClose) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (zklibTcp.getSocket() == null) {
                    zklibTcp.createSocket(cbErr, cbClose).get();
                    zklibTcp.connect().get();
                    System.out.println("ok tcp");
                }
                connectionType = "tcp";
            } catch (Exception err) {
                try {
                    zklibTcp.disconnect().get();
                } catch (Exception e) {
                    // Ignore
                }

                if (!err.getMessage().equals(ERROR_TYPES.ECONNREFUSED)) {
                    throw new ZKError(err, "TCP CONNECT", ip);
                }

                try {
                    if (zklibUdp.getSocket() == null) {
                        zklibUdp.createSocket(cbErr, cbClose).get();
                        zklibUdp.connect().get();
                    }
                    System.out.println("ok udp");
                    connectionType = "udp";
                } catch (Exception udpErr) {
                    if (!udpErr.getMessage().equals("EADDRINUSE")) {
                        connectionType = null;
                        try {
                            zklibUdp.disconnect().get();
                            zklibUdp.setSocket(null);
                            zklibTcp.setSocket(null);
                        } catch (Exception e) {
                            // Ignore
                        }
                        throw new ZKError(udpErr, "UDP CONNECT", ip);
                    } else {
                        connectionType = "udp";
                    }
                }
            }
        });
    }

    public CompletableFuture<List<User>> getUsers() {
        return functionWrapper(
                () -> zklibTcp.getUsers(),
                () -> zklibUdp.getUsers(),
                "getUsers"
        );
    }
}

