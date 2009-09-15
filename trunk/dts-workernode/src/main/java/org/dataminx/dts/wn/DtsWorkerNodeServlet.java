package org.dataminx.dts.wn;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Servlet that will start the DtsWorkerNode service.
 *
 * @author Gerson Galang
 */
public class DtsWorkerNodeServlet extends HttpServlet {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DtsWorkerNodeServlet.class);

    /**
     * {@inheritDoc}
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        super.doGet(request, response);
    }

    /**
     * {@inheritDoc}
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        super.doPost(request, response);
    }

    /**
     * {@inheritDoc}
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        String dataminxDirectory = servletConfig.getInitParameter("dataminxDirectory");

        if (true) {
            new DtsWorkerNodeCommandLineRunner(dataminxDirectory).run();
        }
    }
}
