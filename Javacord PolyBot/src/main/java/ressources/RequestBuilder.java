package ressources;

import org.asynchttpclient.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RequestBuilder {
    private final AsyncHttpClient client;
    private final org.asynchttpclient.RequestBuilder request;

    public RequestBuilder() {
        client = Dsl.asyncHttpClient();
        request = new org.asynchttpclient.RequestBuilder();
    }

    public static RequestBuilder create() {
        return new RequestBuilder();
    }

    public RequestBuilder setMethod(String method) {
        request.setMethod(method);
        return this;
    }

    public RequestBuilder setUrl(String url) {
        request.setUrl(url);
        return this;
    }

    public RequestBuilder addHeader(String key, String val) {
        request.addHeader(key, val);
        return this;
    }

    public Response execute() {
        try {
            Response response = client.executeRequest(request.build()).get();
            client.close();
            return response;
        } catch (InterruptedException | IOException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
