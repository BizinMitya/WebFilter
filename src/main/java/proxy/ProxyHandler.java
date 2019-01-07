package proxy;

import model.Host;
import model.WebRequest;
import model.WebResponse;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import util.HostUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dao.BlacklistDAO.isHostInBlacklist;

public abstract class ProxyHandler {

    private static final Logger LOGGER = Logger.getLogger(ProxyHandler.class);
    private static final double EPS = 1e-6;

    public static WebResponse doHttpRequestToServer(WebRequest webRequest) throws IOException {
        LOGGER.info(webRequest.getMethod() + " " + webRequest.getHost());
        Host host = HostUtil.createHostFromHostOrIp(webRequest.getHost());
        if (isHostInBlacklist(host)) {
            return WebResponse.hostInBlacklistResponse();
        } else {
            return webRequest.doHttpRequest();
        }
    }

    @Nullable
    public static WebResponse doHttpsRequestToServer(WebRequest webRequest) throws IOException {
        LOGGER.info(webRequest.getMethod() + " " + webRequest.getHost());
        Host host = HostUtil.createHostFromHostOrIp(webRequest.getHost());
        if (isHostInBlacklist(host)) {
            return WebResponse.hostInBlacklistResponse();
        } else {
            return webRequest.doHttpsRequest();
        }
    }

    public static WebResponse fromServer(WebResponse webResponse) {
        if (webResponse.getBody() != null && webResponse.isHtml()) {
            try {
                Map<String, Double> categoryProbabilityMap = webResponse.classifyContent();
                if (!allEqual(new ArrayList<>(categoryProbabilityMap.values()))) {
                    webResponse.createProbabilitiesPage(categoryProbabilityMap);
                }
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.trace(webResponse.getStatusCode());
        return webResponse;
    }

    private static boolean allEqual(List<Double> probabilities) {
        if (!probabilities.isEmpty()) {
            Double value = probabilities.get(0);
            for (int i = 1; i < probabilities.size(); i++) {
                if (Math.abs(probabilities.get(i) - value) >= EPS) {
                    return false;
                }
                value = probabilities.get(i);
            }
            return true;
        } else {
            return true;
        }
    }

}
