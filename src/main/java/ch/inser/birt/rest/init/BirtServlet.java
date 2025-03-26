/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package ch.inser.birt.rest.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.data.engine.api.DataEngine;
import org.eclipse.birt.report.engine.api.EngineConfig;

import com.lowagie.text.FontFactory;

import ch.inser.birt.core.ChartEngine;
import ch.inser.birt.core.ReportEngine;
import ch.inser.dynamic.common.IContextManager;
import ch.inser.rest.util.ServiceLocator;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Birt Servlet pour applications "rest"
 *
 * @author INSER SA
 *
 */
public class BirtServlet extends HttpServlet {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 4649738733896088332L;

    /**
     * Logger.
     */
    private static final Log logger = LogFactory.getLog(BirtServlet.class);

    @Override
    public void init() throws ServletException {
        // Définition de la propriété système "ch.inser.isejawa.configDir"
        // ---------------------------------------------------------------
        IContextManager ctx = ServiceLocator.getInstance().getContextManager();
        // Si l'initialisation ne s'est pas bien déroulée, alors on baste
        if (!ctx.isApplicationInitOK()) {
            logger.error("Application not started");
            return;
        }
        logger.debug("Initialising Birt ...");

        String birtResourcePath = ctx.getProperty("BIRT_RESOURCE_PATH");
        if (birtResourcePath == null) {
            // On essaye avec la propriété d'inialisation
            birtResourcePath = getInitParameter("BIRT_RESOURCE_PATH");
        }
        if (birtResourcePath == null) {
            // On essaye avec la propriété système
            birtResourcePath = System.getProperty("BIRT_RESOURCE_PATH");
        }

        // Propriété report.dir pour ApplicationReportBean
        String reportDir = ctx.getProperty("report.dir");
        if (reportDir == null) {
            reportDir = getInitParameter("report.dir");
            Properties prop = new Properties();
            prop.setProperty("report.dir", reportDir != null ? reportDir : birtResourcePath);
            ctx.addProperties(prop);
        }
        if (birtResourcePath == null) {
            birtResourcePath = reportDir;

        }
        if (birtResourcePath == null) {
            throw new ServletException("Impossible to get BIRT_RESOURCE PATH");
        }
        logger.debug("BIRT_RESOURCE_PATH : " + birtResourcePath);

        String logLevel = ctx.getProperty("BIRT_LOG_LEVEL");
        if (logLevel == null) {
            logLevel = getInitParameter("BIRT_LOG_LEVEL");
        }

        // Put Birt logs (and all other java.util.logging) into
        // log4j/logback-configured
        // logfiles with SLF4JBridgeHandler (see Log4jServlet init())
        boolean isJULBridge = false;
        String logDirectory;
        try {
            Class<?> cl = Class.forName("org.slf4j.bridge.SLF4JBridgeHandler");
            if (cl != null) {
                isJULBridge = true;
            }
        } catch (ClassNotFoundException e) {
            logger.debug("No bridge installed. OK. " + e.getMessage());
        }

        if (isJULBridge) {
            logDirectory = null;
            logger.info("Logging BIRT with log4j or logback");
        } else {
            logger.info("Logging BIRT with default Birt logging");
            logDirectory = ctx.getProperty("BIRT_LOG_DIR");
            if (logDirectory == null) {
                logDirectory = getInitParameter("BIRT_LOG_DIR");
            }
            if (logDirectory == null) {
                logDirectory = ctx.getProperty("log.dir");
            }
            if (logDirectory == null) {
                logDirectory = "C:\\Temp";
            }
        }
        // Fichiers temporaires de Birt
        String tempDir = ctx.getProperty("report.tempo.dir");
        if (tempDir == null) {
            tempDir = "Temp";
        }

        // Configuration of engine (Birt 4.6.0)
        EngineConfig config460 = new EngineConfig();
        config460.setLogConfig(logDirectory, getLogLevel(logLevel));
        config460.setTempDir(tempDir);
        config460.setResourcePath(birtResourcePath);
        if ("true".equals(ctx.getProperty("report.memory.cache"))) {
            HashMap<Object, Object> appContext = new HashMap<>();
            appContext.put(DataEngine.MEMORY_DATA_SET_CACHE, ctx.getProperty("report.memory.cache.size"));
            config460.setAppContext(appContext);
        }

        ReportEngine reportEngine = new ReportEngine(config460);
        logger.debug("Birt report engine app context: " + config460.getAppContext());

        // Fonts
        String fontDir = ctx.getProperty("report.font.dir");
        if (fontDir == null) {
            fontDir = getInitParameter("report.font.dir");
        }
        if (fontDir != null) {
            FontFactory.registerDirectory(fontDir);
        }

        String dataAccessType = ctx.getProperty("BIRT_DATA_ACCESS_TYPE");

        if (dataAccessType != null && dataAccessType.equals("jndi")) {
            // DATA_ACCESS_TYPE:jndi
            logger.info("BIRT: JNDI database connection");
            String jndi = ctx.getProperty("datasourceName");
            reportEngine.initParametersJndi(jndi);

            try (Connection con = ctx.getDataSource().getConnection()) {
                if (con == null) {
                    logger.error("*** BD : connection null!");
                } else {
                    logger.info("*** BD : " + con.getMetaData().getURL());
                }
            } catch (Exception e) {
                logger.error("Problem getting the connection", e);
                throw new ServletException("init failure");
            }

        } else {
            // DATA_ACCESS_TYPE:direct
            // Donner la possibilité de définir par fichier de configuration...
            logger.info("BIRT: Direct database connection");
            String driverName = ctx.getProperty("birt.driver.name");
            String dbUrl = ctx.getProperty("birt.db.url");
            String dbUsername = ctx.getProperty("birt.db.username");
            // userpass obligatoire !!!
            String dbUserpass = ctx.getProperty("birt.db.userpass");
            try (Connection con = ctx.getDataSource().getConnection()) {
                DatabaseMetaData meta = con.getMetaData();
                if (driverName == null) {
                    driverName = meta.getDriverName();
                }
                logger.debug("Driver : " + driverName);
                if (dbUrl == null) {
                    dbUrl = meta.getURL();
                }
                logger.debug("URL : " + dbUrl);
                if (dbUsername == null) {
                    dbUsername = meta.getUserName();
                }
                logger.debug("userName : " + dbUsername);

                reportEngine.initParameters(driverName, dbUrl, dbUsername, dbUserpass);
            } catch (SQLException e) {
                logger.error("Error initializing the report Engine", e);
            }
        }
        reportEngine.setContextManager(ServiceLocator.getInstance().getContextManager());
        ServiceLocator.getInstance().getContextManager().setReportEngine(reportEngine);
        ServiceLocator.getInstance().getContextManager().setChartEngine(new ChartEngine(ChartEngine.getPlatformConfig()));
        logger.debug("Birt initialised");
    }

    /**
     * Get the log level.
     *
     * @param aLogLevel
     *            the string log level
     * @return the log level
     */
    private static Level getLogLevel(String aLogLevel) {
        if ("SEVERE".equalsIgnoreCase(aLogLevel)) {
            return Level.SEVERE;
        } else if ("WARNING".equalsIgnoreCase(aLogLevel)) {
            return Level.WARNING;
        } else if ("INFO".equalsIgnoreCase(aLogLevel)) {
            return Level.INFO;
        } else if ("CONFIG".equalsIgnoreCase(aLogLevel)) {
            return Level.CONFIG;
        } else if ("FINE".equalsIgnoreCase(aLogLevel)) {
            return Level.FINE;
        } else if ("FINER".equalsIgnoreCase(aLogLevel)) {
            return Level.FINER;
        } else if ("FINEST".equalsIgnoreCase(aLogLevel)) {
            return Level.FINEST;
        } else if ("ALL".equalsIgnoreCase(aLogLevel)) {
            return Level.ALL;
        }
        return Level.OFF;
    }

    @Override
    public void destroy() {
        ServiceLocator.getInstance().getContextManager().setReportEngine(null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws java.io.IOException {
        // do the same as on a get request
        try {
            doGet(req, resp);
        } catch (IOException e) {
            logger.error("Error posting image", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws java.io.IOException {
        try (OutputStream out = resp.getOutputStream()) {
            sendResponse(out, req.getParameter("image"));
        } catch (IOException e) {
            logger.error("Error writing image", e);
        }
    }

    /**
     * This method serializes and sends the given string on the response.
     *
     * @param out
     *            the output stream
     * @param imageName
     *            the image file name
     *
     * @throws java.io.IOException
     *             lorsqu'il y a un problème
     */
    protected void sendResponse(OutputStream out, String imageName) throws java.io.IOException {
        logger.debug("Image name " + imageName);
        String tempo = ServiceLocator.getInstance().getContextManager().getProperty("report.tempo.dir");
        File file = new File(tempo + File.separator + imageName);
        try (FileInputStream input = new FileInputStream(file)) {
            byte[] inBytes = new byte[128];
            int len = input.read(inBytes);
            while (len != -1) {
                out.write(inBytes, 0, len);
                len = input.read(inBytes);
            }
            out.flush();
        }
        Files.delete(file.toPath());
        logger.debug("Temporary image deleted");
    }
}
