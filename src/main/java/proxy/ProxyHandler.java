package proxy;

import classificators.Category;
import model.Host;
import model.WebRequest;
import model.WebResponse;
import org.apache.log4j.Logger;
import util.HostUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static dao.BlacklistDAO.isHostInBlacklist;

public abstract class ProxyHandler {

    private static final Logger LOGGER = Logger.getLogger(ProxyHandler.class);

    public static WebResponse doHttpRequestToServer(WebRequest webRequest) throws IOException {
        Host host = HostUtil.createHostFromHostOrIp(webRequest.getHost());
        if (isHostInBlacklist(host)) {
            LOGGER.trace(webRequest.getMethod() + " " + webRequest.getURI());
            return WebResponse.hostInBlacklistResponse();
        } else {
            return webRequest.doHttpRequest();
        }
    }

    public static WebResponse doHttpsRequestToServer(WebRequest webRequest) throws IOException {
        Host host = HostUtil.createHostFromHostOrIp(webRequest.getHost());
        if (isHostInBlacklist(host)) {
            LOGGER.trace(webRequest.getMethod() + " " + webRequest.getURI());
            return WebResponse.hostInBlacklistResponse();
        } else {
            return webRequest.doHttpsRequest();
        }
    }

    public static WebResponse fromServer(WebResponse webResponse) {
        if (webResponse.getBody() != null && webResponse.isHtml()) {
            try {
                Map<Category, Double> categoryProbabilityMap = webResponse.classifyContent();
                webResponse.createCategoriesInfoScript(categoryProbabilityMap);
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.trace(webResponse.getStatusCode());
        return webResponse;
    }

}
