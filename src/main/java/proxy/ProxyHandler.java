package proxy;

import classificators.Category;
import org.apache.log4j.Logger;
import proxy.model.Host;
import proxy.model.HttpRequest;
import proxy.model.HttpResponse;
import util.HostUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Map;

import static dao.BlacklistDAO.isHostInBlacklist;

public class ProxyHandler {

    private static final Logger LOGGER = Logger.getLogger(ProxyHandler.class);

    public HttpResponse toServer(HttpRequest httpRequest) throws IOException {
        try {
            Host host = HostUtil.createHostFromHostOrIp(httpRequest.getHost());
            if (isHostInBlacklist(host)) {
                LOGGER.trace(httpRequest.getMethod() + " " + httpRequest.getURI());
                return HttpResponse.hostInBlacklistResponse();
            } else {
                return httpRequest.doRequest();
            }
        } catch (UnknownHostException e) {
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
