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

package ch.inser.birt.format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EngineException;

import ch.inser.birt.core.ReportEngine;
import ch.inser.birt.util.Constants.ReportType;
import ch.inser.dynamic.common.DAOParameter;
import ch.inser.dynamic.common.DAOResult;
import ch.inser.dynamic.common.IContextManager;
import ch.inser.dynamic.common.IDAOResult;
import ch.inser.dynamic.common.IDAOResult.Status;
import ch.inser.dynamic.common.ILoggedUser;
import ch.inser.dynamic.common.IValueObject;
import ch.inser.dynaplus.format.IFormatEngine;
import ch.inser.jsl.exceptions.ISException;

/**
 * Engine pour récuperer le rapport BIRT pour un enregistrement selon format demandé (pdf, doc,...)
 *
 * Utilisé par la couche BO, dans getRecord ou getList
 *
 * Le nom du rapport BIRT est par défaut "print_<nom_objet>.rptdesign". Si le nom est différent du défaut, le BO spécialisé de l'objet
 * métier doit fournir le nom dans la méthode getFormatParameters(), paramètre "filename"
 *
 * @author INSER SA
 *
 */
public class BirtFormatEngine implements IFormatEngine {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(BirtFormatEngine.class);

    /** Context manager de l'application */
    private IContextManager iCtx;

    @Override
    public IDAOResult format(IValueObject aVo, DAOParameter... aParameters) throws ISException {
        // Report design
        String filename = "print_" + aVo.getName().toLowerCase() + ".rptdesign";
        if (DAOParameter.getValue("filename", aParameters) != null) {
            filename = (String) DAOParameter.getValue("filename", aParameters);
        }
        String fullName = iCtx.getProperty("report.dir") + File.separator + filename;

        // Paramètres du rapport Birt
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", aVo.getId());
        if (DAOParameter.getValue("id", aParameters) != null) {
            parameters.put("id", DAOParameter.getValue("id", aParameters));
        }
        for (DAOParameter param : aParameters) {
            if (DAOParameter.Name.OTHER.equals(param.getName())) {
                parameters.put(param.getOtherName(), param.getValue());
            }
        }

        // Langue
        String loc = (String) DAOParameter.getValue(DAOParameter.Name.RESULT_LANG, aParameters);
        if (loc == null) {
            loc = iCtx.getProperty("report.default.lang");
        }

        // Crée le rapport
        byte[] report = getReport(fullName, getReportFormat((Format) DAOParameter.getValue(DAOParameter.Name.RESULT_FORMAT, aParameters)),
                loc, parameters);
        IDAOResult result = new DAOResult(Status.OK);
        result.setValue(report);
        return result;
    }

    /**
     * Exécution de création du rapport Birt
     *
     * @param aReportName
     *            nomn du rapport yc le path
     * @param aType
     *            option de format Birt
     * @param aLang
     *            langue
     * @param aParameters
     *            paramètres du rapport
     * @return rapport en byte[]
     * @throws ISException
     *             erreur d'exécution du rapport
     */
    protected byte[] getReport(String aReportName, ReportType aType, String aLang, Map<String, Object> aParameters) throws ISException {
        ReportEngine reportEngine = (ReportEngine) iCtx.getReportEngine();
        byte[] report;
        try (ByteArrayOutputStream out = reportEngine.getReport(new File(aReportName), aParameters, aType.toString(), false,
                new Locale(aLang))) {
            report = out.toByteArray();
        } catch (SQLException | EngineException | IOException e) {
            logger.error("Erreur de création du rapport", e);
            throw new ISException(e);
        }
        return report;
    }

    /**
     *
     * @param aFormat
     *            option de format de format engine
     * @return option de format de report engine
     */
    private ReportType getReportFormat(Format aFormat) {
        if (Format.PDF.equals(aFormat)) {
            return ReportType.PDF;
        }
        throw new UnsupportedOperationException("Format " + aFormat + " pas implémenté!");
    }

    /**
     *
     * @return contextmanager
     */
    public IContextManager getContextManager() {
        return iCtx;
    }

    /**
     *
     * @param aCtx
     *            contextmanager
     */
    public void setContextManager(IContextManager aCtx) {
        iCtx = aCtx;
    }

    @Override
    public IDAOResult format(List<IValueObject> aRecords, ILoggedUser aUser, DAOParameter... aParams) throws ISException {
        throw new UnsupportedOperationException("Formatage de list d'enregistrements pas implémenté pour Birt");
    }

}
