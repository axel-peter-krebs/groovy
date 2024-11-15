/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.dateutil.extensions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class defines new groovy static methods which appear on normal JDK
 * Date and Calendar classes inside the Groovy environment.
 */
public class DateUtilStaticExtensions {
    /**
     * Parse a String into a Date instance using the given pattern.
     * This convenience method acts as a wrapper for {@link java.text.SimpleDateFormat}.
     * <p>
     * Note that a new SimpleDateFormat instance is created for every
     * invocation of this method (for thread safety).
     *
     * @param self   placeholder variable used by Groovy categories; ignored for default static methods
     * @param format pattern used to parse the input string.
     * @param input  String to be parsed to create the date instance
     * @return a new Date instance representing the parsed input string
     * @throws ParseException if there is a parse error
     * @see java.text.SimpleDateFormat#parse(java.lang.String)
     * @since 1.5.7
     */
    public static Date parse(Date self, String format, String input) throws ParseException {
        return new SimpleDateFormat(format).parse(input);
    }

    /**
     * Parse a String into a Date instance using the given pattern and TimeZone.
     * This convenience method acts as a wrapper for {@link java.text.SimpleDateFormat}.
     * <p>
     * Note that a new SimpleDateFormat instance is created for every
     * invocation of this method (for thread safety).
     *
     * @param self   placeholder variable used by Groovy categories; ignored for default static methods
     * @param format pattern used to parse the input string.
     * @param input  String to be parsed to create the date instance
     * @param zone   TimeZone to use when parsing
     * @return a new Date instance representing the parsed input string
     * @throws ParseException if there is a parse error
     * @see java.text.SimpleDateFormat#parse(java.lang.String)
     * @since 2.4.1
     */
    public static Date parse(Date self, String format, String input, TimeZone zone) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(zone);
        return sdf.parse(input);
    }

    /**
     * Parse a String matching the pattern EEE MMM dd HH:mm:ss zzz yyyy
     * containing US-locale-constants only (e.g. Sat for Saturdays).
     * Such a string is generated by the toString method of {@link java.util.Date}
     * <p>
     * Note that a new SimpleDateFormat instance is created for every
     * invocation of this method (for thread safety).
     *
     * @param self         placeholder variable used by Groovy categories; ignored for default static methods
     * @param dateToString String to be parsed to create the date instance. Must match the pattern EEE MMM dd HH:mm:ss zzz yyyy with US-locale symbols
     * @return a new Date instance representing the parsed input string
     * @throws ParseException if there is a parse error
     * @since 1.8.4
     */
    public static Date parseToStringDate(Date self, String dateToString) throws ParseException {
        return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(dateToString);
    }

}
