package greeting.client;

import com.maan.greeting.GreetingMessage;
import com.maan.greeting.GreetingResponse;
import com.maan.greeting.GreetingServiceGrpc;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("[CLIE] Se necesitan argumentos para funcionar...");
            return;
        }

        System.out.println("[CLIE] Creando el canal...");
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        switch (args[0]) {
            case "greet": doGreet(channel); break;
            case "greet_many_times": doGreetManyTimes(channel); break;
            case "long_greet": doLongGreet(channel); break;
            case "greeting_everyone": doGreetEveryone(channel); break;
            case "greet_with_deadline": doGreetWithDeadLine(channel); break;
            default:
                System.out.println("[CLIE] No es una opcion valida: " + args[0]);
        }

        System.out.println("[CLIE] Cerrando canal...");
        channel.shutdown();
    }

    private static void doGreet(ManagedChannel channel) {
        System.out.println("[CLIE] Iniciando el doGreet...");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        GreetingResponse response = stub.greet(GreetingMessage.newBuilder().setFirstName("Marco").build());

        System.out.println("[CLIE] Response: ".concat(response.getResult()));
    }

    private static void doGreetManyTimes(ManagedChannel channel) {
        System.out.println("[CLIE] Iniciando el doGreetManyTimes...");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        stub.greetManyTimes(GreetingMessage.newBuilder().setFirstName("Marco").build()).forEachRemaining(greetingResponse -> {
            System.out.println("[CLIE] Iteracion: ".concat(greetingResponse.getResult()));
        });
    }

    public static void doLongGreet(ManagedChannel channel) throws InterruptedException {
        System.out.println("[CLIE] Inciando el doLongGreet...");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);
        StreamObserver<GreetingMessage> requestObserver = stub.longGreet(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(GreetingResponse greetingResponse) {
                System.out.println("[CLIE] Respuesta del stream: ".concat(greetingResponse.getResult()));
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        Arrays.asList("Marco", "Evelyn", "Luna", "Remy").forEach(name ->
                requestObserver.onNext(GreetingMessage.newBuilder().setFirstName(name).build()));

        requestObserver.onCompleted();
        countDownLatch.await(3, TimeUnit.SECONDS);
    }

    public static void doGreetEveryone(ManagedChannel channel) throws InterruptedException {
        System.out.println("[CLIE] Iniciando el doGreetEveryone...");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);
        StreamObserver<GreetingMessage> requestObserver = stub.greetingEveryone(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(GreetingResponse greetingResponse) {
                System.out.println("[CLIE] Respuest del stream: ".concat(greetingResponse.getResult()));
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        Arrays.asList("Marco", "Evelyn", "Luna", "Remy").forEach(name ->
                requestObserver.onNext(GreetingMessage.newBuilder().setFirstName(name).build()));

        requestObserver.onCompleted();
        countDownLatch.await(3, TimeUnit.SECONDS);
    }

    protected static void doGreetWithDeadLine(ManagedChannel channel) {
        System.out.println("[CLIE] Iniciando el doGreetWithDeadLine...");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);

        GreetingResponse response = stub.withDeadline(Deadline.after(3, TimeUnit.SECONDS))
                        .greetWithDeadLine(GreetingMessage.newBuilder().setFirstName("Marco").build());
        System.out.println("greeting con Deadline: "+ response.getResult());

        try {
            response = stub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadLine(GreetingMessage.newBuilder().setFirstName("Marco").build());
        } catch (StatusRuntimeException sre) {
            if (sre.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("DeadLine has been exceeded");
            } else {
                System.out.println(" Se obtuvo una exce: " + sre);
            }
        }
    }
}
