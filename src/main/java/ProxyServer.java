import model.HttpRequest;
import model.HttpResponse;

import java.io.IOException;

public class ProxyServer implements Proxy {

    @Override
    public HttpResponse toServer(HttpRequest httpRequest) throws IOException {
        System.out.println(httpRequest.getURI());
        return httpRequest.doRequest();
    }

    @Override
    public HttpResponse fromServer(HttpResponse httpResponse) {
        if (httpResponse.getBody() != null) {
            //httpResponse.replaceInBody(...);
        }
        System.out.println(httpResponse.getStatusCode());
        return httpResponse;
    }

}
