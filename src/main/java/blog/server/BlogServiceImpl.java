package blog.server;

import com.google.protobuf.Empty;
import com.maan.myblog.Blog;
import com.maan.myblog.BlogId;
import com.maan.myblog.BlogServiceGrpc;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoCollection<Document> mongoCollection;

    public BlogServiceImpl(MongoClient mongoClient) {
        MongoDatabase db = mongoClient.getDatabase("blogdb");
        mongoCollection = db.getCollection("blog");
    }

    @Override
    public void createBlog(Blog request, StreamObserver<BlogId> responseObserver) {
        Document doc = new Document("author", request.getAuthor())
                .append("content", request.getContent())
                .append("title", request.getTitle());
        InsertOneResult result;
        try {
            result = mongoCollection.insertOne(doc);
        } catch (MongoException me) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(me.getMessage())
                    .asRuntimeException());
            return;
        }

        if (!result.wasAcknowledged() || result.getInsertedId() == null) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("blog couldn´t be created")
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(BlogId.newBuilder().setId(result.getInsertedId().asObjectId().getValue().toString()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(BlogId blogId, StreamObserver<Blog> responseObserver) {
        if (blogId.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Id no se ha enviado en la peticion")
                    .asRuntimeException());
            return;
        }

        Document result = mongoCollection.find(eq("_id", new ObjectId(blogId.getId()))).first();

        if (result == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog no encontrado")
                    .augmentDescription("BlogId: " + blogId.getId())
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Blog.newBuilder()
                        .setTitle(result.getString("title"))
                        .setAuthor(result.getString("author"))
                        .setContent(result.getString("content"))
                        .setId(blogId.getId())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateBlog(Blog blog, StreamObserver<Empty> responseObserver) {
        if (blog.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Id no se ha enviado en la peticion")
                    .asRuntimeException());
            return;
        }

        Document document = mongoCollection.findOneAndUpdate(
                eq("_id", new ObjectId(blog.getId())),
                combine(
                        set("author", blog.getAuthor()),
                        set("title", blog.getTitle()),
                        set("content", blog.getContent())
                ));

        if (document == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("El blog no fue encontrado")
                    .augmentDescription("Blog id: " + blog.getId())
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void listBlogs(Empty empty, StreamObserver<Blog> responseObserver) {
        for (Document result : mongoCollection.find()) {
            responseObserver.onNext(Blog.newBuilder()
                    .setTitle(result.getString("title"))
                    .setAuthor(result.getString("author"))
                    .setContent(result.getString("content"))
                    .setId(result.getObjectId("_id").toString())
                    .build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(BlogId blogId, StreamObserver<Empty> responseObserver) {
        if (blogId.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Id no se ha enviado en la peticion")
                    .asRuntimeException());
            return;
        }

        DeleteResult deleteResult;
        try {
            deleteResult = mongoCollection.deleteOne(eq("_id", new ObjectId(blogId.getId())));
        } catch (MongoException me) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("El blog no pudo ser borrado")
                    .asRuntimeException());
            return;
        }

        if (!deleteResult.wasAcknowledged()) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("El blog no pudo ser borrado")
                    .asRuntimeException());
            return;
        }

        if (deleteResult.getDeletedCount() == 0) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("El blog no se encontro")
                    .augmentDescription("blogid: "+ blogId.getId())
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
