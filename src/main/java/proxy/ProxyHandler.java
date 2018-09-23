package proxy;

import classificators.Category;
import org.apache.log4j.Logger;
import proxy.model.HttpRequest;
import proxy.model.HttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Map;

import static dao.BlacklistDAO.isHostInBlacklist;
import static util.CheckHost.isValidHost;

public class ProxyHandler {

    private static final Logger LOGGER = Logger.getLogger(ProxyHandler.class);

    public HttpResponse toServer(HttpRequest httpRequest) throws IOException {
        String host = httpRequest.getHost();
        if (isValidHost(host)) {
            InetAddress hostInetAddress = InetAddress.getByName(host);
            if (isHostInBlacklist(hostInetAddress.getHostAddress()) ||
                    isHostInBlacklist(hostInetAddress.getHostName())) {
                LOGGER.trace(httpRequest.getMethod() + " " + httpRequest.getURI());
                return HttpResponse.hostInBlacklistResponse();
            } else {
                return httpRequest.doRequest();
            }
        } else {
            return httpRequest.doRequest();
        }
    }

    public HttpResponse fromServer(HttpResponse httpResponse) {
        if (httpResponse.getBody() != null && httpResponse.isHtml()) {
            try {
                Map<Category, Double> categoryProbabilityMap = httpResponse.classifyContent();
                httpResponse.createCategoriesInfoScript(categoryProbabilityMap);
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.trace(httpResponse.getStatusCode());
        return httpResponse;
    }

}
