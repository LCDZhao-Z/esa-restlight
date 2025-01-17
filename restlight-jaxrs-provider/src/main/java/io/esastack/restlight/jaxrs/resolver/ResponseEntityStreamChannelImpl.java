/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restlight.jaxrs.resolver;

import esa.commons.Checks;
import esa.commons.ExceptionUtils;
import esa.commons.collection.Attribute;
import esa.commons.collection.AttributeKey;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.restlight.core.context.ResponseEntityChannelImpl;
import io.esastack.restlight.core.context.ResponseContent;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.context.HttpOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.MathUtil;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class ResponseEntityStreamChannelImpl extends ResponseEntityChannelImpl
        implements ResponseEntityStreamChannel {

    private static final AttributeKey<ResponseEntityStreamChannel> RESPONSE_STREAM_ENTITY_CHANNEL =
            AttributeKey.valueOf("$response.entity.channel");

    private HttpOutputStream outputStream;

    public static ResponseEntityStreamChannel get(RequestContext context) {
        Attribute<ResponseEntityStreamChannel> attr = context.attrs().attr(RESPONSE_STREAM_ENTITY_CHANNEL);
        ResponseEntityStreamChannel channel = attr.get();
        if (channel != null) {
            return channel;
        }
        channel = new ResponseEntityStreamChannelImpl(context);
        attr.set(channel);
        return channel;
    }

    private ResponseEntityStreamChannelImpl(RequestContext context) {
        super(context);
    }

    @Override
    public HttpOutputStream outputStream() {
        if (content.isEnded()) {
            throw new IllegalStateException("Already ended.");
        }
        if (outputStream == null) {
            checkCommitted();
            outputStream = new ByteBufHttpOutputStream(4094, content);
        }
        return outputStream;
    }

    private void checkCommitted() {
        if (isCommitted()) {
            throw new IllegalStateException("Already committed.");
        }
    }

    static final class ByteBufHttpOutputStream extends HttpOutputStream {

        private static final int MIN_BUFFER_SIZE = 8;

        private final ByteBuf byteBuf;
        private final ResponseContent content;
        private volatile int closed;
        private static final AtomicIntegerFieldUpdater<ByteBufHttpOutputStream> CLOSED_UPDATER =
                AtomicIntegerFieldUpdater.newUpdater(ByteBufHttpOutputStream.class, "closed");

        ByteBufHttpOutputStream(int bufferSize, ResponseContent content) {
            if (bufferSize < MIN_BUFFER_SIZE) {
                throw new IllegalArgumentException("buffer size must be over than "
                        + MIN_BUFFER_SIZE + ". actual: " + bufferSize);
            }
            // use buffer size as the max capacity of the ByteBuf
            // also it means the buffer size is the max chunk size of http response.
            // initialCapacity = 0 => user had opened a ByteBufHttpOutputStream but did not write any data.
            this.byteBuf = content.alloc().buffer(0, bufferSize);
            this.content = content;
        }

        @Override
        public void write(int b) {
            checkCloseState();
            ensureSpace(1);
            byteBuf.writeByte(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            Checks.checkNotNull(b, "b");
            if (MathUtil.isOutOfBounds(off, len, b.length)) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return;
            }
            checkCloseState();
            write0(b, off, len);
        }

        @Override
        public void writeBoolean(boolean v) {
            checkCloseState();
            ensureSpace(1);
            byteBuf.writeBoolean(v);
        }

        @Override
        public void writeByte(int v) {
            checkCloseState();
            ensureSpace(1);
            byteBuf.writeByte(v);
        }

        @Override
        public void writeShort(int v) {
            checkCloseState();
            ensureSpace(2);
            byteBuf.writeShort(v);
        }

        @Override
        public void writeChar(int v) {
            checkCloseState();
            ensureSpace(2);
            byteBuf.writeChar(v);
        }

        @Override
        public void writeInt(int v) {
            checkCloseState();
            ensureSpace(4);
            byteBuf.writeInt(v);
        }

        @Override
        public void writeLong(long v) {
            checkCloseState();
            ensureSpace(8);
            byteBuf.writeLong(v);
        }

        @Override
        public void writeFloat(float v) {
            checkCloseState();
            ensureSpace(4);
            byteBuf.writeFloat(v);
        }

        @Override
        public void writeDouble(double v) {
            checkCloseState();
            ensureSpace(8);
            byteBuf.writeDouble(v);
        }

        @Override
        public void writeBytes(String s) {
            checkNullAndCloseState(s);
            int len = s.length();
            int writable;
            int off = 0;
            while ((writable = byteBuf.maxWritableBytes()) < len) {
                writeAscii0(s, off, writable);
                len -= writable;
                off += writable;
                flush(false);
            }

            if (len > 0) {
                writeAscii0(s, off, len);
            }
        }

        @Override
        public void writeChars(String s) {
            checkNullAndCloseState(s);
            int len = s.length();
            int writable;
            int off = 0;
            //Sets the specified 2-byte UTF-16 character
            //The 16 high-order bits of the specified value are ignored.
            while ((writable = byteBuf.maxWritableBytes() / 2) < len) {
                writeChars0(s, off, writable);
                len -= writable;
                off += writable;
                flush(false);
            }

            if (len > 0) {
                writeChars0(s, off, len);
            }
        }

        @Override
        public void writeUTF(String s) {
            checkNullAndCloseState(s);
            int len = ByteBufUtil.utf8MaxBytes(s);
            if (len > byteBuf.maxCapacity()) {
                byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
                write0(bytes, 0, bytes.length);
            } else {
                ensureSpace(len);
                byteBuf.writeCharSequence(s, CharsetUtil.UTF_8);
            }
        }

        @Override
        public void flush() {
            if (isClosed()) {
                return;
            }
            flush(false);
        }

        @Override
        public void close() {
            if (!CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
                return;
            }
            if (content.isEnded()) {
                // if the response has ended, we must make sure the current byteBuf should be released.
                // NOTE: this is important, due to that even if the output stream has been opened, the end user
                // can also end the response by Response. In this case, when we want to end the response
                // by closing the output stream, it may has been closed before. You can get more information at
                // ResponseEntityStreamChannelImpl.
                byteBuf.release();
            } else {
                flush(true);
                content.end();
            }
        }

        @Override
        public boolean isClosed() {
            return closed == 1;
        }

        private void flush(boolean isLast) {
            if (byteBuf.readableBytes() == 0) {
                if (isLast) {
                    byteBuf.release();
                }
                return;
            }

            if (isLast) {
                content.write(BufferUtil.wrap(byteBuf));
            } else {
                final ByteBuf copy = byteBuf.copy();
                try {
                    content.write(BufferUtil.wrap(copy));
                } catch (Exception e) {
                    copy.release();
                    ExceptionUtils.throwException(e);
                } finally {
                    byteBuf.clear();
                }
            }
        }

        private void checkNullAndCloseState(String s) {
            Checks.checkNotNull(s, "s");
            checkCloseState();
        }

        private void checkCloseState() {
            if (isClosed()) {
                throw new IllegalStateException("Output stream already closed");
            }
        }

        private void write0(byte[] b, int off, int len) {
            // if current buffer's writable space is less than len
            // write in the left space of the current buffer and flush it
            int writable;
            while ((writable = byteBuf.maxWritableBytes()) < len) {
                byteBuf.writeBytes(b, off, writable);
                len -= writable;
                off += writable;
                flush(false);
            }

            if (len > 0) {
                byteBuf.writeBytes(b, off, len);
            }
        }

        private void ensureSpace(int space) {
            if (byteBuf.maxWritableBytes() < space && byteBuf.capacity() > 0) {
                flush(false);
            }
        }

        private void writeAscii0(CharSequence seq, int off, int len) {
            int max = off + len;
            for (int i = off; i < max; i++) {
                byteBuf.writeByte(AsciiString.c2b(seq.charAt(i)));
            }
        }

        private void writeChars0(CharSequence seq, int off, int len) {
            int max = off + len;
            for (int i = off; i < max; i++) {
                byteBuf.writeChar(seq.charAt(i));
            }
        }
    }
}

