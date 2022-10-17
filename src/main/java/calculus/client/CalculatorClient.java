package calculus.client;

import com.maan.calculus.AverageResponse;
import com.maan.calculus.CalculatorServiceGrpc;
import com.maan.calculus.NumberMessage;
import com.maan.calculus.SqrtResponse;
import com.maan.calculus.SumMessage;
import com.maan.calculus.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("[CLIE] Se necesitan argumentos para funcionar...");
            return;
        }

        System.out.println("[CLIE] Creando el canal...");
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        switch (args[0]) {
            case "suma": doSuma(channel); break;
            case "primes": doPrimes(channel); break;
            case "average": doAverage(channel); break;
            case "max": doMax(channel); break;
            case "sqrt": doSqrt(channel); break;
            default:
                System.out.println("[CLIE] No es una opcion valida: " + args[0]);
        }

        System.out.println("[CLIE] Cerrando canal...");
        channel.shutdown();
    }

    private static void doSuma(ManagedChannel channel) {
        System.out.println("[CLIE] Ejecutando la accion de suma en el cliente...");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
        SumResponse sumResponse = stub.sum(SumMessage.newBuilder()
                        .setFirstNumber(10).setSecondNumber(29).build());

        System.out.println("[CLIE] Resultado en el cliente obtenida, la asuma es: " + sumResponse.getSum());
    }

    private static void doPrimes(ManagedChannel channel) {
        System.out.println("[CLIE] Ejecutando la accion de descomponer numero en primos...");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
        stub.primes(NumberMessage.newBuilder().setNumber(132).build()).forEachRemaining(response -> {
            System.out.println("prime: " + response.getPrime());
        });
    }

    private static void doAverage(ManagedChannel channel) throws InterruptedException {
        System.out.println("[CLIE] Ejecutando la accion de promedio de numeros...");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
        StreamObserver<NumberMessage> requestObserver = stub.average(new StreamObserver<AverageResponse>() {
            @Override
            public void onNext(AverageResponse averageResponse) {
                System.out.println("[CLIE] Respuesta del stream: ".concat(String.valueOf(averageResponse.getAverage())));
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).forEach(number ->
                requestObserver.onNext(NumberMessage.newBuilder().setNumber(number).build())
                );

        requestObserver.onCompleted();
        countDownLatch.await(3, TimeUnit.SECONDS);
    }

    private static void doMax(ManagedChannel channel) throws InterruptedException {
        System.out.println("[CLIE] Ejecutando la accion del maximo de numeros...");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
        StreamObserver<NumberMessage> requestObserver = stub.max(new StreamObserver<NumberMessage>() {
            @Override
            public void onNext(NumberMessage numberMessage) {
                System.out.println("[CLIE] Response del stream: ".concat(String.valueOf(numberMessage.getNumber())));
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        Arrays.asList(1, 5, 3, 6, 2, 20, 15, 80, 60, 100).forEach(number ->
                requestObserver.onNext(NumberMessage.newBuilder().setNumber(number).build()));

        requestObserver.onCompleted();
        countDownLatch.await(3, TimeUnit.SECONDS);
    }

    private static void doSqrt(ManagedChannel channel) {
        System.out.println("[CLIE] Ejecutando la accion del doSqrt...");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
        SqrtResponse sqrtResponse = stub.sqrt(NumberMessage.newBuilder().setNumber(234543653).build());

        System.out.println("[CLIE] Response del sqrt: ".concat(String.valueOf(sqrtResponse.getSqrt())));

        try {
            stub.sqrt(NumberMessage.newBuilder().setNumber(-2342).build());
        } catch (RuntimeException re) {
            System.out.println("[CLIE] Get an ex: " + re.getMessage());
        }
    }
}
