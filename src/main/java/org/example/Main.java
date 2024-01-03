package org.example;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
public class Main {
    public static void main(String[] args) throws Exception{
        Server server = new Server(8084);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(ApiServlet.class, "/api/*");

        server.start();
        System.out.println("Server started!");
        server.join();
    }
}