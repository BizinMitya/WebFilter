package util;

import java.util.regex.Pattern;

public abstract class CheckHost {

    private static final Pattern validIpAddressRegex = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    private static final Pattern validHostnameRegex = Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");

    public static boolean isValidHostname(String hostname) {
        return validHostnameRegex.matcher(hostname).matches();
    }

    public static boolean isValidIpAddress(String ip) {
        return validIpAddressRegex.matcher(ip).matches();
    }

    /**
     * Проверка хоста на валидность
     *
     * @param host имя хоста или IP адрес хоста
     * @return true, если валиден
     */
    public static boolean isValidHost(String host) {
        return isValidHostname(host) || isValidIpAddress(host);
    }

}
