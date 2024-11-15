import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        ZKLib zkInstance = new ZKLib("192.168.50.120", 4370, 10000, 4000);

        CompletableFuture.runAsync(() -> {
            try {
                // Intenta conectarte al dispositivo
                zkInstance.createSocket().get();

                System.out.println("Conectado al dispositivo!");

                // Obtén todos los usuarios
                List<User> users = zkInstance.getUsers().get();
                System.out.println("Usuarios: " + users);

                // Desconéctate después de obtener los datos
                zkInstance.disconnect().get();
                System.out.println("Desconectado del dispositivo.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
    }
}

