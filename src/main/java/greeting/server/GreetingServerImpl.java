package greeting.server;

import com.maan.greeting.GreetingMessage;
import com.maan.greeting.GreetingResponse;
import com.maan.greeting.GreetingServiceGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class GreetingServerImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

    @Override
    public void greet(GreetingMessage request, StreamObserver<GreetingResponse> response) {
        System.out.println("[IMPL] Iniciando el metodo greet en la impl del servicio...");
        response.onNext(GreetingResponse.newBuilder()
                .setResult("Response of ".concat(request.getFirstName())).build());

        System.out.println("[IMPL] Se terminaron las acciones del metodo...");
        response.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetingMessage request, StreamObserver<GreetingResponse> responseObserver) {
        System.out.println("[IMPL] Iniciando el metodo greetManyTimes en la impl del servicio...");
        GreetingResponse response = GreetingResponse.newBuilder()
                .setResult("Response of ".concat(request.getFirstName())).build();

        for (int i = 0; i < 10; i++) responseObserver.onNext(response);

        System.out.println("[IMPL] Se terminaron las acciones del metodo...");
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<GreetingMessage> longGreet(StreamObserver<GreetingResponse> response) {
        System.out.println("[IMPL] Iniciando el metodo longGreet en la impl del servicio...");
        StringBuilder builder = new StringBuilder();

        return new StreamObserver<GreetingMessage>() {
            @Override
            public void onNext(GreetingMessage request) {
                builder.append("Hello ");
                builder.append(request.getFirstName());
                builder.append("!, ");
                System.out.println("[IMPL] Acciones del onNext del streamRequest...");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("[IMPL] En caso de error del streamRequest...");
                response.onError(throwable);
            }

            @Override
            public void onCompleted() {
                System.out.println("[IMPL] Acciones del onComplete en el streamRequest");
                response.onNext(GreetingResponse.newBuilder().setResult(builder.toString()).build());
                response.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GreetingMessage> greetingEveryone(StreamObserver<GreetingResponse> response) {
        return new StreamObserver<GreetingMessage>() {
            @Override
            public void onNext(GreetingMessage request) {
                System.out.println("[IMPL] Acciones del onNext del stream...");
                response.onNext(GreetingResponse.newBuilder().setResult("Hello tu " + request.getFirstName()).build());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("[IMPL] Acciones del onError del stream...");
                response.onError(throwable);
            }

            @Override
            public void onCompleted() {
                System.out.println("[IMPL] Acciones del onCompleted del stream...");
                response.onCompleted();
            }
        };
    }

    @Override
    public void greetWithDeadLine(GreetingMessage request, StreamObserver<GreetingResponse> response) {
        Context context = Context.current();
        try {
            for (int i = 0; i < 3; i++) {
                if (context.isCancelled()) return;
                Thread.sleep(100);
            }

            response.onNext(GreetingResponse.newBuilder().setResult("Respuesta: ".concat(request.getFirstName())).build());
            response.onCompleted();
        } catch (InterruptedException ie) {
            response.onError(ie);
        }
    }
}
