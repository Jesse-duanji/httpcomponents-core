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
package org.apache.hc.core5.http2;

import java.io.IOException;

import org.apache.hc.core5.util.Args;

public class H2StreamException extends IOException {

    private static final long serialVersionUID = 6321637486572232180L;

    private final int code;
    private final long streamId;

    public H2StreamException(final H2Error error, final long streamId, final String message) {
        super(message);
        Args.notNull(error, "H2 Error code may not be null");
        Args.positive(streamId, "H2 stream id may not be negative or zero");
        this.code = error.getCode();
        this.streamId = streamId;
    }

    public H2StreamException(final int code, final long streamId, final String message) {
        super(message);
        Args.positive(streamId, "H2 stream id may not be negative or zero");
        this.code = code;
        this.streamId = streamId;
    }

    public int getCode() {
        return code;
    }

    public long getStreamId() {
        return streamId;
    }

};
