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
package org.codehaus.groovy.runtime.metaclass;

/**
 * WARNING: This class is for internal use only!
 * We use this class to store information about if a default MetaClass is
 * used for certain primitives.
 */
public class DefaultMetaClassInfo {

    //---------------------------------------------
    //                  boolean
    //---------------------------------------------

    private static final Object constantMetaClassVersioningLock = new Object();
    // if original boolean metaclass
    private static boolean origBoolean = true;
    // if origBoolean and withoutCustomHandle
    private static boolean origBooleanRes = true;
    // if original Byte metaclass
    private static boolean origByte = true;

    //---------------------------------------------
    //                  byte
    //---------------------------------------------
    // if origByte and withoutCustomHandle
    private static boolean origByteRes = true;
    // if original char metaclass
    private static boolean origChar = true;
    // if origChar and withoutCustomHandle
    private static boolean origCharRes = true;
    // if original short metaclass
    private static boolean origShort = true;

    //---------------------------------------------
    //                  char
    //---------------------------------------------
    // if origShort and withoutCustomHandle
    private static boolean origShortRes = true;
    // if original Integer metaclass
    private static boolean origInt = true;
    // if origInt and withoutCustomHandle
    private static boolean origIntRes = true;
    // if original Integer[] metaclass
    private static boolean origIntArray = true;

    //---------------------------------------------
    //                  short
    //---------------------------------------------
    // if origInt and withoutCustomHandle
    private static boolean origIntArrayWCH = true;
    // if original long metaclass
    private static boolean origLong = true;
    // if origLong and withoutCustomHandle
    private static boolean origLongRes = true;
    // if original float metaclass
    private static boolean origFloat = true;

    //---------------------------------------------
    //                  int
    //---------------------------------------------
    // if origFloat and withoutCustomHandle
    private static boolean origFloatRes = true;
    // if original double metaclass
    private static boolean origDouble = true;
    // if origFloat and withoutCustomHandle
    private static boolean origDoubleRes = true;
    // if a custom metaclass creation handle is set
    private static boolean withoutCustomHandle = true;

    //---------------------------------------------
    //                  int[]
    //---------------------------------------------
    //---------------------------------------------
    //              category handle
    //---------------------------------------------
    private static boolean categoryUsed = false;
    private static boolean disabledStandardMC = false;
    private static ConstantMetaClassVersioning constantMetaClassVersioning = new ConstantMetaClassVersioning();

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original boolean metaclass is used.
     */
    public static boolean isOrigBool() {
        return origBooleanRes;
    }

    //---------------------------------------------
    //                  long
    //---------------------------------------------

    /**
     * Sets if the original boolean metaclass is used.
     */
    public static void setOrigBool(boolean v) {
        origBoolean = v;
        origBooleanRes = withoutCustomHandle && origBoolean;
    }

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original byte metaclass is used.
     */
    public static boolean isOrigByte() {
        return origByteRes;
    }

    /**
     * Sets if the original byte metaclass is used.
     */
    public static void setOrigByte(boolean v) {
        origByte = v;
        origByteRes = withoutCustomHandle && origByte;
    }

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original char metaclass is used.
     */
    public static boolean isOrigChar() {
        return origCharRes;
    }

    //---------------------------------------------
    //                  float
    //---------------------------------------------

    /**
     * Sets if the original char metaclass is used.
     */
    public static void setOrigChar(boolean v) {
        origChar = v;
        origCharRes = withoutCustomHandle && origChar;
    }

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original short metaclass is used.
     */
    public static boolean isOrigShort() {
        return origShortRes;
    }

    /**
     * Sets if the original short metaclass is used.
     */
    public static void setOrigShort(boolean v) {
        origShort = v;
        origShortRes = withoutCustomHandle && origShort;
    }

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original integer metaclass is used.
     */
    public static boolean isOrigInt() {
        return origIntRes;
    }

    //---------------------------------------------
    //                  double
    //---------------------------------------------

    /**
     * Sets if the original int metaclass is used.
     */
    public static void setOrigInt(boolean v) {
        origInt = v;
        origIntRes = withoutCustomHandle && origInt;
    }

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original integer array metaclass is used.
     */
    public static boolean isOrigIntArray() {
        return origIntArrayWCH;
    }

    /**
     * Sets if the original int array metaclass is used.
     */
    public static void setOrigIntArray(boolean v) {
        origIntArray = v;
        origIntArrayWCH = withoutCustomHandle && origIntArray;
    }

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original long metaclass is used.
     */
    public static boolean isOrigLong() {
        return origLongRes;
    }

    //---------------------------------------------
    //     custom metaclass creation handle
    //---------------------------------------------

    /**
     * Sets if the original long metaclass is used.
     */
    public static void setOrigLong(boolean v) {
        origLong = v;
        origLongRes = withoutCustomHandle && origLong;
    }

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original float metaclass is used.
     */
    public static boolean isOrigFloat() {
        return origFloatRes;
    }

    /**
     * Sets if the original float metaclass is used.
     */
    public static void setOrigFloat(boolean v) {
        origFloat = v;
        origFloatRes = withoutCustomHandle && origFloat;
    }

    /**
     * Returns true if no metaclass creation handle is set and if
     * the original double metaclass is used.
     */
    public static boolean isOrigDouble() {
        return origDoubleRes;
    }

    /**
     * Sets if the original double metaclass is used.
     */
    public static void setOrigDouble(boolean v) {
        origDouble = v;
        origDoubleRes = withoutCustomHandle && origDouble;
    }

    /**
     * Sets if the system uses a custom metaclass creation handle.
     */
    public static void setWithoutCustomMetaclassCreationHandle(boolean mch) {
        withoutCustomHandle = mch;
        changeFlags(mch);
    }

    public static void setCategoryUsed(boolean b) {
        categoryUsed = b;
        disabledStandardMC = b || !withoutCustomHandle;
    }

    public static boolean disabledStandardMetaClass() {
        return disabledStandardMC;
    }

    private static void changeFlags(boolean mch) {
        if (mch) {
            disabledStandardMC = true;
            origIntArrayWCH = false;
            origByteRes = origChar = origBoolean = false;
            origShortRes = origIntRes = origLong = false;
            origFloat = origDouble = false;
        } else {
            disabledStandardMC = categoryUsed;
            origByteRes = origByte;
            origCharRes = origChar;
            origBooleanRes = origBoolean;
            origShortRes = origShort;
            origIntRes = origInt;
            origLongRes = origLong;
            origFloatRes = origFloat;
            origDoubleRes = origDouble;
            origIntArrayWCH = origIntArray;
        }
    }

    public static void setPrimitiveMeta(Class c, boolean orig) {
        if (c == Byte.class) {
            setOrigByte(orig);
        } else if (c == Character.class) {
            setOrigChar(orig);
        } else if (c == Short.class) {
            setOrigShort(orig);
        } else if (c == Integer.class) {
            setOrigInt(orig);
        } else if (c.getComponentType() == Integer.class) {
            setOrigIntArray(orig);
        } else if (c == Long.class) {
            setOrigLong(orig);
        } else if (c == Float.class) {
            setOrigFloat(orig);
        } else if (c == Double.class) {
            setOrigDouble(orig);
        }

    }

    public static ConstantMetaClassVersioning getCurrentConstantMetaClassVersioning() {
        return constantMetaClassVersioning;
    }

    public static ConstantMetaClassVersioning getNewConstantMetaClassVersioning() {
        synchronized (constantMetaClassVersioningLock) {
            constantMetaClassVersioning.valid = false;
            constantMetaClassVersioning = new ConstantMetaClassVersioning();
            return constantMetaClassVersioning;
        }
    }

    //---------------------------------------------
    //         GlobalMetaClassVersioning
    //---------------------------------------------
    public static class ConstantMetaClassVersioning {
        private boolean valid = true;

        public boolean isValid() {
            return valid;
        }
    }

}
