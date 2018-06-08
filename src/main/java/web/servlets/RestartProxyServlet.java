package web.servlets;

import proxy.Proxy;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestartProxyServlet extends HttpServlet {

    private Proxy proxy = Proxy.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        proxy.restart();
    }

}
