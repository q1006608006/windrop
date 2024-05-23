package top.ivan.jdrop.framework.util;

import com.fasterxml.jackson.databind.util.ArrayIterator;
import lombok.NonNull;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class CircularQueue<T> extends AbstractQueue<T> {
    private final int capacity;
    private final AtomicInteger head; //队列头部
    private final AtomicInteger tail;//队列尾
    private final AtomicReferenceArray<T> buff; //模拟队列的数组


    public CircularQueue(int capacity) {
        this.capacity = capacity;
        this.buff = new AtomicReferenceArray<>(capacity);
        this.head = new AtomicInteger(0);
        this.tail = new AtomicInteger(0);
    }

    public boolean isFull() {
        return head.get() == tail.get() && peek() != null;
    }

    //添加数据到队列
    public boolean enqueue(@NonNull T item) {
        while (true) {
            int curHead = head.get();
            if (buff.compareAndSet(curHead, null, item)) {
                head.set((curHead + 1) % capacity);
                return true;
            } else if (curHead == tail.get()) {
                return false;
            }
        }
    }

    //数据出队列
    public T dequeue() {
        while (true) {
            int curTail = tail.get();
            T tar = buff.get(curTail);
            if (null == tar) {
                return null;
            }
            if (buff.compareAndSet(curTail, tar, null)) {
                tail.set((curTail + 1) % capacity);
                return tar;
            }
        }
    }

    public T peek() {
        return buff.get(tail.get());
    }

    public Object[] toArray() {
        int size = size();
        int curTail = tail.get();
        Object[] result = new Object[size];
        for (int offset = 0; offset < size; offset++) {
            result[offset] = buff.get((curTail + offset) % capacity);
        }
        return result;
    }

    @Override
    public boolean offer(@NonNull T item) {
        return enqueue(item);
    }

    @Override
    public T poll() {
        return dequeue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return new ArrayIterator<>((T[]) toArray());
    }


    @Override
    public int size() {
        int test = (head.get() - tail.get() + capacity) % capacity;
        if (test == 0) {
            return peek() == null ? 0 : capacity;
        } else {
            return test;
        }
    }


}