/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package com.github.schlosna;

import org.apache.hc.core5.net.InetAddressUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@State(Scope.Thread)
@Measurement(iterations = 2, time = 5)
@Warmup(iterations = 1, time = 5)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@SuppressWarnings("designforextension")
public class Ipv6FormatBenchmarks {

    @Param({"127.0.0.1", "0:0:0:0:0:0:0:1"})
    String address;

    @Benchmark
    public boolean isIPv6Address_original() {
        return InetAddressUtils.isIPv6Address(address);
    }

    @Benchmark
    public boolean isIPv6Address_improved() {
        return Improved.isIPv6Address(address);
    }

    static class Improved {

        private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){7}$");

        private static final Pattern IPV6_HEX_COMPRESSED_PATTERN =
                Pattern.compile("^(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)" + // 0-6 hex fields
                        "::" + "(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)$"); // 0-6 hex fields

        /*
         *  The above pattern is not totally rigorous as it allows for more than 7 hex fields in total
         */
        private static final char COLON_CHAR = ':';

        // Must not have more than 7 colons (i.e. 8 fields)
        private static final int MAX_COLON_COUNT = 7;

        static boolean hasValidIPv6ColonCount(final String input) {
            int colonCount = 0;
            for (int i = 0; i < input.length(); i++) {
                if (input.charAt(i) == COLON_CHAR) {
                    colonCount++;
                }
            }

            // IPv6 address must have at least 2 colons and not more than 7 (i.e. 8 fields)
            return colonCount >= 2 && colonCount <= MAX_COLON_COUNT;
        }
        /**
         * Checks whether the parameter is a valid standard (non-compressed) IPv6 address
         *
         * @param input the address string to check for validity
         * @return true if the input parameter is a valid standard (non-compressed) IPv6 address
         */
        public static boolean isIPv6StdAddress(final String input) {
            return hasValidIPv6ColonCount(input)
                    && IPV6_STD_PATTERN.matcher(input).matches();
        }

        /**
         * Checks whether the parameter is a valid compressed IPv6 address
         *
         * @param input the address string to check for validity
         * @return true if the input parameter is a valid compressed IPv6 address
         */
        public static boolean isIPv6HexCompressedAddress(final String input) {
            return hasValidIPv6ColonCount(input)
                    && IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
        }

        /**
         * Checks whether the parameter is a valid IPv6 address (including compressed).
         *
         * @param input the address string to check for validity
         * @return true if the input parameter is a valid standard or compressed IPv6 address
         */
        public static boolean isIPv6Address(final String input) {
            return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
        }
    }

    public static void main(String[] _args) throws Exception {
        new Runner(new OptionsBuilder()
                        .include(Ipv6FormatBenchmarks.class.getSimpleName())
                        .build())
                .run();
    }
}
