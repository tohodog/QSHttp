/*
 * Copyright (C) 2014 Square, Inc.
 *
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
 */
package okhttp3;

import org.song.http.framework.HttpEnum;
import org.song.http.framework.util.Utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okio.Buffer;
import okio.BufferedSink;

/**
 * okhttp写死了CONTENT_TYPE
 * 这个自定义支持其他编码
 */
public final class FormBody2 extends RequestBody {

    /**
     * 如果post form需要自定义编码,把这个设置为true
     */
    public static boolean formbodyCharset = false;

    private final MediaType CONTENT_TYPE;
    private final List<String> encodedNames;
    private final List<String> encodedValues;
    private final String charset;

    private FormBody2(List<String> encodedNames, List<String> encodedValues, String charset) {
        this.encodedNames = Collections.unmodifiableList(encodedNames);
        this.encodedValues = Collections.unmodifiableList(encodedValues);
        this.charset = charset;
        this.CONTENT_TYPE = MediaType.parse(HttpEnum.CONTENT_TYPE_URL_ + charset);
    }

    /**
     * The number of key-value pairs in this form-encoded body.
     */
    public int size() {
        return encodedNames.size();
    }

    public String encodedName(int index) {
        return encodedNames.get(index);
    }

    public String name(int index) {
        return Utils.URLDecoder(encodedName(index), charset);
    }

    public String encodedValue(int index) {
        return encodedValues.get(index);
    }

    public String value(int index) {
        return Utils.URLDecoder(encodedValue(index), charset);
    }

    @Override
    public MediaType contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public long contentLength() {
        return writeOrCountBytes(null, true);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        writeOrCountBytes(sink, false);
    }

    /**
     * Either writes this request to {@code sink} or measures its content length. We have one method
     * do double-duty to make sure the counting and content are consistent, particularly when it comes
     * to awkward operations like measuring the encoded length of header strings, or the
     * length-in-digits of an encoded integer.
     */
    private long writeOrCountBytes(BufferedSink sink, boolean countBytes) {
        long byteCount = 0L;

        Buffer buffer;
        if (countBytes) {
            buffer = new Buffer();
        } else {
            buffer = sink.buffer();
        }

        for (int i = 0, size = encodedNames.size(); i < size; i++) {
            if (i > 0) buffer.writeByte('&');
            buffer.writeUtf8(encodedNames.get(i));
            buffer.writeByte('=');
            buffer.writeUtf8(encodedValues.get(i));
        }

        if (countBytes) {
            byteCount = buffer.size();
            buffer.clear();
        }

        return byteCount;
    }

    public static final class Builder {
        private final List<String> names = new ArrayList<>();
        private final List<String> values = new ArrayList<>();
        private final String charset;

        public Builder() {
            this.charset = Charset.defaultCharset().name();
        }

        public Builder(String charset) {
            this.charset = charset;
        }

        public Builder add(String name, String value) {
            if (name == null) throw new NullPointerException("name == null");
            if (value == null) throw new NullPointerException("value == null");

            names.add(Utils.URLEncoder(name, charset));
            values.add(Utils.URLEncoder(value, charset));
            return this;
        }

        public Builder addEncoded(String name, String value) {
            if (name == null) throw new NullPointerException("name == null");
            if (value == null) throw new NullPointerException("value == null");

            names.add(name);
            values.add(value);
            return this;
        }

        public FormBody2 build() {
            return new FormBody2(names, values, charset);
        }
    }
}
