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
package org.apache.groovy.json.internal;

import groovy.json.JsonException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import static java.lang.Boolean.parseBoolean;
import static org.apache.groovy.json.internal.CharScanner.isInteger;
import static org.apache.groovy.json.internal.CharScanner.parseDouble;
import static org.apache.groovy.json.internal.CharScanner.parseFloat;
import static org.apache.groovy.json.internal.CharScanner.parseIntFromTo;
import static org.apache.groovy.json.internal.CharScanner.parseLongFromTo;
import static org.apache.groovy.json.internal.Exceptions.die;
import static org.apache.groovy.json.internal.Exceptions.sputs;

public class NumberValue extends java.lang.Number implements Value {

    private final Type type;
    private char[] buffer;
    private boolean chopped;
    private int startIndex;
    private int endIndex;
    private Object value;

    public NumberValue(Type type) {
        this.type = type;
    }

    public NumberValue() {
        this.type = null;
    }

    public NumberValue(boolean chop, Type type, int startIndex, int endIndex, char[] buffer) {
        this.type = type;

        try {
            if (chop) {
                this.buffer = ArrayUtils.copyRange(buffer, startIndex, endIndex);
                this.startIndex = 0;
                this.endIndex = this.buffer.length;
                chopped = true;
            } else {
                this.startIndex = startIndex;
                this.endIndex = endIndex;
                this.buffer = buffer;
            }
        } catch (Exception ex) {
            Exceptions.handle(sputs("exception", ex, "start", startIndex, "end", endIndex), ex);
        }

        // Check for a single minus now, rather than finding out later during lazy parsing.
        if (this.endIndex - this.startIndex == 1 && this.buffer[this.startIndex] == '-') {
            die("A single minus is not a valid number");
        }

    }

    public static <T extends Enum> T toEnum(Class<T> cls, int value) {
        T[] enumConstants = cls.getEnumConstants();
        for (T e : enumConstants) {
            if (e.ordinal() == value) {
                return e;
            }
        }
        die("Can't convert ordinal value " + value + " into enum of type " + cls);
        return null;
    }

    @Override
    public String toString() {
        if (startIndex == 0 && endIndex == buffer.length) {
            return FastStringUtils.noCopyStringFromChars(buffer);
        } else {
            return new String(buffer, startIndex, (endIndex - startIndex));
        }
    }

    @Override
    public final Object toValue() {
        return value != null ? value : (value = doToValue());
    }

    @Override
    public <T extends Enum> T toEnum(Class<T> cls) {
        return toEnum(cls, intValue());
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    private Object doToValue() {
        switch (type) {
            case DOUBLE:
                return bigDecimalValue();
            case INTEGER:
                if (isInteger(buffer, startIndex, endIndex - startIndex)) {
                    return intValue();
                } else {
                    return longValue();
                }
        }
        die();
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Value)) return false;

        NumberValue value1 = (NumberValue) o;

        if (endIndex != value1.endIndex) return false;
        if (startIndex != value1.startIndex) return false;
        if (!Arrays.equals(buffer, value1.buffer)) return false;
        if (type != value1.type) return false;
        return Objects.equals(value, value1.value);

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (buffer != null ? Arrays.hashCode(buffer) : 0);
        result = 31 * result + startIndex;
        result = 31 * result + endIndex;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public BigDecimal bigDecimalValue() {
        try {
            return new BigDecimal(buffer, startIndex, endIndex - startIndex);
        } catch (NumberFormatException e) {
            throw new JsonException("unable to parse " + new String(buffer, startIndex, endIndex - startIndex), e);
        }
    }

    @Override
    public BigInteger bigIntegerValue() {
        return new BigInteger(toString());
    }

    @Override
    public String stringValue() {
        return toString();
    }

    @Override
    public String stringValueEncoded() {
        return toString();
    }

    @Override
    public Date dateValue() {
        return new Date(Dates.utc(longValue()));
    }

    @Override
    public int intValue() {
        return parseIntFromTo(buffer, startIndex, endIndex);
    }

    @Override
    public long longValue() {
        if (isInteger(buffer, startIndex, endIndex - startIndex)) {
            return parseIntFromTo(buffer, startIndex, endIndex);
        } else {
            return parseLongFromTo(buffer, startIndex, endIndex);
        }
    }

    @Override
    public byte byteValue() {
        return (byte) intValue();
    }

    @Override
    public short shortValue() {
        return (short) intValue();
    }

    @Override
    public double doubleValue() {
        return parseDouble(this.buffer, startIndex, endIndex);
    }

    @Override
    public boolean booleanValue() {
        return parseBoolean(toString());
    }

    @Override
    public float floatValue() {
        return parseFloat(this.buffer, startIndex, endIndex);
    }

    @Override
    public final void chop() {
        if (!chopped) {
            this.chopped = true;
            this.buffer = ArrayUtils.copyRange(buffer, startIndex, endIndex);
            this.startIndex = 0;
            this.endIndex = this.buffer.length;
        }
    }

    @Override
    public char charValue() {
        return buffer[startIndex];
    }
}
