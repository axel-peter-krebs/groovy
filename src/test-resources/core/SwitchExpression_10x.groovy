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

def a = 6
def result = switch (a) {
    case 6 -> {
        def n = 1
        yield 'a' + n
    }
    default -> {
        yield 'z'
    }
}
assert 'a1' == result

a = 8
result = switch (a) {
    case 6, 8 -> {
        def n = 1
        yield 'a' + n
    }
    default -> {
        yield 'z'
    }
}
assert 'a1' == result

a = 9
result = switch (a) {
    case 6, 8 -> {
        def n = 1
        yield 'a' + n
    }
    default -> {
        yield 'z'
    }
}
assert 'z' == result

a = 9
result = switch (a) {
    case 6, 8 -> {
        def n = 1
        yield 'a' + n
    }
    default -> yield 'z'
}
assert 'z' == result

