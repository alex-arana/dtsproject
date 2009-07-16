/**
 *
 */
package org.dataminx.dts.main;

import javax.servlet.ServletContext;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * The Jetty Embedded DTS Web Service Runner.
 *
 * @author Gerson Galang
 */
public class JettyEmbeddedDtsWebService {

    /**
     * Runs the Jetty Embedded DTS Web Service.
     *
     * @throws Exception if the Server fails to start
     */
    public void run() throws Exception {
        AbstractApplicationContext ctx =
            new ClassPathXmlApplicationContext("/embedded-jetty-context.xml");
        ctx.registerShutdownHook();

        Server server = (Server) ctx.getBean("jettyServer");

        ServletContext servletContext = null;

        for (Handler handler : server.getHandlers()) {
            if (handler instanceof Context) {
                Context context = (Context) handler;

                servletContext = context.getServletContext();
            }
        }

        XmlWebApplicationContext wctx = new XmlWebApplicationContext();
        wctx.setParent(ctx);
        wctx.setConfigLocation("");
        wctx.setServletContext(servletContext);
        wctx.refresh();

        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wctx);

        server.start();
    }

    /**
     * The Jetty Embedded DTS Web Service command-line runner.
     *
     * @param args the arguments
     * @throws Exception if the Jetty server fails to start
     */
    public static void main(String[] args) throws Exception {
        new JettyEmbeddedDtsWebService().run();
    }

}
