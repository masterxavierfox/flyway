/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.util.scanner.classpath;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.BomStrippingReader;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.line.DefaultLineReader;
import org.flywaydb.core.internal.util.line.LineReader;
import org.flywaydb.core.internal.util.scanner.AbstractLoadableResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;

/**
 * A resource on the classpath.
 */
public class ClassPathResource extends AbstractLoadableResource implements Comparable<ClassPathResource> {
    /**
     * The location of the resource on the classpath.
     */
    private final String location;

    /**
     * The ClassLoader to use.
     */
    private final ClassLoader classLoader;
    private final Charset encoding;

    /**
     * Creates a new ClassPathResource.
     *
     * @param location    The location of the resource on the classpath.
     * @param classLoader The ClassLoader to use.
     */
    public ClassPathResource(String location, ClassLoader classLoader, Charset encoding) {
        this.location = location;
        this.classLoader = classLoader;
        this.encoding = encoding;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getLocationOnDisk() {
        URL url = getUrl();
        if (url == null) {
            throw new FlywayException("Unable to location resource on disk: " + location);
        }
        try {
            return new File(URLDecoder.decode(url.getPath(), "UTF-8")).getAbsolutePath();
        } catch (UnsupportedEncodingException e) {
            throw new FlywayException("Unknown encoding: UTF-8", e);
        }
    }

    /**
     * @return The url of this resource.
     */
    private URL getUrl() {
        return classLoader.getResource(location);
    }

    @Override
    public LineReader loadAsString() {
        try {
            InputStream inputStream = classLoader.getResourceAsStream(location);
            if (inputStream == null) {
                throw new FlywayException("Unable to obtain inputstream for resource: " + location);
            }
            return new DefaultLineReader(new BomStrippingReader(new InputStreamReader(inputStream, encoding)));
        } catch (IOException e) {
            throw new FlywayException("Unable to load resource: " + location + " (encoding: " + encoding + ")", e);
        }
    }

    @Override
    public byte[] loadAsBytes() {
        try {
            InputStream inputStream = classLoader.getResourceAsStream(location);
            if (inputStream == null) {
                throw new FlywayException("Unable to obtain inputstream for resource: " + location);
            }
            return FileCopyUtils.copyToByteArray(inputStream);
        } catch (IOException e) {
            throw new FlywayException("Unable to load resource: " + location, e);
        }
    }

    @Override
    public String getFilename() {
        return location.substring(location.lastIndexOf("/") + 1);
    }

    public boolean exists() {
        return getUrl() != null;
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassPathResource that = (ClassPathResource) o;

        if (!location.equals(that.location)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(ClassPathResource o) {
        return location.compareTo(o.location);
    }
}