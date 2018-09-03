/*
 * Copyright (c) 2013-2018. BIN.CHEN
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.asosat.kernel.util;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.apache.commons.codec.binary.Base32;

/**
 * @author bingo 上午12:30:13
 */
public class IdentifierGenerators {

  public static final long TIME_EPOCH = 1_451_372_606_990L;
  static final SecureRandom SEC_RDM_INST = new SecureRandom();
  static final Map<Integer, IdentifierGenerator> SNOWFLAKE_UUID_GENERATOR =
      new ConcurrentHashMap<>();
  static final Map<Integer, IdentifierGenerator> SNOWFLAKE_BUFFRE_UUID_GENERATOR =
      new ConcurrentHashMap<>();
  static final IdentifierGenerator TIME_UUID_GENERATOR = new TimeBasedUUIDGenerator();
  static final IdentifierGenerator JAVA_UUID_GENERATOR = new JavaUUIDGenerator();
  static volatile SnowflakeUUIDGenerator lastSnowflakeUUIDGenerator;
  static volatile SnowflakeBufferUUIDGenerator lastSnowflakeBufferUUIDGenerator;

  public static String javaUUID() {
    return JAVA_UUID_GENERATOR.generate(null).toString();
  }

  public static long snowflakeBufferUUID(final int workerId, final boolean useTimeBuff,
      Supplier<Long> timeSupplier) {
    return (long) snowflakeBufferUUIDGenerator(workerId, useTimeBuff).generate(timeSupplier);
  }

  public static IdentifierGenerator snowflakeBufferUUIDGenerator(final int workerId,
      final boolean useTimeBuff) {
    if (lastSnowflakeBufferUUIDGenerator != null
        && lastSnowflakeBufferUUIDGenerator.workerId == workerId) {
      return lastSnowflakeBufferUUIDGenerator;
    } else {
      return SNOWFLAKE_BUFFRE_UUID_GENERATOR.computeIfAbsent(workerId,
          k -> lastSnowflakeBufferUUIDGenerator = new SnowflakeBufferUUIDGenerator(k, useTimeBuff));
    }
  }

  public static long snowflakeUUID(int dataCenterId, int workerId, Supplier<Long> timeSupplier) {
    return (long) snowflakeUUIDGenerator(dataCenterId, workerId).generate(timeSupplier);
  }

  public static IdentifierGenerator snowflakeUUIDGenerator(int dataCenterId, int workerId) {
    if (lastSnowflakeUUIDGenerator != null
        && lastSnowflakeUUIDGenerator.dataCenterId == dataCenterId
        && lastSnowflakeUUIDGenerator.workerId == workerId) {
      return lastSnowflakeUUIDGenerator;
    } else {
      int key = dataCenterId << SnowflakeUUIDGenerator.DATACENTER_ID_BITS | workerId;
      return SNOWFLAKE_UUID_GENERATOR.computeIfAbsent(key,
          (k) -> lastSnowflakeUUIDGenerator = new SnowflakeUUIDGenerator(dataCenterId, workerId));
    }
  }

  public static String timeBaseUUID(Supplier<Long> timeSupplier) {
    return (String) TIME_UUID_GENERATOR.generate(timeSupplier);
  }

  static Long getCurrentTimestamp(Supplier<?> timeGener) {
    return timeGener == null ? System.currentTimeMillis() : ((Long) timeGener.get());
  }

  static long tilMillis(Supplier<?> timeGener, long lastTimestamp, boolean allowEq) {
    long timestamp = getCurrentTimestamp(timeGener);
    if (timestamp <= lastTimestamp) {
      while (allowEq ? timestamp < lastTimestamp : timestamp <= lastTimestamp) {
        timestamp = getCurrentTimestamp(timeGener);
      }
      return timestamp;
    } else {
      return timestamp;
    }
  }

  public static interface IdentifierGenerator {
    Serializable generate(Supplier<?> suppler);
  }

  public static class JavaUUIDGenerator implements IdentifierGenerator {
    @Override
    public Serializable generate(Supplier<?> suppler) {
      return UUID.randomUUID().toString();
    }
  }

  public static class SnowflakeBufferUUIDGenerator implements IdentifierGenerator {

    public static final long WORKER_ID_BITS = 10;// 支持1024个进程
    public static final long MAX_WORKER_ID = -1L ^ -1L << WORKER_ID_BITS;
    public static final long SEQUENCE_BITS = 12L;
    public static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    public static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    public static final long SEQUENCE_MASK = -1L ^ -1L << SEQUENCE_BITS;

    private final long workerId;
    private final long workerSegm;
    private final boolean useTimeBuffer;
    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    public SnowflakeBufferUUIDGenerator(long workerId) {
      this(workerId, false);
    }

    public SnowflakeBufferUUIDGenerator(long workerId, boolean useTimeBuffer) {
      if (workerId < 0 || workerId > MAX_WORKER_ID) {
        throw new IllegalArgumentException(
            "Worker id is illegal: " + workerId + " [0," + MAX_WORKER_ID + "]");
      }
      this.workerId = workerId;
      this.workerSegm = workerId << WORKER_ID_SHIFT;
      this.useTimeBuffer = useTimeBuffer;
    }

    /**
     * 解析id获得时间戳
     *
     * @param id
     * @return
     */
    public static Instant parseGeningInstant(long id) {
      long timestamp = id >>> TIMESTAMP_LEFT_SHIFT;
      return Instant.ofEpochMilli(timestamp + TIME_EPOCH);
    }

    /**
     * 解析顺序号
     *
     * @param id
     * @return
     */
    public static long parseGeningSequence(long id) {
      long tmp = id << 64 - TIMESTAMP_LEFT_SHIFT + WORKER_ID_BITS;
      tmp >>>= 64 - SEQUENCE_BITS;
      return tmp;
    }

    /**
     * 解析id获得工作进程id
     *
     * @param id
     * @return
     */
    public static long parseGeningWorkerId(long id) {
      long tmp = id << 64 - TIMESTAMP_LEFT_SHIFT;
      tmp >>>= 64 - TIMESTAMP_LEFT_SHIFT + SEQUENCE_BITS;
      return tmp;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      SnowflakeBufferUUIDGenerator other = (SnowflakeBufferUUIDGenerator) obj;
      if (this.workerId != other.workerId) {
        return false;
      }
      return true;
    }

    @Override
    public Long generate(Supplier<?> timeGener) {
      if (this.useTimeBuffer) {
        return this.doGenerateWithCache(timeGener);
      } else {
        return this.doGenerateWithoutCace(timeGener);
      }
    }

    public long getWorkerId() {
      return this.workerId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (this.workerId ^ (this.workerId >>> 32));
      return result;
    }

    protected synchronized Long doGenerateWithCache(Supplier<?> timeGener) {
      int cursor = (int) this.sequence;
      this.sequence++;
      this.sequence &= SEQUENCE_MASK;
      if (cursor == 0) {
        this.lastTimestamp = tilMillis(timeGener, this.lastTimestamp, false);
      }
      // System.out.println(
      // this.workerId + "\t" + this.lastTimestamp + "\t" + this.sequence + "\t" + cursor);
      return this.nextId(this.lastTimestamp, cursor);
    }

    protected synchronized Long doGenerateWithoutCace(Supplier<?> timeGener) {
      long timestamp = tilMillis(timeGener, this.lastTimestamp, true);
      if (this.lastTimestamp == timestamp) {
        this.sequence++;
        this.sequence &= SEQUENCE_MASK;
        if (this.sequence == 0) {
          timestamp = tilMillis(timeGener, this.lastTimestamp, false);
        }
      } else {
        this.sequence = 0;
      }
      // System.out.println(this.workerId + "\t" + timestamp + "\t" + this.sequence);
      this.lastTimestamp = timestamp;
      return this.nextId(timestamp, this.sequence);
    }

    protected long nextId(long timestamp, long seq) {
      return timestamp - TIME_EPOCH << TIMESTAMP_LEFT_SHIFT | this.workerSegm | seq;
    }
  }

  /**
   * 根据Twitter的算法实现的id生成器，同一个应用实例内只能单例使用。 当前的实现假设，所有进程依赖同一个数据库实例，因此依赖数据库授时；如果不是则需另外实现。 <br/>
   * 1~41 为当前时间至1451372606990L（2015-12-29 15:15:???）的时间差（毫秒） <br/>
   * 42~46为子系统或数据中心编号，2^5即从0~31 <br/>
   * 47~51为进程编号，2^5即从0~31 <br/>
   * 52~63为每个时间毫秒内的顺序号，2^12即从0~4095号，共12位 <br/>
   * 整体表现：同一毫秒内允许1024个进程进行id生成，每个进程可生成4096个顺序id <br/>
   * 注意不可用日期为 ：<b>2085-09-04T06:51:02.541+08:00[Asia/Shanghai]</b>
   * 如果有人在那天遇到该问题，如果long还是只有64位的话，请换掉它！
   *
   * @author bingo 2016年3月9日
   * @since
   */
  public static class SnowflakeUUIDGenerator implements IdentifierGenerator {

    public static final long WORKER_ID_BITS = 5L;
    public static final long DATACENTER_ID_BITS = 5L;
    public static final long MAX_WORKER_ID = -1L ^ -1L << WORKER_ID_BITS;
    public static final long MAX_DATACENTER_ID = -1L ^ -1L << DATACENTER_ID_BITS;

    public static final long SEQUENCE_BITS = 12L;
    public static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    public static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    public static final long TIMESTAMP_LEFT_SHIFT =
        SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    public static final long SEQUENCE_MASK = -1L ^ -1L << SEQUENCE_BITS;


    private final long workerId;
    private final long dataCenterId;
    private final long dataCenterSegm;
    private final long workerSegm;
    private volatile long lastTimestamp = -1L;
    private volatile long sequence;


    public SnowflakeUUIDGenerator(long dataCenterId, long workerId) {
      if (workerId < 0 || workerId > MAX_WORKER_ID) {
        throw new IllegalArgumentException(
            "Worker id is illegal: " + workerId + " [0," + MAX_WORKER_ID + "]");
      }
      if (dataCenterId < 0 || dataCenterId > MAX_DATACENTER_ID) {
        throw new IllegalArgumentException(
            "Data center id is illegal: " + dataCenterId + " [0," + MAX_DATACENTER_ID + "]");
      }
      if (TIME_EPOCH >= System.currentTimeMillis()) {
        throw new IllegalArgumentException("Id epoch is illegal: " + TIME_EPOCH);
      }
      this.dataCenterId = dataCenterId;
      this.workerId = workerId;
      this.dataCenterSegm = dataCenterId << DATACENTER_ID_SHIFT;
      this.workerSegm = workerId << WORKER_ID_SHIFT;
      this.sequence = 0;
    }

    /**
     * 解析id获得数据中心或子系统id
     *
     * @param id
     * @return
     */
    public static long parseGeningDataCenterId(long id) {
      long tmp = id << 64 - TIMESTAMP_LEFT_SHIFT;
      tmp >>>= 64 - TIMESTAMP_LEFT_SHIFT;
      tmp >>>= DATACENTER_ID_SHIFT;
      return tmp;
    }

    /**
     * 解析id获得时间戳
     *
     * @param id
     * @return
     */
    public static Instant parseGeningInstant(long id) {
      long timestamp = id >>> TIMESTAMP_LEFT_SHIFT;
      return Instant.ofEpochMilli(timestamp + TIME_EPOCH);
    }

    /**
     * 解析顺序号
     *
     * @param id
     * @return
     */
    public static long parseGeningSequence(long id) {
      long tmp = id << 64 - TIMESTAMP_LEFT_SHIFT + DATACENTER_ID_BITS + WORKER_ID_BITS;
      tmp >>>= 64 - SEQUENCE_BITS;
      return tmp;
    }

    /**
     * 解析id获得工作进程id
     *
     * @param id
     * @return
     */
    public static long parseGeningWorkerId(long id) {
      long tmp = id << 64 - TIMESTAMP_LEFT_SHIFT + DATACENTER_ID_BITS;
      tmp >>>= 64 - TIMESTAMP_LEFT_SHIFT + DATACENTER_ID_BITS;
      tmp >>>= SEQUENCE_BITS;
      return tmp;
    }


    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      SnowflakeUUIDGenerator other = (SnowflakeUUIDGenerator) obj;
      if (this.dataCenterId != other.dataCenterId) {
        return false;
      }
      if (this.workerId != other.workerId) {
        return false;
      }
      return true;
    }

    @Override
    public synchronized Long generate(Supplier<?> timeGener) {
      long timestamp = tilMillis(timeGener, this.lastTimestamp, true);
      if (this.lastTimestamp == timestamp) {
        this.sequence++;
        this.sequence &= SEQUENCE_MASK;
        if (this.sequence == 0) {
          timestamp = tilMillis(timeGener, this.lastTimestamp, false);
        }
      } else {
        this.sequence = 0;
      }
      // System.out.println(this.workerId + "\t" + timestamp + "\t" + this.sequence);
      this.lastTimestamp = timestamp;
      return timestamp - TIME_EPOCH << TIMESTAMP_LEFT_SHIFT | this.dataCenterSegm | this.workerSegm
          | this.sequence;
    }

    /**
     * 子系统/数据中心id
     *
     * @return
     */
    public long getDataCenterId() {
      return this.dataCenterId;
    }

    /**
     * 死亡时间
     *
     * @return
     */
    public Instant getDeathTime() {
      return Instant.ofEpochMilli((Long.MAX_VALUE >>> 22) + TIME_EPOCH);
    }

    /**
     * 最后时间戳
     *
     * @return
     */
    public synchronized long getLastTimestamp() {
      return this.lastTimestamp;
    }

    /**
     * 进程Id
     *
     * @return
     */
    public long getWorkerId() {
      return this.workerId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (this.dataCenterId ^ (this.dataCenterId >>> 32));
      result = prime * result + (int) (this.workerId ^ (this.workerId >>> 32));
      return result;
    }

  }

  public static class TimeBasedUUIDGenerator implements IdentifierGenerator {

    private static final byte[] SECURE_MUNGED_ADDRESS =
        MacAddrUtils.getSecureMungedAddress(SEC_RDM_INST);

    static {
      assert SECURE_MUNGED_ADDRESS.length == 6;
    }

    private final AtomicInteger sequence = new AtomicInteger(SEC_RDM_INST.nextInt());

    private long lastTimestamp;

    private static void putLong(byte[] array, long l, int pos, int numberOfLongBytes) {
      for (int i = 0; i < numberOfLongBytes; ++i) {
        array[pos + numberOfLongBytes - i - 1] = (byte) (l >>> (i << 3));
      }
    }

    @Override
    public String generate(Supplier<?> suppler) {
      final int sequenceId = this.sequence.incrementAndGet() & 0xffffff;
      long timestamp = (Long) suppler.get();

      synchronized (this) {
        timestamp = Math.max(this.lastTimestamp, timestamp);

        if (sequenceId == 0) {
          timestamp++;
        }

        this.lastTimestamp = timestamp;
      }

      final byte[] uuidBytes = new byte[15];

      putLong(uuidBytes, timestamp, 0, 6);

      System.arraycopy(SECURE_MUNGED_ADDRESS, 0, uuidBytes, 6, SECURE_MUNGED_ADDRESS.length);

      putLong(uuidBytes, sequenceId, 12, 3);

      assert 9 + SECURE_MUNGED_ADDRESS.length == uuidBytes.length;

      return new Base32((byte) 0x5F).encodeAsString(uuidBytes);
    }
  }


}
