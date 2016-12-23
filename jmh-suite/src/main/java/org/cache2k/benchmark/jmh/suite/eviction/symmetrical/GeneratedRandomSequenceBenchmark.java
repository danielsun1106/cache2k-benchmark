package org.cache2k.benchmark.jmh.suite.eviction.symmetrical;

/*
 * #%L
 * Benchmarks: JMH suite.
 * %%
 * Copyright (C) 2013 - 2016 headissue GmbH, Munich
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unimi.dsi.util.XorShift1024StarRandomGenerator;
import org.cache2k.benchmark.BenchmarkCache;
import org.cache2k.benchmark.jmh.BenchmarkBase;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Random;

/**
 * Penetrate a 100k entry cache with a random pattern that produces a
 * specified hitrate. Use un-looped random sequence with XorShift1024StarRandom.
 *
 * @author Jens Wilke
 */
@State(Scope.Benchmark)
public class GeneratedRandomSequenceBenchmark extends BenchmarkBase {

  public static final int ENTRY_COUNT = 100_000;
  public static final int MINI_PERCENT_HITRATE = 30;
  public static final int INTEGER_SPACE_COUNT = 100 * ENTRY_COUNT / (MINI_PERCENT_HITRATE - 1);

  @Param({"30", "50", "80", "90", "95"})
  public int hitRate = 0;

  /** Use thread safe RPNG to give each thread state another seed. */
  private final static Random offsetSeed = new Random(1802);

  private int range;
  private Integer[] keySpace;

  @State(Scope.Thread)
  public static class ThreadState {
    XorShift1024StarRandomGenerator generator = new XorShift1024StarRandomGenerator(offsetSeed.nextLong());
  }

  BenchmarkCache<Integer, Integer> cache;

  @Setup(Level.Iteration)
  public void setup() throws Exception {
    getsDestroyed = cache = getFactory().create(ENTRY_COUNT);
    range = (int) (ENTRY_COUNT * (100D / hitRate));
    keySpace = new Integer[INTEGER_SPACE_COUNT];
    for (int i = 0; i < INTEGER_SPACE_COUNT; i++) {
      keySpace[i] = i;
    }
  }

  @Benchmark @BenchmarkMode(Mode.Throughput)
  public long operation(ThreadState threadState, HitCountRecorder rec) {
    Integer k = keySpace[threadState.generator.nextInt(range)];
    Integer v = cache.getIfPresent(k);
    if (v == null) {
      cache.put(k, k);
      rec.missCount++;
    } else {
      rec.hitCount++;
    }
    return k;
  }

}
