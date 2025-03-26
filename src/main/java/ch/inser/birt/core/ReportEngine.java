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

package ch.inser.birt.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IDataIterator;
import org.eclipse.birt.report.engine.api.IEngineTask;
import org.eclipse.birt.report.engine.api.IExtractionResults;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IResultMetaData;
import org.eclipse.birt.report.engine.api.IResultSetItem;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.xml.xmp.XmpWriter;

import ch.inser.birt.util.Constants.ReportType;
import ch.inser.dynamic.common.IContextManager;
import ch.inser.jsl.exceptions.ISException;

/**
 * Report engine pour la génération des rapports Birt
 *
 * @author INSER SA
 *
 */
public class ReportEngine implements Serializable {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 7070435230053511907L;

    /**
     * Dénition de la catégorie de logging
     */
    private static final Log logger = LogFactory.getLog(ReportEngine.class);

    /**
     * The BIRT report engine.
     */
    private transient org.eclipse.birt.report.engine.api.ReportEngine iReportEngine;

    /**
     * The map of parameters
     */
    private transient Map<String, Object> iParameters;

    /** Injecté par le BirtServlet */
    private transient IContextManager iContextManager;

    /**
     * Constructor.
     *
     * @param config
     *            the engine configuration
     */
    public ReportEngine(EngineConfig config) {
        iReportEngine = new org.eclipse.birt.report.engine.api.ReportEngine(config);
    }

    /**
     * Helper method to obtain an EngineConfig to construct the report engine. usage: new EngineConfig(getEngineConfig(...));
     *
     * @param aBirtHome
     *            set the BIRT_HOME system property
     * @param aLogDirectoryName
     *            the directory name of the log file(e.g C:\Log). Engine appends a file name with date and time to the directory name (e.g.
     *            C:\Log\BIRT_Engine_2005_02_26_11_26_56.log).
     * @param aTempDirectoryName
     *            the directory name for the temporary files
     * @param aLevel
     *            the engine log level
     * @return the engine configuration
     */
    public static EngineConfig getEngineConfig(String aBirtHome, String aLogDirectoryName, String aTempDirectoryName, Level aLevel) {
        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setEngineHome(aBirtHome);
        engineConfig.setLogConfig(aLogDirectoryName, aLevel);
        engineConfig.setTempDir(aTempDirectoryName);
        return engineConfig;
    }

    /**
     * To initialize the parameters);
     *
     * @param aDriver
     *            the driver for the JDBC connection
     * @param aUrl
     *            the url for the JDBC connection
     * @param aUser
     *            the user name for the JDBC connection
     * @param aPassword
     *            the password for the JDBC connection
     */
    public void initParameters(String aDriver, String aUrl, String aUser, String aPassword) {
        iParameters = new HashMap<>(4, 1);
        iParameters.put("jdbcDriver", aDriver);
        iParameters.put("jdbcUrl", aUrl);
        iParameters.put("jdbcUser", aUser);
        iParameters.put("jdbcPassword", aPassword);
    }

    /**
     * Initialize the JDBC-JNDI parameter.
     *
     * @param jdbcJndi
     *            the JDBC-JNDI parameter
     */
    public void initParametersJndi(String jdbcJndi) {
        iParameters = new HashMap<>();
        iParameters.put("jdbcJndi", jdbcJndi);
    }

    /**
     * Create and run a task renderer.
     *
     * @param reportRunnable
     *            a runnable report
     * @return the task
     */
    public IRunAndRenderTask createRunAndRenderTask(IReportRunnable reportRunnable) {
        return iReportEngine.createRunAndRenderTask(reportRunnable);
    }

    /**
     * Creates a getParamerDefinitionTask
     *
     * @param reportRunnable
     *            a runnable report
     * @return the task
     */
    public IGetParameterDefinitionTask createGetParameterDefinitionTask(IReportRunnable reportRunnable) {
        return iReportEngine.createGetParameterDefinitionTask(reportRunnable);
    }

    /**
     * Open a report design.
     *
     * @param designName
     *            the design file name
     * @return a runnable report
     * @throws EngineException
     *             erreur dans le Birt report engine
     */
    public IReportRunnable openReportDesign(String designName) throws EngineException {
        return iReportEngine.openReportDesign(designName);
    }

    /**
     * Open a report design.
     *
     * @param designStream
     *            the design file input stream
     * @return a runnable report
     * @throws EngineException
     *             problème de lecture du rapport BIRT
     */
    public IReportRunnable openReportDesign(InputStream designStream) throws EngineException {
        return iReportEngine.openReportDesign(designStream);
    }

    /**
     * Create an HTML or PDF report.
     *
     * @param aReportFile
     *            the report design file, either a File or Blob object
     * @param aParameters
     *            the parameters for the report
     * @param aFormat
     *            the output format: PDF, HTML or Excel
     * @param aEmbeddable
     *            <code>true</code> for an embeddable HTML (without &lt;HTML&gt; and &lt;BODY&gt;)
     * @param aLocale
     *            the locale to use in the report generation
     * @return the report
     * @throws EngineException
     *             throwed when the report design file does not exist or is invalid or when an exception occurs in the report generation
     * @throws SQLException
     *             thrown when the report design file is a blob and cannot be read in an input stream
     */
    public ByteArrayOutputStream getReport(Object aReportFile, Map<String, Object> aParameters, String aFormat, boolean aEmbeddable,
            Locale aLocale) throws EngineException, SQLException {
        String outFormat = aFormat;
        if (outFormat == null) {
            outFormat = "html";
        }
        // Temporary output stream while BIRT close the stream
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IRunAndRenderTask task = null;
        InputStream reportIS = null;
        try {
            // Read the design and create the task
            IReportRunnable design = null;
            if (aReportFile instanceof File) {
                design = iReportEngine.openReportDesign(((File) aReportFile).getPath());
            } else {
                reportIS = ((Blob) aReportFile).getBinaryStream();
                design = iReportEngine.openReportDesign(reportIS);
            }
            task = iReportEngine.createRunAndRenderTask(design);
            if (aLocale != null) {
                task.setLocale(aLocale);
            }

            // Set the parameters
            ReportEngine.setParams(task, iParameters);
            ReportEngine.setParams(task, aParameters);

            // Set Render context and rendering options
            HashMap<Object, Object> contextMap = new HashMap<>();
            RenderOption options;
            if (outFormat.equals("pdf")) {
                options = new PDFRenderOption();
                options.setOutputFormat("pdf");
            } else if (outFormat.equals("excel")) {
                options = new EXCELRenderOption();
                options.setOutputFormat("xls");
            } else if (outFormat.equals("excel-xlsx")) {
                options = new EXCELRenderOption();
                options.setOutputFormat("xlsx");
            } else if (outFormat.equals("doc")) {
                options = new RenderOption();
                options.setOutputFormat("doc");
            } else {
                // OK html
                HTMLRenderOption optionsHTML = new HTMLRenderOption();
                options = optionsHTML;
                options.setImageHandler(new HTMLServerImageHandler());
                String tempo = getContextManager().getProperty("report.tempo.dir");
                optionsHTML.setImageDirectory(tempo);
                optionsHTML.setBaseImageURL("BirtServlet?image=");
                if (aEmbeddable) {
                    optionsHTML.setEmbeddable(true);
                }
            }
            options.setOutputStream(output);
            task.setAppContext(contextMap);
            task.setRenderOption(options);

            // Run the report
            task.run();

            return output;
        } finally {
            if (task != null) {
                try {
                    task.close();
                } catch (Exception ex) {
                    logger.error("Error getting report", ex);
                }
            }
            if (reportIS != null) {
                try {
                    reportIS.close();
                } catch (IOException e) {
                    logger.error("Error closing report design input stream", e);
                }
            }
        }
    }

    /**
     * Create an HTML or PDF report.
     *
     * @param aStream
     *            the result stream
     * @param aReportFile
     *            the report design file, either as File or Blob object
     * @param aParameters
     *            the parameters for the report
     * @param outFormat
     *            PDF, HTML or Excel
     * @param aEmbeddable
     *            <code>true</code> for an embeddable HTML (without &lt;HTML&gt; and &lt;BODY&gt;)
     * @param aLocale
     *            the locale to use in the report generation
     * @throws EngineException
     *             throwed when the report design file does not exist or is invalid or when an exception occurs in the report generation
     * @throws IOException
     *             if an I/O error occurs
     * @throws SQLException
     *             thrown when the report design file is a blob and cannot be opened in an input stream
     */
    public void getReport(OutputStream aStream, Object aReportFile, Map<String, Object> aParameters, String outFormat, boolean aEmbeddable,
            Locale aLocale) throws EngineException, IOException, SQLException {
        aStream.write(getReport(aReportFile, aParameters, outFormat, aEmbeddable, aLocale).toByteArray());

    }

    /**
     * Extract the data of a report in a text file.
     *
     * @param aStream
     *            the result stream
     * @param aReportFile
     *            the report design file
     * @param aParameters
     *            the parametres for the report
     * @param aSeparator
     *            the field separator ex: <code>','</code> or <code>'\t'</code>
     * @throws BirtException
     *             erreur dans l'éxtraction du rapport
     * @throws IOException
     *             erreur au niveau d'écriture dans le fichier
     */
    public void extractData(OutputStream aStream, Object aReportFile, Map<String, Object> aParameters, char aSeparator)
            throws BirtException, IOException {
        // Get a print stream from the output stream
        PrintStream stream;
        if (aStream instanceof PrintStream) {
            stream = (PrintStream) aStream;
        } else {
            stream = new PrintStream(aStream);
        }

        // Create the temporary report document
        File file = File.createTempFile("birt_", ".rptdocument");

        IRunTask runTask = null;
        IDataExtractionTask extractionTask = null;
        IReportDocument document = null;
        try {
            // Read the design and create de task
            IReportRunnable design = null;
            if (aReportFile instanceof File) {
                design = iReportEngine.openReportDesign(((File) aReportFile).getPath());
            } else {
                design = iReportEngine.openReportDesign((InputStream) aReportFile);
            }
            runTask = iReportEngine.createRunTask(design);

            // Set the parameters
            ReportEngine.setParams(runTask, iParameters);
            ReportEngine.setParams(runTask, aParameters);

            // Run the report and store the result in the temporary file
            runTask.run(file.getPath());

            // Read the temporary file and create the extraction task
            document = iReportEngine.openReportDocument(file.getPath());
            extractionTask = iReportEngine.createDataExtractionTask(document);

            // Iterate over all the data sets in the report
            for (Object obj : extractionTask.getResultSetList()) {
                // Set the result set name
                IResultSetItem resultSetItem = (IResultSetItem) obj;
                extractionTask.selectResultSet(resultSetItem.getResultSetName());

                // Extract the data
                IExtractionResults extractResults = extractionTask.extract();

                // Read the internal result and construct the external text
                // result
                IDataIterator it = extractResults.nextResultIterator();
                IResultMetaData metadata = it.getResultMetaData();

                // Write the field names in the first row
                for (int i = 0; i < metadata.getColumnCount(); i++) {
                    if (i > 0) {
                        stream.print(aSeparator);
                    }
                    stream.print(metadata.getColumnLabel(i));
                }
                stream.println();

                // Write the data
                while (it.next()) {
                    for (int i = 0; i < metadata.getColumnCount(); i++) {
                        if (i > 0) {
                            stream.print(aSeparator);
                        }
                        stream.print(it.getValue(i));
                    }
                    stream.println();
                }
                it.close();
                extractResults.close();
            }
            stream.flush();
        } finally {
            if (runTask != null) {
                try {
                    runTask.close();
                } catch (Exception ex) {
                    logger.error("Error closing data", ex);
                }
            }
            if (extractionTask != null) {
                try {
                    extractionTask.close();
                } catch (Exception ex) {
                    logger.error("Error closing data", ex);
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (Exception ex) {
                    logger.error("Error closing document", ex);
                }
            }
            if (file != null && file.exists()) {
                try {
                    Files.delete(file.toPath());
                } catch (Exception ex) {
                    logger.error("Error closing document", ex);
                }
            }
        }
    }

    /**
     * Writes a PDF report to the output stream. The PDF is stripped of the Creator attribute, which contains birt runtime version and
     * location and therefore compromises security
     *
     * @param aStream
     *            outputstream to write the report in
     * @param aReportFile
     *            design file
     * @param aParameters
     *            report parameters
     * @param aLocale
     *            language
     * @throws ISException
     *             error in design file or report generation
     */
    public void writeSecurePDF(OutputStream aStream, Object aReportFile, Map<String, Object> aParameters, Locale aLocale)
            throws ISException {
        try {
            aStream.write(getSecureReport(aReportFile, aParameters, ReportType.PDF.toString(), false, aLocale));
        } catch (IOException e) {
            throw new ISException(e);
        }
    }

    /**
     * Creates an HTML or PDF report. In case of PDF, creates it without the Creator attribute, which can compromise security
     *
     *
     * @param aReportFile
     *            the report design file, either a File or Blob object
     * @param aParameters
     *            the parameters for the report
     * @param aFormat
     *            the output format: PDF, HTML or Excel
     * @param aEmbeddable
     *            <code>true</code> for an embeddable HTML (without &lt;HTML&gt; and &lt;BODY&gt;)
     * @param aLocale
     *            the locale to use in the report generation
     * @return the report
     * @throws ISException
     *             error in the design file or in the generation of the repport
     */
    public byte[] getSecureReport(Object aReportFile, Map<String, Object> aParameters, String aFormat, boolean aEmbeddable, Locale aLocale)
            throws ISException {
        try (ByteArrayOutputStream baos = getReport(aReportFile, aParameters, aFormat, aEmbeddable, aLocale)) {
            if (ReportType.PDF.toString().equalsIgnoreCase(aFormat)) {
                return removeCreatorAttribute(baos.toByteArray());
            }
            return baos.toByteArray();
        } catch (EngineException | IOException | SQLException e) {
            throw new ISException(e);
        }
    }

    /**
     * Supprime l'information sur l'emplacement et version de Birt runtime dans le méta-info "Creator" du PDF
     *
     * @param aPdf
     *            le pdf Birt
     * @return le pdf Birt sans path vers Birt runtime
     * @throws ISException
     *             erreur de lecture ou écriture dans le pdf
     */
    private byte[] removeCreatorAttribute(byte[] aPdf) throws ISException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(aPdf);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream()) {
            // read existing pdf document
            PdfReader reader = new PdfReader(bais);
            PdfStamper stamper = new PdfStamper(reader, baos);

            // get and edit meta-data
            @SuppressWarnings("unchecked")
            HashMap<String, String> info = reader.getInfo();
            String creator = info.get("Creator");
            if (creator == null || creator.isEmpty()) {
                return aPdf;
            }
            info.put("Creator", null);

            // add updated meta-data to pdf
            stamper.setMoreInfo(info);

            // update xmp meta-data
            XmpWriter xmp = new XmpWriter(baos2, info);
            xmp.close();
            stamper.setXmpMetadata(baos2.toByteArray());
            stamper.close();
            return baos.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new ISException(e);
        }
    }

    /**
     * Set the engine parameters into the task.
     *
     * @param task
     *            birt engine task
     */
    public void setParams(IEngineTask task) {
        setParams(task, iParameters);
    }

    /**
     * Set the parameters into the task.
     *
     * @param task
     *            birt engine task
     * @param aParams
     *            paramètres du rapport
     */
    private static void setParams(IEngineTask task, Map<String, Object> aParams) {
        if (aParams != null && !aParams.isEmpty()) {
            task.setParameterValues(aParams);
        }
    }

    /**
     * Destroy this engine.
     */
    public void destroy() {
        iReportEngine.destroy();
    }

    /**
     *
     * @return context manager
     */
    public IContextManager getContextManager() {
        return iContextManager;
    }

    /**
     *
     * @return la configuration du Birt report engine
     */
    public EngineConfig getConfig() {
        return iReportEngine.getConfig();
    }

    /**
     *
     * @param aContextManager
     *            context manager
     */
    public void setContextManager(IContextManager aContextManager) {
        iContextManager = aContextManager;
    }
}
