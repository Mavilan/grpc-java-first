package greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;

        Server server = ServerBuilder
                .forPort(port)
                .addService(new GreetingServerImpl())
                .build();
        server.start();
        System.out.println("[SERV] Servidor inicializado...");
        System.out.println("[SERV] Escuchando en el puerto: " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SERV] Recepcion de orden de apagado... Amonos");
            server.shutdown();
            System.out.println("[SERV] Servidor apagado... Hasta lueeeeeeggggoooo...");
        }));

        server.awaitTermination();
    }
}
