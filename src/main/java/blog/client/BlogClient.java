package blog.client;

import com.google.protobuf.Empty;
import com.maan.myblog.Blog;
import com.maan.myblog.BlogId;
import com.maan.myblog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("[CLIE] Creando el canal...");
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        run(channel);

        System.out.println("[CLIE] Cerrando canal...");
        channel.shutdown();
    }

    private static void run(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub blockingStub = BlogServiceGrpc.newBlockingStub(channel);

        BlogId blogId = createblog(blockingStub);
        if (blogId == null) {
            return;
        }
        readBlog(blockingStub, blogId);
        updateBlog(blockingStub, blogId);

        listBlogs(blockingStub);

        deleteBlog(blockingStub, blogId);
    }

    private static BlogId createblog(BlogServiceGrpc.BlogServiceBlockingStub blockingStub) {
        try {
            BlogId blogId = blockingStub.createBlog(Blog.newBuilder()
                            .setAuthor("Marco Avila")
                            .setContent("Este es el Hello World del blog")
                            .setTitle("Nuevo blog")
                    .build());
            System.out.println("Blog creado: ".concat(blogId.getId()));
            return blogId;
        } catch (RuntimeException re) {
            System.out.println("No se pudo crear el blog: ".concat(re.getLocalizedMessage()));
            return null;
        }
    }

    private static void readBlog(BlogServiceGrpc.BlogServiceBlockingStub blockingStub, BlogId blogId) {
        try {
            Blog readResponse = blockingStub.readBlog(blogId);
            System.out.println("Blog obtenido: ".concat(readResponse.toString()));
        } catch (RuntimeException re) {
            System.out.println("No se pudo leer: " + re.getMessage());
        }
    }

    private static void updateBlog(BlogServiceGrpc.BlogServiceBlockingStub blockingStub, BlogId blogId) {
        try {
            blockingStub.updateBlog(Blog.newBuilder()
                    .setId(blogId.getId())
                    .setContent("nuevo contenido")
                    .setAuthor("nuevo autor")
                    .setTitle("nuevo titulo")
                    .build());
            System.out.println("Blog actualizado: ".concat(blogId.getId()));
        } catch (RuntimeException re) {
            System.out.println("No se pudo actualizar el blog: " + re.getMessage());
        }
    }

    private static void listBlogs(BlogServiceGrpc.BlogServiceBlockingStub blockingStub) {
        blockingStub.listBlogs(Empty.getDefaultInstance()).forEachRemaining(System.out::println);
    }

    private static void deleteBlog(BlogServiceGrpc.BlogServiceBlockingStub blockingStub, BlogId blogId) {
        try {
            blockingStub.deleteBlog(blogId);
            System.out.println("Blog borrado: " + blogId);
        } catch (RuntimeException re) {
            System.out.println("No se pudo borrar el blog: " +re.getMessage());
        }
    }
}
