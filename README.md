# Proyecto gRPC para java

Este proyecto esta enfocado a poder conocer como funciona la tecnologia de grpc basado en el lenguaje java.

---
## Estructura

Existen 3 servicios en donde podemos ver diferetes implementaciones de las funcionalidades de grpc

- Greeting
- Calculator
- Blog

---
## Piezas

### Greeting service

Este servicio se dedica a enviar un nombre y recibir un saludo desde la implementacion de los rpc siguientes:

- **greet**: que envía un *GreetingMessage* y regresa un *GreetingResponse*.
- **greetManyTimes**: que envía un *GreetingMessage* y regresa un stream de *GreetingResponse*.
- **longGreet**: que envía un stream de *GreetingMessage* y retorna un *GreetingResponse*.
- **greetingEveryone**: que envía un stream de *GreetingMessage* y retorna un stream *GreetingResponse*.
- **greetWithDeadLine**: que enví un *GreetingMessage* y retorne un *GreetingResponse* dependiendo de los tiempos de ejecucion o retornara un error.

### Calculus service

Este servicio se dedica a recibir numeros y ejecutar operaciones matematicas desde la implementación de los rpc siguientes:

- **sum**: envía un *SumMessage* y regresa un *SumResponse*.
- **primes**: envía un *NumberMessage* y regresa un stream de *PrimeResponse*.
- **average**: envía un stream de *NumberMessage* y regresa un *AverageResponse*.
- **max**: envía un stream de *NumberMessage* y regresa un stream de *NumberMessage*.
- **sqrt**: envía un *NumberMessage* y regresa un *SqrtResponse* si es positivo y un Status.INVALID_ARGUMENT si el numero es negativo. 

Ademas este servicio puede ser leido por el paquete de evan, es decir desde el cli de grpc se puede obtener la estructura del servicio.

### Blog Service

Este servicio que implementa a un blog permite hacer uso de un CRUD para las entradas a un blog. 
Todo esta gestionado desde una base **MongoDB** con la implementacion de los rpc siguientes:

- **createBlog**: envía un *Blog* y regresa un *BlogId*.
- **readBlog**: envía un *BlogId* y regresa un *Blog*.
- **updateBlog**: envía un *Blog* y regresa el *Empty* de google.
- **deleteBlog**: envía un *BlogId* y regresa el *Empty* de google.
- **listBlogs**: envía el *Empty* de google y regresa un stream de *Blog*.

Se tiene una configuracion de intellij para obtener una imagen docker de mongodb y levantarla, todo basado en el docker compose que se encuentra en la carpeta de yml.

---
## Uso del servicio

### Requisitos técnicos

Para poder desplegar el componente hace falta lo siguientes requerimientos:

- Tener instalado java 11.
- Tener conocimiento de docker.
- Tener conocimiento de MongoDB.
- Tener instalado el evan-cli para grpc.

### Despliegue

Utilizar la configuracion creada en intellij para el despliegue de los servicios.
