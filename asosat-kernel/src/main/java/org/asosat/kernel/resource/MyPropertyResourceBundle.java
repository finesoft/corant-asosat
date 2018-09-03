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
package org.asosat.kernel.resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.asosat.kernel.context.DefaultSetting;

/**
 * @author bingo 上午12:25:48
 *
 */
public class MyPropertyResourceBundle extends ResourceBundle {

  public static final String LOCALE_SPT = "_";
  private Map<String, Object> lookup;
  private long lastModifiedTime;
  private Locale locale;
  private String baseBundleName;

  @SuppressWarnings({"rawtypes", "unchecked"})
  public MyPropertyResourceBundle(FileObject fo) throws IOException {
    this.baseBundleName = fo.getName().getBaseName();
    this.locale = MyPropertyResourceBundle.detectLocaleByName(this.baseBundleName);
    this.lastModifiedTime = fo.getContent().getLastModifiedTime();
    Properties properties = new Properties();
    properties
        .load(new InputStreamReader(fo.getContent().getInputStream(), DefaultSetting.CHARSET));
    this.lookup = new HashMap(properties);
  }

  public static Map<String, MyPropertyResourceBundle> getBundles(FileSelector fs) {
    Map<String, MyPropertyResourceBundle> map = new ConcurrentHashMap<>();
    MultiClassPathFiles.select(fs).forEach((s, fo) -> {
      try {
        map.putIfAbsent(s, new MyPropertyResourceBundle(fo));
      } catch (IOException e) {
      }
    });
    return map;
  }

  protected static Locale detectLocaleByName(String name) {
    int f = name != null ? name.indexOf(LOCALE_SPT) : -1;
    if (f > 0) {
      return LocaleUtils.toLocale(FilenameUtils.getBaseName(name.substring(f + 1)));
    } else {
      return DefaultSetting.LOCALE;
    }
  }

  public Map<String, String> dump() {
    Map<String, String> map = new HashMap<>();
    Enumeration<String> msgKeys = this.getKeys();
    while (msgKeys.hasMoreElements()) {
      String msgKey = msgKeys.nextElement();
      String mfv = this.getString(msgKey);
      if (mfv != null) {
        map.put(msgKey, mfv);
      }
    }
    return map;
  }

  @Override
  public String getBaseBundleName() {
    return this.baseBundleName;
  }

  @Override
  public Enumeration<String> getKeys() {
    ResourceBundle parent = this.parent;
    return new MyResourceBundleEnumeration(this.lookup.keySet(),
        (parent != null) ? parent.getKeys() : null);
  }

  public long getLastModifiedTime() {
    return this.lastModifiedTime;
  }

  @Override
  public Locale getLocale() {
    return this.locale;
  }

  @Override
  protected Object handleGetObject(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return this.lookup.get(key);
  }

  public static class MyResourceBundleEnumeration implements Enumeration<String> {

    Set<String> set;
    Iterator<String> iterator;
    Enumeration<String> enumeration; // may remain null

    String next = null;

    /**
     * Constructs a resource bundle enumeration.
     *
     * @param set an set providing some elements of the enumeration
     * @param enumeration an enumeration providing more elements of the enumeration. enumeration may
     *        be null.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public MyResourceBundleEnumeration(Set<String> set, Enumeration enumeration) {
      this.set = set;
      this.iterator = set.iterator();
      this.enumeration = enumeration;
    }

    @Override
    public boolean hasMoreElements() {
      if (this.next == null) {
        if (this.iterator.hasNext()) {
          this.next = this.iterator.next();
        } else if (this.enumeration != null) {
          while (this.next == null && this.enumeration.hasMoreElements()) {
            this.next = this.enumeration.nextElement();
            if (this.set.contains(this.next)) {
              this.next = null;
            }
          }
        }
      }
      return this.next != null;
    }

    @Override
    public String nextElement() {
      if (this.hasMoreElements()) {
        String result = this.next;
        this.next = null;
        return result;
      } else {
        throw new NoSuchElementException();
      }
    }
  }
}