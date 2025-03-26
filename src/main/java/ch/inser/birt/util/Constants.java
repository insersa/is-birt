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

package ch.inser.birt.util;

/**
 * Constrants de la librairie isBirt
 *
 * @author INSER SA
 *
 */
public class Constants {

    /**
     * Types de rapports Birt
     *
     * @author INSER SA
     *
     */
    public enum ReportType {
        /** Rapport Birt de type PDF */
        PDF,

        /** Rapport Birt de type Word (rtf) */
        DOC,

        /** Rapport Birt de type CSV */
        CSV;

        @Override
        public String toString() {
            switch (this) {
                case PDF:
                    return "pdf";
                case DOC:
                    return "doc";
                case CSV:
                    return "csv";
                default:
                    return super.toString();

            }
        }
    }
}
