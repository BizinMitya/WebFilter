package proxy;

import proxy.model.HttpRequest;
import proxy.model.HttpResponse;

import java.io.IOException;

public class ProxyHandler {

    public HttpResponse toServer(HttpRequest httpRequest) throws IOException {
        System.out.println(httpRequest.getMethod() + " " + httpRequest.getURI());
        return httpRequest.doRequest();
    }

    public HttpResponse fromServer(HttpResponse httpResponse) {
        if (httpResponse.getBody() != null) {
            //httpResponse.replaceInBody(...);
        }
        System.out.println(httpResponse.getStatusCode());
        return httpResponse;
    }

}
