package com.alibaba.dubbo.performance.demo.agent.holder.bytebuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import io.netty.buffer.AbstractReferenceCountedByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.EmptyArrays;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 针对
 * @see com.alibaba.dubbo.performance.demo.agent.consumeragent.tcp.TcpProviderAgentMsgHandler 做的2元组优化。
 * 头部长度不变，内容长度可变
 */
public class TwoCompositeByteBuf  extends AbstractReferenceCountedByteBuf implements Iterable<ByteBuf>  {
    private static final ByteBuffer EMPTY_NIO_BUFFER = Unpooled.EMPTY_BUFFER.nioBuffer();
    private static final Iterator<ByteBuf> EMPTY_ITERATOR = Collections.<ByteBuf>emptyList().iterator();

    private final ByteBufAllocator alloc;
    private final boolean direct;
    private final ComponentList components;
    private final int maxNumComponents;

    private boolean freed;

    /**
     * @param alloc
     * @param head 和头部长度一样的ByteBuf
     */
    public TwoCompositeByteBuf(ByteBufAllocator alloc, ByteBuf head) {
        super(Integer.MAX_VALUE);
        this.alloc = alloc;
        this.direct = true;
        this.maxNumComponents = 2;
        components = newList(maxNumComponents);
        this.addComponents(head, new EmptyByteBuf(this.alloc));
    }

    public TwoCompositeByteBuf setHeadAndBody(ByteBuf head, ByteBuf body) {

        Component compHead = this.components.get(0);
        compHead.buf = head.slice();
//        compHead.length = head.readableBytes();
//        compHead.offset = 0;
//        compHead.endOffset = compHead.length;

        Component compBody = this.components.get(1);
        compBody.buf = body.slice();
        int length = body.readableBytes();
        compBody.length = length;
        compBody.offset = compHead.endOffset;
        compBody.endOffset = compBody.offset + length;

//        readerIndex(0);
        writerIndex(compBody.endOffset);

        return this;
    }

    private static ComponentList newList(int maxNumComponents) {
        return new ComponentList(Math.min(Integer.MAX_VALUE, maxNumComponents));
    }

    /**
     * Add the given {@link ByteBuf}.
     * <p>
     * Be aware that this method does not increase the {@code writerIndex} of the {@link TwoCompositeByteBuf}.
     * If you need to have it increased use {@link #addComponent(boolean, ByteBuf)}.
     * <p>
     * {@link ByteBuf#release()} ownership of {@code buffer} is transfered to this {@link TwoCompositeByteBuf}.
     * @param buffer the {@link ByteBuf} to add. {@link ByteBuf#release()} ownership is transfered to this
     * {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponent(ByteBuf buffer) {
        return addComponent(false, buffer);
    }

    /**
     * Add the given {@link ByteBuf}s.
     * <p>
     * Be aware that this method does not increase the {@code writerIndex} of the {@link TwoCompositeByteBuf}.
     * If you need to have it increased use {@link #addComponents(boolean, ByteBuf[])}.
     * <p>
     * {@link ByteBuf#release()} ownership of all {@link ByteBuf} objects in {@code buffers} is transfered to this
     * {@link TwoCompositeByteBuf}.
     * @param buffers the {@link ByteBuf}s to add. {@link ByteBuf#release()} ownership of all {@link ByteBuf#release()}
     * ownership of all {@link ByteBuf} objects is transfered to this {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponents(ByteBuf... buffers) {
        return addComponents(false, buffers);
    }

    /**
     * Add the given {@link ByteBuf}s.
     * <p>
     * Be aware that this method does not increase the {@code writerIndex} of the {@link TwoCompositeByteBuf}.
     * If you need to have it increased use {@link #addComponents(boolean, Iterable)}.
     * <p>
     * {@link ByteBuf#release()} ownership of all {@link ByteBuf} objects in {@code buffers} is transfered to this
     * {@link TwoCompositeByteBuf}.
     * @param buffers the {@link ByteBuf}s to add. {@link ByteBuf#release()} ownership of all {@link ByteBuf#release()}
     * ownership of all {@link ByteBuf} objects is transfered to this {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponents(Iterable<ByteBuf> buffers) {
        return addComponents(false, buffers);
    }

    /**
     * Add the given {@link ByteBuf} on the specific index.
     * <p>
     * Be aware that this method does not increase the {@code writerIndex} of the {@link TwoCompositeByteBuf}.
     * If you need to have it increased use {@link #addComponent(boolean, int, ByteBuf)}.
     * <p>
     * {@link ByteBuf#release()} ownership of {@code buffer} is transfered to this {@link TwoCompositeByteBuf}.
     * @param cIndex the index on which the {@link ByteBuf} will be added.
     * @param buffer the {@link ByteBuf} to add. {@link ByteBuf#release()} ownership is transfered to this
     * {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponent(int cIndex, ByteBuf buffer) {
        return addComponent(false, cIndex, buffer);
    }

    /**
     * Add the given {@link ByteBuf} and increase the {@code writerIndex} if {@code increaseWriterIndex} is
     * {@code true}.
     *
     * {@link ByteBuf#release()} ownership of {@code buffer} is transfered to this {@link TwoCompositeByteBuf}.
     * @param buffer the {@link ByteBuf} to add. {@link ByteBuf#release()} ownership is transfered to this
     * {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponent(boolean increaseWriterIndex, ByteBuf buffer) {
        checkNotNull(buffer, "buffer");
        addComponent0(increaseWriterIndex, components.size(), buffer);
        consolidateIfNeeded();
        return this;
    }

    /**
     * Add the given {@link ByteBuf}s and increase the {@code writerIndex} if {@code increaseWriterIndex} is
     * {@code true}.
     *
     * {@link ByteBuf#release()} ownership of all {@link ByteBuf} objects in {@code buffers} is transfered to this
     * {@link TwoCompositeByteBuf}.
     * @param buffers the {@link ByteBuf}s to add. {@link ByteBuf#release()} ownership of all {@link ByteBuf#release()}
     * ownership of all {@link ByteBuf} objects is transfered to this {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponents(boolean increaseWriterIndex, ByteBuf... buffers) {
        addComponents0(increaseWriterIndex, components.size(), buffers, 0, buffers.length);
        consolidateIfNeeded();
        return this;
    }

    /**
     * Add the given {@link ByteBuf}s and increase the {@code writerIndex} if {@code increaseWriterIndex} is
     * {@code true}.
     *
     * {@link ByteBuf#release()} ownership of all {@link ByteBuf} objects in {@code buffers} is transfered to this
     * {@link TwoCompositeByteBuf}.
     * @param buffers the {@link ByteBuf}s to add. {@link ByteBuf#release()} ownership of all {@link ByteBuf#release()}
     * ownership of all {@link ByteBuf} objects is transfered to this {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponents(boolean increaseWriterIndex, Iterable<ByteBuf> buffers) {
        addComponents0(increaseWriterIndex, components.size(), buffers);
        consolidateIfNeeded();
        return this;
    }

    /**
     * Add the given {@link ByteBuf} on the specific index and increase the {@code writerIndex}
     * if {@code increaseWriterIndex} is {@code true}.
     *
     * {@link ByteBuf#release()} ownership of {@code buffer} is transfered to this {@link TwoCompositeByteBuf}.
     * @param cIndex the index on which the {@link ByteBuf} will be added.
     * @param buffer the {@link ByteBuf} to add. {@link ByteBuf#release()} ownership is transfered to this
     * {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponent(boolean increaseWriterIndex, int cIndex, ByteBuf buffer) {
        checkNotNull(buffer, "buffer");
        addComponent0(increaseWriterIndex, cIndex, buffer);
        consolidateIfNeeded();
        return this;
    }

    /**
     * Precondition is that {@code buffer != null}.
     */
    private int addComponent0(boolean increaseWriterIndex, int cIndex, ByteBuf buffer) {
        assert buffer != null;
        boolean wasAdded = false;
        try {
            checkComponentIndex(cIndex);

            int readableBytes = buffer.readableBytes();

            // No need to consolidate - just add a component to the list.
            @SuppressWarnings("deprecation")
            Component c = new Component(buffer.order(ByteOrder.BIG_ENDIAN).slice());
            if (cIndex == components.size()) {
                wasAdded = components.add(c);
                if (cIndex == 0) {
                    c.endOffset = readableBytes;
                } else {
                    Component prev = components.get(cIndex - 1);
                    c.offset = prev.endOffset;
                    c.endOffset = c.offset + readableBytes;
                }
            } else {
                components.add(cIndex, c);
                wasAdded = true;
                if (readableBytes != 0) {
                    updateComponentOffsets(cIndex);
                }
            }
            if (increaseWriterIndex) {
                writerIndex(writerIndex() + buffer.readableBytes());
            }
            return cIndex;
        } finally {
            if (!wasAdded) {
                buffer.release();
            }
        }
    }

    /**
     * Add the given {@link ByteBuf}s on the specific index
     * <p>
     * Be aware that this method does not increase the {@code writerIndex} of the {@link TwoCompositeByteBuf}.
     * If you need to have it increased you need to handle it by your own.
     * <p>
     * {@link ByteBuf#release()} ownership of all {@link ByteBuf} objects in {@code buffers} is transfered to this
     * {@link TwoCompositeByteBuf}.
     * @param cIndex the index on which the {@link ByteBuf} will be added. {@link ByteBuf#release()} ownership of all
     * {@link ByteBuf#release()} ownership of all {@link ByteBuf} objects is transfered to this
     * {@link TwoCompositeByteBuf}.
     * @param buffers the {@link ByteBuf}s to add. {@link ByteBuf#release()} ownership of all {@link ByteBuf#release()}
     * ownership of all {@link ByteBuf} objects is transfered to this {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponents(int cIndex, ByteBuf... buffers) {
        addComponents0(false, cIndex, buffers, 0, buffers.length);
        consolidateIfNeeded();
        return this;
    }

    private int addComponents0(boolean increaseWriterIndex, int cIndex, ByteBuf[] buffers, int offset, int len) {
        checkNotNull(buffers, "buffers");
        int i = offset;
        try {
            checkComponentIndex(cIndex);

            // No need for consolidation
            while (i < len) {
                // Increment i now to prepare for the next iteration and prevent a duplicate release (addComponent0
                // will release if an exception occurs, and we also release in the finally block here).
                ByteBuf b = buffers[i++];
                if (b == null) {
                    break;
                }
                cIndex = addComponent0(increaseWriterIndex, cIndex, b) + 1;
                int size = components.size();
                if (cIndex > size) {
                    cIndex = size;
                }
            }
            return cIndex;
        } finally {
            for (; i < len; ++i) {
                ByteBuf b = buffers[i];
                if (b != null) {
                    try {
                        b.release();
                    } catch (Throwable ignored) {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * Add the given {@link ByteBuf}s on the specific index
     *
     * Be aware that this method does not increase the {@code writerIndex} of the {@link TwoCompositeByteBuf}.
     * If you need to have it increased you need to handle it by your own.
     * <p>
     * {@link ByteBuf#release()} ownership of all {@link ByteBuf} objects in {@code buffers} is transfered to this
     * {@link TwoCompositeByteBuf}.
     * @param cIndex the index on which the {@link ByteBuf} will be added.
     * @param buffers the {@link ByteBuf}s to add.  {@link ByteBuf#release()} ownership of all
     * {@link ByteBuf#release()} ownership of all {@link ByteBuf} objects is transfered to this
     * {@link TwoCompositeByteBuf}.
     */
    public TwoCompositeByteBuf addComponents(int cIndex, Iterable<ByteBuf> buffers) {
        addComponents0(false, cIndex, buffers);
        consolidateIfNeeded();
        return this;
    }

    private int addComponents0(boolean increaseIndex, int cIndex, Iterable<ByteBuf> buffers) {
        if (buffers instanceof ByteBuf) {
            // If buffers also implements ByteBuf (e.g. TwoCompositeByteBuf), it has to go to addComponent(ByteBuf).
            return addComponent0(increaseIndex, cIndex, (ByteBuf) buffers);
        }
        checkNotNull(buffers, "buffers");

        if (!(buffers instanceof Collection)) {
            List<ByteBuf> list = new ArrayList<ByteBuf>();
            try {
                for (ByteBuf b: buffers) {
                    list.add(b);
                }
                buffers = list;
            } finally {
                if (buffers != list) {
                    for (ByteBuf b: buffers) {
                        if (b != null) {
                            try {
                                b.release();
                            } catch (Throwable ignored) {
                                // ignore
                            }
                        }
                    }
                }
            }
        }

        Collection<ByteBuf> col = (Collection<ByteBuf>) buffers;
        return addComponents0(increaseIndex, cIndex, col.toArray(new ByteBuf[col.size()]), 0 , col.size());
    }

    /**
     * This should only be called as last operation from a method as this may adjust the underlying
     * array of components and so affect the index etc.
     */
    private void consolidateIfNeeded() {
        // Consolidate if the number of components will exceed the allowed maximum by the current
        // operation.
        final int numComponents = components.size();
        if (numComponents > maxNumComponents) {
            final int capacity = components.get(numComponents - 1).endOffset;

            ByteBuf consolidated = allocBuffer(capacity);

            // We're not using foreach to avoid creating an iterator.
            for (int i = 0; i < numComponents; i ++) {
                Component c = components.get(i);
                ByteBuf b = c.buf;
                consolidated.writeBytes(b);
                c.freeIfNecessary();
            }
            Component c = new Component(consolidated);
            c.endOffset = c.length;
            components.clear();
            components.add(c);
        }
    }

    private void checkComponentIndex(int cIndex) {
        ensureAccessible();
        if (cIndex < 0 || cIndex > components.size()) {
            throw new IndexOutOfBoundsException(String.format(
                    "cIndex: %d (expected: >= 0 && <= numComponents(%d))",
                    cIndex, components.size()));
        }
    }

    private void checkComponentIndex(int cIndex, int numComponents) {
        ensureAccessible();
        if (cIndex < 0 || cIndex + numComponents > components.size()) {
            throw new IndexOutOfBoundsException(String.format(
                    "cIndex: %d, numComponents: %d " +
                            "(expected: cIndex >= 0 && cIndex + numComponents <= totalNumComponents(%d))",
                    cIndex, numComponents, components.size()));
        }
    }

    private void updateComponentOffsets(int cIndex) {
        int size = components.size();
        if (size <= cIndex) {
            return;
        }

        Component c = components.get(cIndex);
        if (cIndex == 0) {
            c.offset = 0;
            c.endOffset = c.length;
            cIndex ++;
        }

        for (int i = cIndex; i < size; i ++) {
            Component prev = components.get(i - 1);
            Component cur = components.get(i);
            cur.offset = prev.endOffset;
            cur.endOffset = cur.offset + cur.length;
        }
    }

    /**
     * Remove the {@link ByteBuf} from the given index.
     *
     * @param cIndex the index on from which the {@link ByteBuf} will be remove
     */
    public TwoCompositeByteBuf removeComponent(int cIndex) {
        checkComponentIndex(cIndex);
        Component comp = components.remove(cIndex);
        comp.freeIfNecessary();
        if (comp.length > 0) {
            // Only need to call updateComponentOffsets if the length was > 0
            updateComponentOffsets(cIndex);
        }
        return this;
    }

    /**
     * Remove the number of {@link ByteBuf}s starting from the given index.
     *
     * @param cIndex the index on which the {@link ByteBuf}s will be started to removed
     * @param numComponents the number of components to remove
     */
    public TwoCompositeByteBuf removeComponents(int cIndex, int numComponents) {
        checkComponentIndex(cIndex, numComponents);

        if (numComponents == 0) {
            return this;
        }
        int endIndex = cIndex + numComponents;
        boolean needsUpdate = false;
        for (int i = cIndex; i < endIndex; ++i) {
            Component c = components.get(i);
            if (c.length > 0) {
                needsUpdate = true;
            }
            c.freeIfNecessary();
        }
        components.removeRange(cIndex, endIndex);

        if (needsUpdate) {
            // Only need to call updateComponentOffsets if the length was > 0
            updateComponentOffsets(cIndex);
        }
        return this;
    }

    @Override
    public Iterator<ByteBuf> iterator() {
        ensureAccessible();
        if (components.isEmpty()) {
            return EMPTY_ITERATOR;
        }
        return new CompositeByteBufIterator();
    }

    /**
     * Same with {@link #slice(int, int)} except that this method returns a list.
     */
    public List<ByteBuf> decompose(int offset, int length) {
        checkIndex(offset, length);
        if (length == 0) {
            return Collections.emptyList();
        }

        int componentId = toComponentIndex(offset);
        List<ByteBuf> slice = new ArrayList<ByteBuf>(components.size());

        // The first component
        Component firstC = components.get(componentId);
        ByteBuf first = firstC.buf.duplicate();
        first.readerIndex(offset - firstC.offset);

        ByteBuf buf = first;
        int bytesToSlice = length;
        do {
            int readableBytes = buf.readableBytes();
            if (bytesToSlice <= readableBytes) {
                // Last component
                buf.writerIndex(buf.readerIndex() + bytesToSlice);
                slice.add(buf);
                break;
            } else {
                // Not the last component
                slice.add(buf);
                bytesToSlice -= readableBytes;
                componentId ++;

                // Fetch the next component.
                buf = components.get(componentId).buf.duplicate();
            }
        } while (bytesToSlice > 0);

        // Slice all components because only readable bytes are interesting.
        for (int i = 0; i < slice.size(); i ++) {
            slice.set(i, slice.get(i).slice());
        }

        return slice;
    }

    @Override
    public boolean isDirect() {
        int size = components.size();
        if (size == 0) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!components.get(i).buf.isDirect()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasArray() {
        switch (components.size()) {
            case 0:
                return true;
            case 1:
                return components.get(0).buf.hasArray();
            default:
                return false;
        }
    }

    @Override
    public byte[] array() {
        switch (components.size()) {
            case 0:
                return EmptyArrays.EMPTY_BYTES;
            case 1:
                return components.get(0).buf.array();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public int arrayOffset() {
        switch (components.size()) {
            case 0:
                return 0;
            case 1:
                return components.get(0).buf.arrayOffset();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean hasMemoryAddress() {
        switch (components.size()) {
            case 0:
                return Unpooled.EMPTY_BUFFER.hasMemoryAddress();
            case 1:
                return components.get(0).buf.hasMemoryAddress();
            default:
                return false;
        }
    }

    @Override
    public long memoryAddress() {
        switch (components.size()) {
            case 0:
                return Unpooled.EMPTY_BUFFER.memoryAddress();
            case 1:
                return components.get(0).buf.memoryAddress();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public int capacity() {
        final int numComponents = components.size();
        if (numComponents == 0) {
            return 0;
        }
        return components.get(numComponents - 1).endOffset;
    }

    @Override
    public TwoCompositeByteBuf capacity(int newCapacity) {
        checkNewCapacity(newCapacity);

        int oldCapacity = capacity();
        if (newCapacity > oldCapacity) {
            final int paddingLength = newCapacity - oldCapacity;
            ByteBuf padding;
            int nComponents = components.size();
            if (nComponents < maxNumComponents) {
                padding = allocBuffer(paddingLength);
                padding.setIndex(0, paddingLength);
                addComponent0(false, components.size(), padding);
            } else {
                padding = allocBuffer(paddingLength);
                padding.setIndex(0, paddingLength);
                // FIXME: No need to create a padding buffer and consolidate.
                // Just create a big single buffer and put the current content there.
                addComponent0(false, components.size(), padding);
                consolidateIfNeeded();
            }
        } else if (newCapacity < oldCapacity) {
            int bytesToTrim = oldCapacity - newCapacity;
            for (ListIterator<Component> i = components.listIterator(components.size()); i.hasPrevious();) {
                Component c = i.previous();
                if (bytesToTrim >= c.length) {
                    bytesToTrim -= c.length;
                    i.remove();
                    continue;
                }

                // Replace the last component with the trimmed slice.
                Component newC = new Component(c.buf.slice(0, c.length - bytesToTrim));
                newC.offset = c.offset;
                newC.endOffset = newC.offset + newC.length;
                i.set(newC);
                break;
            }

            if (readerIndex() > newCapacity) {
                setIndex(newCapacity, newCapacity);
            } else if (writerIndex() > newCapacity) {
                writerIndex(newCapacity);
            }
        }
        return this;
    }

    @Override
    public ByteBufAllocator alloc() {
        return alloc;
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.BIG_ENDIAN;
    }

    /**
     * Return the current number of {@link ByteBuf}'s that are composed in this instance
     */
    public int numComponents() {
        return components.size();
    }

    /**
     * Return the max number of {@link ByteBuf}'s that are composed in this instance
     */
    public int maxNumComponents() {
        return maxNumComponents;
    }

    /**
     * Return the index for the given offset
     */
    public int toComponentIndex(int offset) {
        checkIndex(offset);

        for (int low = 0, high = components.size(); low <= high;) {
            int mid = low + high >>> 1;
            Component c = components.get(mid);
            if (offset >= c.endOffset) {
                low = mid + 1;
            } else if (offset < c.offset) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        throw new Error("should not reach here");
    }

    public int toByteIndex(int cIndex) {
        checkComponentIndex(cIndex);
        return components.get(cIndex).offset;
    }

    @Override
    public byte getByte(int index) {
        return _getByte(index);
    }

    @Override
    protected byte _getByte(int index) {
        Component c = findComponent(index);
        return c.buf.getByte(index - c.offset);
    }

    @Override
    protected short _getShort(int index) {
        Component c = findComponent(index);
        if (index + 2 <= c.endOffset) {
            return c.buf.getShort(index - c.offset);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            return (short) ((_getByte(index) & 0xff) << 8 | _getByte(index + 1) & 0xff);
        } else {
            return (short) (_getByte(index) & 0xff | (_getByte(index + 1) & 0xff) << 8);
        }
    }

    @Override
    protected short _getShortLE(int index) {
        Component c = findComponent(index);
        if (index + 2 <= c.endOffset) {
            return c.buf.getShortLE(index - c.offset);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            return (short) (_getByte(index) & 0xff | (_getByte(index + 1) & 0xff) << 8);
        } else {
            return (short) ((_getByte(index) & 0xff) << 8 | _getByte(index + 1) & 0xff);
        }
    }

    @Override
    protected int _getUnsignedMedium(int index) {
        Component c = findComponent(index);
        if (index + 3 <= c.endOffset) {
            return c.buf.getUnsignedMedium(index - c.offset);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            return (_getShort(index) & 0xffff) << 8 | _getByte(index + 2) & 0xff;
        } else {
            return _getShort(index) & 0xFFFF | (_getByte(index + 2) & 0xFF) << 16;
        }
    }

    @Override
    protected int _getUnsignedMediumLE(int index) {
        Component c = findComponent(index);
        if (index + 3 <= c.endOffset) {
            return c.buf.getUnsignedMediumLE(index - c.offset);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            return _getShortLE(index) & 0xffff | (_getByte(index + 2) & 0xff) << 16;
        } else {
            return (_getShortLE(index) & 0xffff) << 8 | _getByte(index + 2) & 0xff;
        }
    }

    @Override
    protected int _getInt(int index) {
        Component c = findComponent(index);
        if (index + 4 <= c.endOffset) {
            return c.buf.getInt(index - c.offset);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            return (_getShort(index) & 0xffff) << 16 | _getShort(index + 2) & 0xffff;
        } else {
            return _getShort(index) & 0xFFFF | (_getShort(index + 2) & 0xFFFF) << 16;
        }
    }

    @Override
    protected int _getIntLE(int index) {
        Component c = findComponent(index);
        if (index + 4 <= c.endOffset) {
            return c.buf.getIntLE(index - c.offset);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            return _getShortLE(index) & 0xffff | (_getShortLE(index + 2) & 0xffff) << 16;
        } else {
            return (_getShortLE(index) & 0xffff) << 16 | _getShortLE(index + 2) & 0xffff;
        }
    }

    @Override
    protected long _getLong(int index) {
        Component c = findComponent(index);
        if (index + 8 <= c.endOffset) {
            return c.buf.getLong(index - c.offset);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            return (_getInt(index) & 0xffffffffL) << 32 | _getInt(index + 4) & 0xffffffffL;
        } else {
            return _getInt(index) & 0xFFFFFFFFL | (_getInt(index + 4) & 0xFFFFFFFFL) << 32;
        }
    }

    @Override
    protected long _getLongLE(int index) {
        Component c = findComponent(index);
        if (index + 8 <= c.endOffset) {
            return c.buf.getLongLE(index - c.offset);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            return _getIntLE(index) & 0xffffffffL | (_getIntLE(index + 4) & 0xffffffffL) << 32;
        } else {
            return (_getIntLE(index) & 0xffffffffL) << 32 | _getIntLE(index + 4) & 0xffffffffL;
        }
    }

    @Override
    public TwoCompositeByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        checkDstIndex(index, length, dstIndex, dst.length);
        if (length == 0) {
            return this;
        }

        int i = toComponentIndex(index);
        while (length > 0) {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            s.getBytes(index - adjustment, dst, dstIndex, localLength);
            index += localLength;
            dstIndex += localLength;
            length -= localLength;
            i ++;
        }
        return this;
    }

    @Override
    public TwoCompositeByteBuf getBytes(int index, ByteBuffer dst) {
        int limit = dst.limit();
        int length = dst.remaining();

        checkIndex(index, length);
        if (length == 0) {
            return this;
        }

        int i = toComponentIndex(index);
        try {
            while (length > 0) {
                Component c = components.get(i);
                ByteBuf s = c.buf;
                int adjustment = c.offset;
                int localLength = Math.min(length, s.capacity() - (index - adjustment));
                dst.limit(dst.position() + localLength);
                s.getBytes(index - adjustment, dst);
                index += localLength;
                length -= localLength;
                i ++;
            }
        } finally {
            dst.limit(limit);
        }
        return this;
    }

    @Override
    public TwoCompositeByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        checkDstIndex(index, length, dstIndex, dst.capacity());
        if (length == 0) {
            return this;
        }

        int i = toComponentIndex(index);
        while (length > 0) {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            s.getBytes(index - adjustment, dst, dstIndex, localLength);
            index += localLength;
            dstIndex += localLength;
            length -= localLength;
            i ++;
        }
        return this;
    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length)
            throws IOException {
        int count = nioBufferCount();
        if (count == 1) {
            return out.write(internalNioBuffer(index, length));
        } else {
            long writtenBytes = out.write(nioBuffers(index, length));
            if (writtenBytes > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else {
                return (int) writtenBytes;
            }
        }
    }

    @Override
    public int getBytes(int index, FileChannel out, long position, int length)
            throws IOException {
        int count = nioBufferCount();
        if (count == 1) {
            return out.write(internalNioBuffer(index, length), position);
        } else {
            long writtenBytes = 0;
            for (ByteBuffer buf : nioBuffers(index, length)) {
                writtenBytes += out.write(buf, position + writtenBytes);
            }
            if (writtenBytes > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) writtenBytes;
        }
    }

    @Override
    public TwoCompositeByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        checkIndex(index, length);
        if (length == 0) {
            return this;
        }

        int i = toComponentIndex(index);
        while (length > 0) {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            s.getBytes(index - adjustment, out, localLength);
            index += localLength;
            length -= localLength;
            i ++;
        }
        return this;
    }

    @Override
    public TwoCompositeByteBuf setByte(int index, int value) {
        Component c = findComponent(index);
        c.buf.setByte(index - c.offset, value);
        return this;
    }

    @Override
    protected void _setByte(int index, int value) {
        setByte(index, value);
    }

    @Override
    public TwoCompositeByteBuf setShort(int index, int value) {
        return (TwoCompositeByteBuf) super.setShort(index, value);
    }

    @Override
    protected void _setShort(int index, int value) {
        Component c = findComponent(index);
        if (index + 2 <= c.endOffset) {
            c.buf.setShort(index - c.offset, value);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            _setByte(index, (byte) (value >>> 8));
            _setByte(index + 1, (byte) value);
        } else {
            _setByte(index, (byte) value);
            _setByte(index + 1, (byte) (value >>> 8));
        }
    }

    @Override
    protected void _setShortLE(int index, int value) {
        Component c = findComponent(index);
        if (index + 2 <= c.endOffset) {
            c.buf.setShortLE(index - c.offset, value);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            _setByte(index, (byte) value);
            _setByte(index + 1, (byte) (value >>> 8));
        } else {
            _setByte(index, (byte) (value >>> 8));
            _setByte(index + 1, (byte) value);
        }
    }

    @Override
    public TwoCompositeByteBuf setMedium(int index, int value) {
        return (TwoCompositeByteBuf) super.setMedium(index, value);
    }

    @Override
    protected void _setMedium(int index, int value) {
        Component c = findComponent(index);
        if (index + 3 <= c.endOffset) {
            c.buf.setMedium(index - c.offset, value);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            _setShort(index, (short) (value >> 8));
            _setByte(index + 2, (byte) value);
        } else {
            _setShort(index, (short) value);
            _setByte(index + 2, (byte) (value >>> 16));
        }
    }

    @Override
    protected void _setMediumLE(int index, int value) {
        Component c = findComponent(index);
        if (index + 3 <= c.endOffset) {
            c.buf.setMediumLE(index - c.offset, value);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            _setShortLE(index, (short) value);
            _setByte(index + 2, (byte) (value >>> 16));
        } else {
            _setShortLE(index, (short) (value >> 8));
            _setByte(index + 2, (byte) value);
        }
    }

    @Override
    public TwoCompositeByteBuf setInt(int index, int value) {
        return (TwoCompositeByteBuf) super.setInt(index, value);
    }

    @Override
    protected void _setInt(int index, int value) {
        Component c = findComponent(index);
        if (index + 4 <= c.endOffset) {
            c.buf.setInt(index - c.offset, value);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            _setShort(index, (short) (value >>> 16));
            _setShort(index + 2, (short) value);
        } else {
            _setShort(index, (short) value);
            _setShort(index + 2, (short) (value >>> 16));
        }
    }

    @Override
    protected void _setIntLE(int index, int value) {
        Component c = findComponent(index);
        if (index + 4 <= c.endOffset) {
            c.buf.setIntLE(index - c.offset, value);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            _setShortLE(index, (short) value);
            _setShortLE(index + 2, (short) (value >>> 16));
        } else {
            _setShortLE(index, (short) (value >>> 16));
            _setShortLE(index + 2, (short) value);
        }
    }

    @Override
    public TwoCompositeByteBuf setLong(int index, long value) {
        return (TwoCompositeByteBuf) super.setLong(index, value);
    }

    @Override
    protected void _setLong(int index, long value) {
        Component c = findComponent(index);
        if (index + 8 <= c.endOffset) {
            c.buf.setLong(index - c.offset, value);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            _setInt(index, (int) (value >>> 32));
            _setInt(index + 4, (int) value);
        } else {
            _setInt(index, (int) value);
            _setInt(index + 4, (int) (value >>> 32));
        }
    }

    @Override
    protected void _setLongLE(int index, long value) {
        Component c = findComponent(index);
        if (index + 8 <= c.endOffset) {
            c.buf.setLongLE(index - c.offset, value);
        } else if (order() == ByteOrder.BIG_ENDIAN) {
            _setIntLE(index, (int) value);
            _setIntLE(index + 4, (int) (value >>> 32));
        } else {
            _setIntLE(index, (int) (value >>> 32));
            _setIntLE(index + 4, (int) value);
        }
    }

    @Override
    public TwoCompositeByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        checkSrcIndex(index, length, srcIndex, src.length);
        if (length == 0) {
            return this;
        }

        int i = toComponentIndex(index);
        while (length > 0) {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            s.setBytes(index - adjustment, src, srcIndex, localLength);
            index += localLength;
            srcIndex += localLength;
            length -= localLength;
            i ++;
        }
        return this;
    }

    @Override
    public TwoCompositeByteBuf setBytes(int index, ByteBuffer src) {
        int limit = src.limit();
        int length = src.remaining();

        checkIndex(index, length);
        if (length == 0) {
            return this;
        }

        int i = toComponentIndex(index);
        try {
            while (length > 0) {
                Component c = components.get(i);
                ByteBuf s = c.buf;
                int adjustment = c.offset;
                int localLength = Math.min(length, s.capacity() - (index - adjustment));
                src.limit(src.position() + localLength);
                s.setBytes(index - adjustment, src);
                index += localLength;
                length -= localLength;
                i ++;
            }
        } finally {
            src.limit(limit);
        }
        return this;
    }

    @Override
    public TwoCompositeByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        checkSrcIndex(index, length, srcIndex, src.capacity());
        if (length == 0) {
            return this;
        }

        int i = toComponentIndex(index);
        while (length > 0) {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            s.setBytes(index - adjustment, src, srcIndex, localLength);
            index += localLength;
            srcIndex += localLength;
            length -= localLength;
            i ++;
        }
        return this;
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        checkIndex(index, length);
        if (length == 0) {
            return in.read(EmptyArrays.EMPTY_BYTES);
        }

        int i = toComponentIndex(index);
        int readBytes = 0;

        do {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            if (localLength == 0) {
                // Skip empty buffer
                i++;
                continue;
            }
            int localReadBytes = s.setBytes(index - adjustment, in, localLength);
            if (localReadBytes < 0) {
                if (readBytes == 0) {
                    return -1;
                } else {
                    break;
                }
            }

            if (localReadBytes == localLength) {
                index += localLength;
                length -= localLength;
                readBytes += localLength;
                i ++;
            } else {
                index += localReadBytes;
                length -= localReadBytes;
                readBytes += localReadBytes;
            }
        } while (length > 0);

        return readBytes;
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        checkIndex(index, length);
        if (length == 0) {
            return in.read(EMPTY_NIO_BUFFER);
        }

        int i = toComponentIndex(index);
        int readBytes = 0;
        do {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            if (localLength == 0) {
                // Skip empty buffer
                i++;
                continue;
            }
            int localReadBytes = s.setBytes(index - adjustment, in, localLength);

            if (localReadBytes == 0) {
                break;
            }

            if (localReadBytes < 0) {
                if (readBytes == 0) {
                    return -1;
                } else {
                    break;
                }
            }

            if (localReadBytes == localLength) {
                index += localLength;
                length -= localLength;
                readBytes += localLength;
                i ++;
            } else {
                index += localReadBytes;
                length -= localReadBytes;
                readBytes += localReadBytes;
            }
        } while (length > 0);

        return readBytes;
    }

    @Override
    public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        checkIndex(index, length);
        if (length == 0) {
            return in.read(EMPTY_NIO_BUFFER, position);
        }

        int i = toComponentIndex(index);
        int readBytes = 0;
        do {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            if (localLength == 0) {
                // Skip empty buffer
                i++;
                continue;
            }
            int localReadBytes = s.setBytes(index - adjustment, in, position + readBytes, localLength);

            if (localReadBytes == 0) {
                break;
            }

            if (localReadBytes < 0) {
                if (readBytes == 0) {
                    return -1;
                } else {
                    break;
                }
            }

            if (localReadBytes == localLength) {
                index += localLength;
                length -= localLength;
                readBytes += localLength;
                i ++;
            } else {
                index += localReadBytes;
                length -= localReadBytes;
                readBytes += localReadBytes;
            }
        } while (length > 0);

        return readBytes;
    }

    @Override
    public ByteBuf copy(int index, int length) {
        checkIndex(index, length);
        ByteBuf dst = allocBuffer(length);
        if (length != 0) {
            copyTo(index, length, toComponentIndex(index), dst);
        }
        return dst;
    }

    private void copyTo(int index, int length, int componentId, ByteBuf dst) {
        int dstIndex = 0;
        int i = componentId;

        while (length > 0) {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            s.getBytes(index - adjustment, dst, dstIndex, localLength);
            index += localLength;
            dstIndex += localLength;
            length -= localLength;
            i ++;
        }

        dst.writerIndex(dst.capacity());
    }

    /**
     * Return the {@link ByteBuf} on the specified index
     *
     * @param cIndex the index for which the {@link ByteBuf} should be returned
     * @return buf the {@link ByteBuf} on the specified index
     */
    public ByteBuf component(int cIndex) {
        return internalComponent(cIndex).duplicate();
    }

    /**
     * Return the {@link ByteBuf} on the specified index
     *
     * @param offset the offset for which the {@link ByteBuf} should be returned
     * @return the {@link ByteBuf} on the specified index
     */
    public ByteBuf componentAtOffset(int offset) {
        return internalComponentAtOffset(offset).duplicate();
    }

    /**
     * Return the internal {@link ByteBuf} on the specified index. Note that updating the indexes of the returned
     * buffer will lead to an undefined behavior of this buffer.
     *
     * @param cIndex the index for which the {@link ByteBuf} should be returned
     */
    public ByteBuf internalComponent(int cIndex) {
        checkComponentIndex(cIndex);
        return components.get(cIndex).buf;
    }

    /**
     * Return the internal {@link ByteBuf} on the specified offset. Note that updating the indexes of the returned
     * buffer will lead to an undefined behavior of this buffer.
     *
     * @param offset the offset for which the {@link ByteBuf} should be returned
     */
    public ByteBuf internalComponentAtOffset(int offset) {
        return findComponent(offset).buf;
    }

    private Component findComponent(int offset) {
        checkIndex(offset);

        for (int low = 0, high = components.size(); low <= high;) {
            int mid = low + high >>> 1;
            Component c = components.get(mid);
            if (offset >= c.endOffset) {
                low = mid + 1;
            } else if (offset < c.offset) {
                high = mid - 1;
            } else {
                assert c.length != 0;
                return c;
            }
        }

        throw new Error("should not reach here");
    }

    @Override
    public int nioBufferCount() {
        switch (components.size()) {
            case 0:
                return 1;
            case 1:
                return components.get(0).buf.nioBufferCount();
            default:
                int count = 0;
                int componentsCount = components.size();
                for (int i = 0; i < componentsCount; i++) {
                    Component c = components.get(i);
                    count += c.buf.nioBufferCount();
                }
                return count;
        }
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        switch (components.size()) {
            case 0:
                return EMPTY_NIO_BUFFER;
            case 1:
                return components.get(0).buf.internalNioBuffer(index, length);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        checkIndex(index, length);

        switch (components.size()) {
            case 0:
                return EMPTY_NIO_BUFFER;
            case 1:
                ByteBuf buf = components.get(0).buf;
                if (buf.nioBufferCount() == 1) {
                    return components.get(0).buf.nioBuffer(index, length);
                }
        }

        ByteBuffer merged = ByteBuffer.allocate(length).order(order());
        ByteBuffer[] buffers = nioBuffers(index, length);

        for (ByteBuffer buf: buffers) {
            merged.put(buf);
        }

        merged.flip();
        return merged;
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        checkIndex(index, length);
        if (length == 0) {
            return new ByteBuffer[] { EMPTY_NIO_BUFFER };
        }

        List<ByteBuffer> buffers = new ArrayList<ByteBuffer>(components.size());
        int i = toComponentIndex(index);
        while (length > 0) {
            Component c = components.get(i);
            ByteBuf s = c.buf;
            int adjustment = c.offset;
            int localLength = Math.min(length, s.capacity() - (index - adjustment));
            switch (s.nioBufferCount()) {
                case 0:
                    throw new UnsupportedOperationException();
                case 1:
                    buffers.add(s.nioBuffer(index - adjustment, localLength));
                    break;
                default:
                    Collections.addAll(buffers, s.nioBuffers(index - adjustment, localLength));
            }

            index += localLength;
            length -= localLength;
            i ++;
        }

        return buffers.toArray(new ByteBuffer[buffers.size()]);
    }

    /**
     * Consolidate the composed {@link ByteBuf}s
     */
    public TwoCompositeByteBuf consolidate() {
        ensureAccessible();
        final int numComponents = numComponents();
        if (numComponents <= 1) {
            return this;
        }

        final Component last = components.get(numComponents - 1);
        final int capacity = last.endOffset;
        final ByteBuf consolidated = allocBuffer(capacity);

        for (int i = 0; i < numComponents; i ++) {
            Component c = components.get(i);
            ByteBuf b = c.buf;
            consolidated.writeBytes(b);
            c.freeIfNecessary();
        }

        components.clear();
        components.add(new Component(consolidated));
        updateComponentOffsets(0);
        return this;
    }

    /**
     * Consolidate the composed {@link ByteBuf}s
     *
     * @param cIndex the index on which to start to compose
     * @param numComponents the number of components to compose
     */
    public TwoCompositeByteBuf consolidate(int cIndex, int numComponents) {
        checkComponentIndex(cIndex, numComponents);
        if (numComponents <= 1) {
            return this;
        }

        final int endCIndex = cIndex + numComponents;
        final Component last = components.get(endCIndex - 1);
        final int capacity = last.endOffset - components.get(cIndex).offset;
        final ByteBuf consolidated = allocBuffer(capacity);

        for (int i = cIndex; i < endCIndex; i ++) {
            Component c = components.get(i);
            ByteBuf b = c.buf;
            consolidated.writeBytes(b);
            c.freeIfNecessary();
        }

        components.removeRange(cIndex + 1, endCIndex);
        components.set(cIndex, new Component(consolidated));
        updateComponentOffsets(cIndex);
        return this;
    }

    /**
     * Discard all {@link ByteBuf}s which are read.
     */
    public TwoCompositeByteBuf discardReadComponents() {
        ensureAccessible();
        final int readerIndex = readerIndex();
        if (readerIndex == 0) {
            return this;
        }

        // Discard everything if (readerIndex = writerIndex = capacity).
        int writerIndex = writerIndex();
        if (readerIndex == writerIndex && writerIndex == capacity()) {
            int size = components.size();
            for (int i = 0; i < size; i++) {
                components.get(i).freeIfNecessary();
            }
            components.clear();
            setIndex(0, 0);
            adjustMarkers(readerIndex);
            return this;
        }

        // Remove read components.
        int firstComponentId = toComponentIndex(readerIndex);
        for (int i = 0; i < firstComponentId; i ++) {
            components.get(i).freeIfNecessary();
        }
        components.removeRange(0, firstComponentId);

        // Update indexes and markers.
        Component first = components.get(0);
        int offset = first.offset;
        updateComponentOffsets(0);
        setIndex(readerIndex - offset, writerIndex - offset);
        adjustMarkers(offset);
        return this;
    }

    @Override
    public TwoCompositeByteBuf discardReadBytes() {
        ensureAccessible();
        final int readerIndex = readerIndex();
        if (readerIndex == 0) {
            return this;
        }

        // Discard everything if (readerIndex = writerIndex = capacity).
        int writerIndex = writerIndex();
        if (readerIndex == writerIndex && writerIndex == capacity()) {
            int size = components.size();
            for (int i = 0; i < size; i++) {
                components.get(i).freeIfNecessary();
            }
            components.clear();
            setIndex(0, 0);
            adjustMarkers(readerIndex);
            return this;
        }

        // Remove read components.
        int firstComponentId = toComponentIndex(readerIndex);
        for (int i = 0; i < firstComponentId; i ++) {
            components.get(i).freeIfNecessary();
        }

        // Remove or replace the first readable component with a new slice.
        Component c = components.get(firstComponentId);
        int adjustment = readerIndex - c.offset;
        if (adjustment == c.length) {
            // new slice would be empty, so remove instead
            firstComponentId++;
        } else {
            Component newC = new Component(c.buf.slice(adjustment, c.length - adjustment));
            components.set(firstComponentId, newC);
        }

        components.removeRange(0, firstComponentId);

        // Update indexes and markers.
        updateComponentOffsets(0);
        setIndex(0, writerIndex - readerIndex);
        adjustMarkers(readerIndex);
        return this;
    }

    private ByteBuf allocBuffer(int capacity) {
        return direct ? alloc().directBuffer(capacity) : alloc().heapBuffer(capacity);
    }

    @Override
    public String toString() {
        String result = super.toString();
        result = result.substring(0, result.length() - 1);
        return result + ", components=" + components.size() + ')';
    }

    private static final class Component {
        ByteBuf buf;
        int length;
        int offset;
        int endOffset;

        Component(ByteBuf buf) {
            this.buf = buf;
            length = buf.readableBytes();
        }

        void freeIfNecessary() {
            buf.release(); // We should not get a NPE here. If so, it must be a bug.
        }
    }

    @Override
    public TwoCompositeByteBuf readerIndex(int readerIndex) {
        return (TwoCompositeByteBuf) super.readerIndex(readerIndex);
    }

    @Override
    public TwoCompositeByteBuf writerIndex(int writerIndex) {
        return (TwoCompositeByteBuf) super.writerIndex(writerIndex);
    }

    @Override
    public TwoCompositeByteBuf setIndex(int readerIndex, int writerIndex) {
        return (TwoCompositeByteBuf) super.setIndex(readerIndex, writerIndex);
    }

    @Override
    public TwoCompositeByteBuf clear() {
        return (TwoCompositeByteBuf) super.clear();
    }

    @Override
    public TwoCompositeByteBuf markReaderIndex() {
        return (TwoCompositeByteBuf) super.markReaderIndex();
    }

    @Override
    public TwoCompositeByteBuf resetReaderIndex() {
        return (TwoCompositeByteBuf) super.resetReaderIndex();
    }

    @Override
    public TwoCompositeByteBuf markWriterIndex() {
        return (TwoCompositeByteBuf) super.markWriterIndex();
    }

    @Override
    public TwoCompositeByteBuf resetWriterIndex() {
        return (TwoCompositeByteBuf) super.resetWriterIndex();
    }

    @Override
    public TwoCompositeByteBuf ensureWritable(int minWritableBytes) {
        return (TwoCompositeByteBuf) super.ensureWritable(minWritableBytes);
    }

    @Override
    public TwoCompositeByteBuf getBytes(int index, ByteBuf dst) {
        return (TwoCompositeByteBuf) super.getBytes(index, dst);
    }

    @Override
    public TwoCompositeByteBuf getBytes(int index, ByteBuf dst, int length) {
        return (TwoCompositeByteBuf) super.getBytes(index, dst, length);
    }

    @Override
    public TwoCompositeByteBuf getBytes(int index, byte[] dst) {
        return (TwoCompositeByteBuf) super.getBytes(index, dst);
    }

    @Override
    public TwoCompositeByteBuf setBoolean(int index, boolean value) {
        return (TwoCompositeByteBuf) super.setBoolean(index, value);
    }

    @Override
    public TwoCompositeByteBuf setChar(int index, int value) {
        return (TwoCompositeByteBuf) super.setChar(index, value);
    }

    @Override
    public TwoCompositeByteBuf setFloat(int index, float value) {
        return (TwoCompositeByteBuf) super.setFloat(index, value);
    }

    @Override
    public TwoCompositeByteBuf setDouble(int index, double value) {
        return (TwoCompositeByteBuf) super.setDouble(index, value);
    }

    @Override
    public TwoCompositeByteBuf setBytes(int index, ByteBuf src) {
        return (TwoCompositeByteBuf) super.setBytes(index, src);
    }

    @Override
    public TwoCompositeByteBuf setBytes(int index, ByteBuf src, int length) {
        return (TwoCompositeByteBuf) super.setBytes(index, src, length);
    }

    @Override
    public TwoCompositeByteBuf setBytes(int index, byte[] src) {
        return (TwoCompositeByteBuf) super.setBytes(index, src);
    }

    @Override
    public TwoCompositeByteBuf setZero(int index, int length) {
        return (TwoCompositeByteBuf) super.setZero(index, length);
    }

    @Override
    public TwoCompositeByteBuf readBytes(ByteBuf dst) {
        return (TwoCompositeByteBuf) super.readBytes(dst);
    }

    @Override
    public TwoCompositeByteBuf readBytes(ByteBuf dst, int length) {
        return (TwoCompositeByteBuf) super.readBytes(dst, length);
    }

    @Override
    public TwoCompositeByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return (TwoCompositeByteBuf) super.readBytes(dst, dstIndex, length);
    }

    @Override
    public TwoCompositeByteBuf readBytes(byte[] dst) {
        return (TwoCompositeByteBuf) super.readBytes(dst);
    }

    @Override
    public TwoCompositeByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return (TwoCompositeByteBuf) super.readBytes(dst, dstIndex, length);
    }

    @Override
    public TwoCompositeByteBuf readBytes(ByteBuffer dst) {
        return (TwoCompositeByteBuf) super.readBytes(dst);
    }

    @Override
    public TwoCompositeByteBuf readBytes(OutputStream out, int length) throws IOException {
        return (TwoCompositeByteBuf) super.readBytes(out, length);
    }

    @Override
    public TwoCompositeByteBuf skipBytes(int length) {
        return (TwoCompositeByteBuf) super.skipBytes(length);
    }

    @Override
    public TwoCompositeByteBuf writeBoolean(boolean value) {
        return (TwoCompositeByteBuf) super.writeBoolean(value);
    }

    @Override
    public TwoCompositeByteBuf writeByte(int value) {
        return (TwoCompositeByteBuf) super.writeByte(value);
    }

    @Override
    public TwoCompositeByteBuf writeShort(int value) {
        return (TwoCompositeByteBuf) super.writeShort(value);
    }

    @Override
    public TwoCompositeByteBuf writeMedium(int value) {
        return (TwoCompositeByteBuf) super.writeMedium(value);
    }

    @Override
    public TwoCompositeByteBuf writeInt(int value) {
        return (TwoCompositeByteBuf) super.writeInt(value);
    }

    @Override
    public TwoCompositeByteBuf writeLong(long value) {
        return (TwoCompositeByteBuf) super.writeLong(value);
    }

    @Override
    public TwoCompositeByteBuf writeChar(int value) {
        return (TwoCompositeByteBuf) super.writeChar(value);
    }

    @Override
    public TwoCompositeByteBuf writeFloat(float value) {
        return (TwoCompositeByteBuf) super.writeFloat(value);
    }

    @Override
    public TwoCompositeByteBuf writeDouble(double value) {
        return (TwoCompositeByteBuf) super.writeDouble(value);
    }

    @Override
    public TwoCompositeByteBuf writeBytes(ByteBuf src) {
        return (TwoCompositeByteBuf) super.writeBytes(src);
    }

    @Override
    public TwoCompositeByteBuf writeBytes(ByteBuf src, int length) {
        return (TwoCompositeByteBuf) super.writeBytes(src, length);
    }

    @Override
    public TwoCompositeByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return (TwoCompositeByteBuf) super.writeBytes(src, srcIndex, length);
    }

    @Override
    public TwoCompositeByteBuf writeBytes(byte[] src) {
        return (TwoCompositeByteBuf) super.writeBytes(src);
    }

    @Override
    public TwoCompositeByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return (TwoCompositeByteBuf) super.writeBytes(src, srcIndex, length);
    }

    @Override
    public TwoCompositeByteBuf writeBytes(ByteBuffer src) {
        return (TwoCompositeByteBuf) super.writeBytes(src);
    }

    @Override
    public TwoCompositeByteBuf writeZero(int length) {
        return (TwoCompositeByteBuf) super.writeZero(length);
    }

    @Override
    public TwoCompositeByteBuf retain(int increment) {
        return (TwoCompositeByteBuf) super.retain(increment);
    }

    @Override
    public TwoCompositeByteBuf retain() {
        return (TwoCompositeByteBuf) super.retain();
    }

    @Override
    public TwoCompositeByteBuf touch() {
        return this;
    }

    @Override
    public TwoCompositeByteBuf touch(Object hint) {
        return this;
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return nioBuffers(readerIndex(), readableBytes());
    }

    @Override
    public TwoCompositeByteBuf discardSomeReadBytes() {
        return discardReadComponents();
    }

    @Override
    protected void deallocate() {
        if (freed) {
            return;
        }

        freed = true;
        int size = components.size();
        // We're not using foreach to avoid creating an iterator.
        // see https://github.com/netty/netty/issues/2642
        for (int i = 0; i < size; i++) {
            components.get(i).freeIfNecessary();
        }
    }

    @Override
    public ByteBuf unwrap() {
        return null;
    }

    private final class CompositeByteBufIterator implements Iterator<ByteBuf> {
        private final int size = components.size();
        private int index;

        @Override
        public boolean hasNext() {
            return size > index;
        }

        @Override
        public ByteBuf next() {
            if (size != components.size()) {
                throw new ConcurrentModificationException();
            }
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            try {
                return components.get(index++).buf;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Read-Only");
        }
    }

    private static final class ComponentList extends ArrayList<Component> {

        ComponentList(int initialCapacity) {
            super(initialCapacity);
        }

        // Expose this methods so we not need to create a new subList just to remove a range of elements.
        @Override
        public void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
        }
    }
}
