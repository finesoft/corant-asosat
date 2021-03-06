package org.asosat.ddd.util;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableMap;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.corant.shared.util.Assertions.shouldNotNull;
import static org.corant.shared.util.Objects.defaultObject;
import static org.corant.shared.util.Strings.isNotBlank;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.corant.shared.util.Resources.Resource;
import org.corant.shared.util.Resources.SourceType;
import org.corant.suites.servlet.abstraction.ContentDispositions;
import org.corant.suites.servlet.abstraction.ContentDispositions.ContentDisposition;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

/**
 * resteasy InputPart resource
 * @author don
 * @date 2019-09-26
 */
public class InputPartResource implements Resource {

  private InputPart inputPart;

  private String filename;

  private Map<String, Object> metaData;

  public InputPartResource(InputPart inputPart) {
    this.inputPart = shouldNotNull(inputPart);
    ContentDisposition disposition = ContentDispositions.parse(inputPart.getHeaders().getFirst(CONTENT_DISPOSITION));
    String filename = disposition.getFilename();
    if (disposition.getCharset() == null && isNotBlank(filename)) {
      // 因为apache mime4j 解析浏览器提交的文件名按ISO_8859_1处理
      // 上传文件断点ContentUtil.decode(ByteSequence byteSequence, int offset, int length)
      filename = new String(filename.getBytes(ISO_8859_1), UTF_8);
    }
    this.filename = defaultObject(filename, () -> "unnamed-" + UUID.randomUUID());
    this.metaData = new HashMap<>();
    this.metaData.put(CONTENT_TYPE, getContentType());
  }

  public String getContentType() {
    return inputPart.getMediaType().toString();
  }

  public void addMetadata(String key, Object value) {
    this.metaData.put(key, value);
  }

  @Override
  public Map<String, Object> getMetadata() {
    return unmodifiableMap(metaData);
  }

  @Override
  public String getName() {
    return filename;
  }

  @Override
  public InputStream openStream() throws IOException {
    return inputPart.getBody(InputStream.class, null);
  }

  @Override
  public SourceType getSourceType() {
    return null;
  }

  @Override
  public String getLocation() {
    return getName();
  }
}
