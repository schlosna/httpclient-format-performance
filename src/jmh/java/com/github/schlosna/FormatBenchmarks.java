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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@Measurement(iterations = 5, time = 3)
@Warmup(iterations = 10, time = 3)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@SuppressWarnings("designforextension")
public class FormatBenchmarks {
    private static final int WIDTH = 10;
    private static final String PREFIX = "ex-";
    private static final String[] prefixes = generatePrefixes(PREFIX, WIDTH);

    private final AtomicLong count = new AtomicLong(9_999_000_000L);


    private static String[] generatePrefixes(String prefix, int count) {
        String[] prefixes = new String[count];
        for (int i = 0; i < prefixes.length; i++) {
            prefixes[i] = prefix + zeros(i);
        }
        return prefixes;
    }

    @Benchmark
    public String stringFormat() {
        return String.format("ex-%010d", count.incrementAndGet());
    }

    @Benchmark
    public String zeroPad() {
        return createId(count.incrementAndGet());
    }

    /**
     * Create an exchange ID.
     *
     * Hand rolled equivalent to `String.format("ex-%010d", value)` optimized to reduce
     * allocation and CPU overhead.
     */
    static String createId(long value) {
        String longString = Long.toString(value);
        return prefix(WIDTH - longString.length()) + longString;
    }

    /**
     * Hand rolled equivalent to JDK 11 `"0".repeat(count)` due to JDK 8 dependency
     */
    private static String prefix(int leadingZeros) {
        if (leadingZeros <= 0 || leadingZeros >= prefixes.length) {
            return PREFIX;
        }
        return prefixes[leadingZeros];
    }

    private static String zeros(int count) {
        char[] zeros = new char[count];
        Arrays.fill(zeros, '0');
        return new String(zeros);
    }

    public static void main(String[] _args) throws Exception {
        new Runner(new OptionsBuilder()
                        .include(FormatBenchmarks.class.getSimpleName())
                        .addProfiler(GCProfiler.class)
                        .build())
                .run();
    }
}

