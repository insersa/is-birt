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

import java.io.Serializable;

import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

import ch.inser.dynamic.common.IValueObject.Type;

/**
 * Classe utilitaires pour transformer des constants du BIRT en constants du
 * IValueObject
 *
 * @author INSER SA
 *
 */
public class ReportUtil implements Serializable {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -230227750513854702L;

    /**
     * Enumeration de display types des paramètres du rapport
     *
     * @author INSER SA
     *
     */
    public enum DisplayType {

        /** Input text */
        TEXT_BOX,

        /** Liste de choix (multi-select) */
        LIST_BOX,

        /** Liste de choix (simple-select) */
        COMBO_BOX,

        /** Radio button */
        RADIO_BUTTON;

        /** Int value of TEXT_BOX */
        private static final int TEXT_BOX_INT = 1;

        /** Int value of LIST_BOX */
        private static final int LIST_BOX_INT = 2;

        /** Int value of COMBO_BOX */
        private static final int COMBO_BOX_INT = 3;

        /** Int value of RADIO_BUTTON */
        private static final int RADIO_BUTTON_INT = 4;

        /**
         * Transforms from enum DataType to int
         *
         * @return int value of a DisplayType
         */
        public int intValue() {
            switch (this) {
                case TEXT_BOX:
                    return TEXT_BOX_INT;
                case LIST_BOX:
                    return LIST_BOX_INT;
                case COMBO_BOX:
                    return COMBO_BOX_INT;
                case RADIO_BUTTON:
                    return RADIO_BUTTON_INT;
                default:
                    return -1;
            }
        }

        /**
         * Transforms from intValue to DisplayType
         *
         * @param intValue
         *            the intValue of a DisplayType
         * @return the corresponding DisplayType
         */
        public static DisplayType typeValue(int intValue) {
            switch (intValue) {
                case TEXT_BOX_INT:
                    return TEXT_BOX;
                case LIST_BOX_INT:
                    return LIST_BOX;
                case COMBO_BOX_INT:
                    return COMBO_BOX;
                case RADIO_BUTTON_INT:
                    return RADIO_BUTTON;
                default:
                    return null;
            }
        }

        /**
         * Transforme l'enumeration actuelle en enumeration du BIRT
         *
         * @param aType
         *            display type dans l'enumeration actuelle
         * @return displaytype dans BIRT
         */
        public static int getBirtDisplayType(DisplayType aType) {
            switch (aType) {
                case TEXT_BOX:
                    return IScalarParameterDefn.TEXT_BOX;
                case LIST_BOX:
                    return IScalarParameterDefn.LIST_BOX;
                case COMBO_BOX:
                    return IScalarParameterDefn.CHECK_BOX;
                case RADIO_BUTTON:
                    return IScalarParameterDefn.RADIO_BUTTON;
                default:
                    return -1;
            }
        }

        /**
         * Transforme l'enumeration du BIRT en enumeration actuelle
         * (DisplayType)
         *
         * @param aBirtType
         *            displayType dans BIRT
         * @return displayType dans l'enumeration actuelle
         */
        public static DisplayType getDisplayType(int aBirtType) {
            switch (aBirtType) {
                case IScalarParameterDefn.TEXT_BOX:
                    return TEXT_BOX;
                case IScalarParameterDefn.LIST_BOX:
                    return LIST_BOX;
                case IScalarParameterDefn.CHECK_BOX:
                    return COMBO_BOX;
                case IScalarParameterDefn.RADIO_BUTTON:
                    return RADIO_BUTTON;
                default:
                    return null;
            }
        }
    }

    /**
     * Transform des data types de Birt en data types du isejawa
     *
     * @param aBirtDataType
     *            data type dans l'enumeration du IParameterDefn
     * @return data type dans l'enumeration du DynamicVO
     */
    public static Type getType(int aBirtDataType) {

        switch (aBirtDataType) {
            case IParameterDefn.TYPE_STRING:
                return Type.STRING;
            case IParameterDefn.TYPE_BOOLEAN:
                return Type.BOOLEAN;
            case IParameterDefn.TYPE_INTEGER:
                return Type.INTEGER;
            case IParameterDefn.TYPE_DECIMAL:
            case IParameterDefn.TYPE_FLOAT:
                return Type.DOUBLE;
            case IParameterDefn.TYPE_DATE:
                return Type.DATE;
            case IParameterDefn.TYPE_TIME:
                return Type.TIME;
            case IParameterDefn.TYPE_DATE_TIME:
                return Type.TIMESTAMP;
            default:
                return null;
        }
    }

    /** Int value of data type String */
    public static final int STRING = 1;

    /** Int value of data type Boolean */
    public static final int BOOLEAN = 2;

    /** Int value of data type Integer */
    public static final int INTEGER = 3;

    /** Int value of data type Double */
    public static final int DOUBLE = 4;

    /** Int value of data type Date */
    public static final int DATE = 5;

    /** Int value of data type Time */
    public static final int TIME = 6;

    /** Int value of data type Timestamp */
    public static final int TIMESTAMP = 7;

    /**
     * Transforms an isEJaWa dataType to int
     *
     * @param aDataType
     *            data type in isEJaWa enum
     * @return intValue
     */
    public static int intValue(Type aDataType) {
        switch (aDataType) {
            case STRING:
                return STRING;
            case BOOLEAN:
                return BOOLEAN;
            case INTEGER:
                return INTEGER;
            case DOUBLE:
                return DOUBLE;
            case DATE:
                return DATE;
            case TIME:
                return TIME;
            case TIMESTAMP:
                return TIMESTAMP;
            default:
                return -1;
        }
    }

    /**
     * Transforms from int value to isEJaWa type
     *
     * @param intValue
     *            the int-value defined in ReportUtil
     * @return an isEJaWa data type
     */
    public static Type typeValue(int intValue) {
        switch (intValue) {
            case STRING:
                return Type.STRING;
            case BOOLEAN:
                return Type.BOOLEAN;
            case INTEGER:
                return Type.INTEGER;
            case DOUBLE:
                return Type.DOUBLE;
            case DATE:
                return Type.DATE;
            case TIME:
                return Type.TIME;
            case TIMESTAMP:
                return Type.TIMESTAMP;
            default:
                return null;
        }
    }

    /**
     * Transforme un data type de isEJaWa dans un data type de Birt
     *
     * @param aDataType
     *            data type dans isEJaWa
     * @return data type dans Birt (int)
     */
    public static int getBirtType(Type aDataType) {
        switch (aDataType) {
            case STRING:
                return IParameterDefn.TYPE_STRING;
            case BOOLEAN:
                return IParameterDefn.TYPE_BOOLEAN;
            case INTEGER:
                return IParameterDefn.TYPE_INTEGER;
            case DOUBLE:
                return IParameterDefn.TYPE_FLOAT;
            case DATE:
                return IParameterDefn.TYPE_DATE;
            case TIME:
                return IParameterDefn.TYPE_TIME;
            case TIMESTAMP:
                return IParameterDefn.TYPE_DATE_TIME;
            default:
                return -1;
        }
    }

}
