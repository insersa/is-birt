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

import java.io.Serializable;

import org.eclipse.birt.core.framework.PlatformConfig;

/**
 * Wrapper class for org.eclipse.birt.chart.api.ChartEngine
 *
 * @author INSER SA
 *
 */
public class ChartEngine implements Serializable {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 8441885032210488542L;

    /**
     * Constructor.
     *
     * @param config
     *            the platform configuration
     */
    public ChartEngine(PlatformConfig config) {
        org.eclipse.birt.chart.api.ChartEngine.instance(config);
    }

    /**
     * Helper method to obtain an platform configuration to construct the chart
     * engine. usage: new ChartConfig(getPlatformConfig(...));
     *
     * Attention: paramètre aBirtHome (BIRT_HOME system property) n'est plus
     * configuré depuis Birt 3.7.2
     *
     * @return a platform configuration
     */
    public static PlatformConfig getPlatformConfig() {
        PlatformConfig platformConfig = new PlatformConfig();
        // standalone platform
        platformConfig.setProperty("STANDALONE", "true");
        return platformConfig;
    }
}
