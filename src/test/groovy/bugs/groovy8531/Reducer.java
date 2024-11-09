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
package groovy.bugs.groovy8531;

interface Reducable {
    class InterfaceContext {
    }
}

class BaseReducer {
    public static abstract class PublicStaticBaseContext {
    }

    protected static abstract class ProtectedStaticBaseContext {
    }

    /*package*/ static abstract class PackagePrivateStaticBaseContext {
    }

    public abstract class PublicBaseContext {
    }

    protected abstract class ProtectedBaseContext {
    }

    /*package*/ abstract class PackagePrivateBaseContext {
    }

    private abstract class PrivateBaseContext {
    }
}

public class Reducer extends BaseReducer implements Reducable {
    public enum Type {DYNAMIC, STATIC}

    public static abstract class PublicStaticContext {
    }

    protected static abstract class ProtectedStaticContext {
    }

    /*package*/ static abstract class PackagePrivateStaticContext {
    }

    public abstract class PublicContext {
    }

    protected abstract class ProtectedContext {
    }

    /*package*/ abstract class PackagePrivateContext {
    }

    private abstract class PrivateContext {
    }
}
