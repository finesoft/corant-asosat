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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

/**
 * @author bingo 下午7:29:35
 *
 */
public class VFSUtils {

  private static FileSystemManager fileSystemManager;
  static {
    try {
      fileSystemManager = VFS.getManager();
    } catch (FileSystemException e) {
      e.printStackTrace();
    }
  }

  public static FileSystemManager getFileSystemManager() {
    return fileSystemManager;
  }

  public static void readTxtFile(String filePath, BiConsumer<String, Long> consumer) {
    AtomicLong lineNum = new AtomicLong();
    try (Stream<String> stream = new BufferedReader(new FileReader(filePath)).lines();) {
      stream.forEach(line -> consumer.accept(line, lineNum.incrementAndGet()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static void saveTxtFile(String filePath, Collection<?> lines) throws IOException {
    String lineSpr = String.valueOf((char) Character.LINE_SEPARATOR);
    try (BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(new File(filePath), true)))) {
      for (Object obj : lines) {
        writer.append(obj == null ? StringUtils.EMPTY : obj.toString());
        writer.append(lineSpr);
      }
    }
  }

}