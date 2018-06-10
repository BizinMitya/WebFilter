package proxy;

import org.apache.log4j.Logger;
import proxy.model.HttpRequest;
import proxy.model.HttpResponse;

import java.io.IOException;

public class ProxyHandler {

    private static final Logger LOGGER = Logger.getLogger(ProxyHandler.class);

    public HttpResponse toServer(HttpRequest httpRequest) throws IOException {
        LOGGER.trace(httpRequest.getMethod() + " " + httpRequest.getURI());
        return httpRequest.doRequest();
    }

    public HttpResponse fromServer(HttpResponse httpResponse) {
        if (httpResponse.getBody() != null) {
            //httpResponse.replaceInBody(...);
        }
        LOGGER.trace(httpResponse.getStatusCode());
        return httpResponse;
    }

}
