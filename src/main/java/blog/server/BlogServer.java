package blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BlogServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50053;

        MongoClient mongoClient = MongoClients.create("mongodb://root:root@localhost:27017/");

        Server server = ServerBuilder
                .forPort(port)
                .addService(new BlogServiceImpl(mongoClient))
                .build();
        server.start();
        System.out.println("[SERV] Servidor inicializado...");
        System.out.println("[SERV] Escuchando en el puerto: " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SERV] Recepcion de orden de apagado... Amonos");
            server.shutdown();
            mongoClient.close();
            System.out.println("[SERV] Servidor apagado... Hasta lueeeeeeggggoooo...");
        }));

        server.awaitTermination();
    }
}
