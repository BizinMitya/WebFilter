import model.HttpRequest;
import model.HttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface Proxy {

    HttpResponse toServer(HttpRequest httpRequest) throws IOException;

    HttpResponse fromServer(HttpResponse httpResponse) throws UnsupportedEncodingException;
}
