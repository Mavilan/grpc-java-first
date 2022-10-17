package calculus.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class CalculatorServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50052;

        Server server = ServerBuilder
                .forPort(port)
                .addService(new CalculatorServiceImpl())
                .addService(ProtoReflectionService.newInstance())
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
