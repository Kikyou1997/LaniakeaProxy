package com.proxy.client;

import base.crypto.CryptoUtil;
import base.interfaces.Context;
import base.interfaces.Crypto;
import base.interfaces.Listener;
import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * @author kikyou
 * Created at 2020/2/18
 */
@Slf4j
public class ClientContext {

    public int id = -1;
    public byte[] iv = CryptoUtil.generateIv();
    public Crypto crypto = new ClientCryptoImpl();
    private static ClientContext context = new ClientContext();
    private List<WeakReference<Listener>> listenersList = new LinkedList<>();
    private ReferenceQueue<Listener> referenceQueue = new ReferenceQueue<>();

    public static void updateIv(byte[] iv) {
        context.iv = iv;
        context.notifyListeners();
    }

    public static void updateId(int id) {
        context.id = id;
        context.notifyListeners();
    }

    public static void registerListener(Listener listener) {
        WeakReference<Listener> weakReference = new WeakReference<>(listener);
        context.listenersList.add(weakReference);
    }

    public void notifyListeners() {
        for (WeakReference<Listener> listenerRef : context.listenersList) {
            Listener listener = listenerRef.get();
            if (listenerRef.get() != null) {
                listener.notify();
            } else {
                listenerRef.clear();
            }
        }

        Reference toBeRemoved = null;
        do {
            toBeRemoved = referenceQueue.poll();
            listenersList.remove(toBeRemoved);
        } while (toBeRemoved != null);
    }

    public static int getId() {
        return context.id;
    }

    public static byte[] getIv() {
        return context.iv;
    }

    public static Crypto getCrypto() {
        return context.crypto;
    }
}
