/*
 * Copyright 2015-2026 Open Text.
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
package com.github.cafdataprocessing.workers.languagedetection.tracing;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class HttpBodyTracingFilter implements Filter
{
    private static final int MAX_BODY_BYTES = 4096;

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException
    {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        final CachedBodyHttpServletRequest requestWrapper = new CachedBodyHttpServletRequest((HttpServletRequest) request);
        final CachingHttpServletResponseWrapper responseWrapper = new CachingHttpServletResponseWrapper((HttpServletResponse) response);

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            final Span currentSpan = Span.current();
            if (currentSpan.getSpanContext().isValid()) {
                setBodyAttribute(currentSpan, "http.request.body", requestWrapper.getCachedBody(), requestWrapper.getCharacterEncoding());
                setBodyAttribute(currentSpan, "http.response.body", responseWrapper.getCachedBody(), responseWrapper.getCharacterEncoding());
            }
        }
    }

    private static void setBodyAttribute(final Span span, final String attributeName, final byte[] body, final String charsetName)
    {
        if (body.length == 0) {
            return;
        }

        final Charset charset = resolveCharset(charsetName);
        final String truncatedBody = new String(body, 0, Math.min(body.length, MAX_BODY_BYTES), charset);
        span.setAttribute(attributeName, truncatedBody);
    }

    private static Charset resolveCharset(final String charsetName)
    {
        if (charsetName == null) {
            return StandardCharsets.UTF_8;
        }

        try {
            return Charset.forName(charsetName);
        } catch (IllegalArgumentException ex) {
            return StandardCharsets.UTF_8;
        }
    }

    private static final class CachedBodyHttpServletRequest extends HttpServletRequestWrapper
    {
        private final byte[] cachedBody;

        private CachedBodyHttpServletRequest(final HttpServletRequest request) throws IOException
        {
            super(request);
            cachedBody = request.getInputStream().readAllBytes();
        }

        private byte[] getCachedBody()
        {
            return cachedBody;
        }

        @Override
        public ServletInputStream getInputStream()
        {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream()
            {
                @Override
                public int read()
                {
                    return byteArrayInputStream.read();
                }

                @Override
                public boolean isFinished()
                {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady()
                {
                    return true;
                }

                @Override
                public void setReadListener(final ReadListener readListener)
                {
                    throw new UnsupportedOperationException("Async reads are not supported");
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException
        {
            final Charset charset = resolveCharset(getCharacterEncoding());
            return new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }
    }

    private static final class CachingHttpServletResponseWrapper extends HttpServletResponseWrapper
    {
        private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
        private ServletOutputStream outputStream;
        private PrintWriter writer;

        private CachingHttpServletResponseWrapper(final HttpServletResponse response)
        {
            super(response);
        }

        private byte[] getCachedBody()
        {
            if (writer != null) {
                writer.flush();
            }
            return cachedBody.toByteArray();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException
        {
            if (writer != null) {
                throw new IllegalStateException("getWriter() has already been called on this response.");
            }

            if (outputStream == null) {
                outputStream = new TeeServletOutputStream(super.getOutputStream(), cachedBody);
            }
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException
        {
            if (outputStream != null) {
                throw new IllegalStateException("getOutputStream() has already been called on this response.");
            }

            if (writer == null) {
                final Charset charset = resolveCharset(getCharacterEncoding());
                writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), charset), true);
            }
            return writer;
        }
    }

    private static final class TeeServletOutputStream extends ServletOutputStream
    {
        private final ServletOutputStream delegate;
        private final ByteArrayOutputStream copy;

        private TeeServletOutputStream(final ServletOutputStream delegate, final ByteArrayOutputStream copy)
        {
            this.delegate = delegate;
            this.copy = copy;
        }

        @Override
        public void write(final int value) throws IOException
        {
            delegate.write(value);
            copy.write(value);
        }

        @Override
        public void write(final byte[] bytes, final int offset, final int length) throws IOException
        {
            delegate.write(bytes, offset, length);
            copy.write(bytes, offset, length);
        }

        @Override
        public boolean isReady()
        {
            return delegate.isReady();
        }

        @Override
        public void setWriteListener(final WriteListener writeListener)
        {
            delegate.setWriteListener(writeListener);
        }
    }
}
