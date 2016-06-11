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

package org.apache.hc.core5.http.protocol;

import java.io.IOException;
import java.util.Set;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HeaderElements;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.Args;

/**
 * RequestContent is the most important interceptor for outgoing requests.
 * It is responsible for delimiting content length by adding
 * {@code Content-Length} or {@code Transfer-Content} headers based
 * on the properties of the enclosed entity and the protocol version.
 * This interceptor is required for correct functioning of client side protocol
 * processors.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class RequestContent implements HttpRequestInterceptor {

    private final boolean overwrite;

    /**
     * Default constructor. The {@code Content-Length} or {@code Transfer-Encoding}
     * will cause the interceptor to throw {@link ProtocolException} if already present in the
     * response message.
     */
    public RequestContent() {
        this(false);
    }

    /**
     * Constructor that can be used to fine-tune behavior of this interceptor.
     *
     * @param overwrite If set to {@code true} the {@code Content-Length} and
     * {@code Transfer-Encoding} headers will be created or updated if already present.
     * If set to {@code false} the {@code Content-Length} and
     * {@code Transfer-Encoding} headers will cause the interceptor to throw
     * {@link ProtocolException} if already present in the response message.
     *
     * @since 4.2
     */
     public RequestContent(final boolean overwrite) {
         super();
         this.overwrite = overwrite;
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context)
            throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        if (this.overwrite) {
            request.removeHeaders(HttpHeaders.TRANSFER_ENCODING);
            request.removeHeaders(HttpHeaders.CONTENT_LENGTH);
        } else {
            if (request.containsHeader(HttpHeaders.TRANSFER_ENCODING)) {
                throw new ProtocolException("Transfer-encoding header already present");
            }
            if (request.containsHeader(HttpHeaders.CONTENT_LENGTH)) {
                throw new ProtocolException("Content-Length header already present");
            }
        }
        final HttpEntity entity = request.getEntity();
        if (entity != null) {
            final ProtocolVersion ver = context.getProtocolVersion();
            // Must specify a transfer encoding or a content length
            if (entity.isChunked() || entity.getContentLength() < 0) {
                if (ver.lessEquals(HttpVersion.HTTP_1_0)) {
                    throw new ProtocolException(
                            "Chunked transfer encoding not allowed for " + ver);
                }
                request.addHeader(HttpHeaders.TRANSFER_ENCODING, HeaderElements.CHUNKED_ENCODING);
                final Set<String> trailerNames = entity.getTrailerNames();
                if (trailerNames != null && !trailerNames.isEmpty()) {
                    request.setHeader(TrailerNameFormatter.format(entity));
                }
            } else {
                request.addHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(entity.getContentLength()));
            }
            // Specify a content type if known
            if (entity.getContentType() != null && !request.containsHeader(HttpHeaders.CONTENT_TYPE)) {
                request.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, entity.getContentType()));
            }
            // Specify a content encoding if known
            if (entity.getContentEncoding() != null && !request.containsHeader(HttpHeaders.CONTENT_ENCODING)) {
                request.addHeader(new BasicHeader(HttpHeaders.CONTENT_ENCODING, entity.getContentEncoding()));
            }
        }
    }

}