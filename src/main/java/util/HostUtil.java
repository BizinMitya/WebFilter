package util;

import model.Host;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public abstract class HostUtil {

    private static final Pattern validIpAddressRegex = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    private static final Pattern validHostnameRegex = Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");

    private static boolean isValidHostname(String hostname) {
        return validHostnameRegex.matcher(hostname).matches();
    }

    private static boolean isValidIpAddress(String ip) {
        return validIpAddressRegex.matcher(ip).matches();
    }

    /**
     * Метод для создания объекта {@link Host}
     *
     * @param hostOrIp хост или IP адрес хоста
     * @return объект {@link Host}
     */
    public static Host createHostFromHostOrIp(String hostOrIp) throws UnknownHostException {
        if (hostOrIp == null) {
            throw new UnknownHostException();
        }
        if (isValidIpAddress(hostOrIp)) {
            InetAddress hostInetAddress = InetAddress.getByName(hostOrIp);
            return new Host(hostInetAddress.getHostAddress(),
                    hostInetAddress.getHostAddress().equals(hostInetAddress.getHostName()) ? "" : hostInetAddress.getHostName());
        } else if (isValidHostname(hostOrIp)) {
            InetAddress hostInetAddress = InetAddress.getByName(hostOrIp);
            return new Host(hostInetAddress.getHostAddress(), hostInetAddress.getHostName());
        }
        throw new UnknownHostException();
    }

}
