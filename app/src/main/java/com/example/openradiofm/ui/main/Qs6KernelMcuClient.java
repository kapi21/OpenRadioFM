package com.example.openradiofm.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

/**
 * Cliente "directo a MCU" para QS6/NWD a través de KernelService.
 *
 * Implementa el método AIDL IKernelFeature.request([B) vía transact, sin depender de stubs AIDL
 * en el proyecto (el dump OEM confirma TRANSACTION_request = 0x1, descriptor
 * "com.nwd.kernel.aidl.IKernelFeature").
 */
public final class Qs6KernelMcuClient {
    private static final String TAG = "Qs6KernelMcuClient";

    private static final String ACTION_KERNEL_SERVICE = "com.nwd.kernel.service.KernelService";
    private static final String KERNEL_PACKAGE = "com.nwd.kernel";

    private static final String I_KERNEL_FEATURE_DESCRIPTOR = "com.nwd.kernel.aidl.IKernelFeature";
    private static final int TRANSACTION_REQUEST = 0x1;

    // KernelProtocal
    private static final byte HEADCODE_F0 = (byte) 0xF0;
    private static final byte TYPE_FM = 0x03;

    private final Context mAppContext;
    private final Object mLock = new Object();
    private IBinder mKernelBinder;
    private boolean mBound;

    public interface Listener {
        void onKernelBindChanged(boolean bound);
    }

    private Listener mListener;

    public Qs6KernelMcuClient(Context context) {
        this.mAppContext = context.getApplicationContext();
    }

    public void setListener(Listener l) {
        this.mListener = l;
    }

    public boolean isBound() {
        synchronized (mLock) {
            return mBound && mKernelBinder != null && mKernelBinder.isBinderAlive();
        }
    }

    public void connect() {
        synchronized (mLock) {
            if (mBound) return;
        }
        try {
            Intent i = new Intent(ACTION_KERNEL_SERVICE);
            // Forzar explícito para evitar restricciones Android 8+
            i.setPackage(KERNEL_PACKAGE);
            boolean ok = mAppContext.bindService(i, mConn, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "bindService(KernelService) ok=" + ok);
        } catch (Throwable t) {
            Log.e(TAG, "connect: bindService failed", t);
            notifyBound(false);
        }
    }

    public void disconnect() {
        try {
            synchronized (mLock) {
                if (!mBound) return;
            }
            mAppContext.unbindService(mConn);
        } catch (Throwable ignored) {
        } finally {
            synchronized (mLock) {
                mBound = false;
                mKernelBinder = null;
            }
            notifyBound(false);
        }
    }

    private void notifyBound(boolean bound) {
        try {
            if (mListener != null) mListener.onKernelBindChanged(bound);
        } catch (Throwable ignored) {}
    }

    private final ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (mLock) {
                mKernelBinder = service;
                mBound = true;
            }
            Log.d(TAG, "KernelService connected: " + name);
            notifyBound(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            synchronized (mLock) {
                mBound = false;
                mKernelBinder = null;
            }
            Log.w(TAG, "KernelService disconnected: " + name);
            notifyBound(false);
        }
    };

    /**
     * Envía un frame KernelProtocal ya construido.
     */
    public void requestRaw(byte[] frame) throws RemoteException {
        IBinder b;
        synchronized (mLock) {
            b = mKernelBinder;
        }
        if (b == null || !b.isBinderAlive()) throw new RemoteException("Kernel binder not alive");

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(I_KERNEL_FEATURE_DESCRIPTOR);
            data.writeByteArray(frame);
            b.transact(TRANSACTION_REQUEST, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    // ====== helpers para construir frames FM (type=0x03) ======

    public static byte[] buildFmAction(byte actionType, byte actionValue) {
        // OEM: len=5, dataType=0x01
        byte[] frame = generateNullProtocal(/*aLength*/ 0x05, /*type*/ TYPE_FM, /*dataType*/ (byte) 0x01);
        int off = getProtocalDataStartOffset();
        frame[off] = actionType;
        frame[off + 1] = actionValue;
        calCheckSumAndWriteEndOfData(frame);
        return frame;
    }

    /**
     * OEM {@code RadioManager.seek}: siempre con actionValue=1.
     * - up:   (1, 1)
     * - down: (2, 1)
     *
     * Nota: en algunas unidades esto se percibe como “paso fino” (~0.5) aunque el método OEM se llame {@code seek}.
     */
    public static byte[] buildFmSeekUp() {
        return buildFmAction((byte) 0x01, (byte) 0x01);
    }

    public static byte[] buildFmSeekDown() {
        return buildFmAction((byte) 0x02, (byte) 0x01);
    }

    /**
     * OEM {@code RadioManager.search}: actionValue=1.
     * - up:   (3, 1)
     * - down: (4, 1)
     *
     * Nota: en algunas unidades esto se percibe como “salto de emisora” aunque el método OEM se llame {@code search}.
     */
    public static byte[] buildFmSearchUp() {
        return buildFmAction((byte) 0x03, (byte) 0x01);
    }

    public static byte[] buildFmSearchDown() {
        return buildFmAction((byte) 0x04, (byte) 0x01);
    }

    /** Cambio de banda (OEM {@code RadioManager.changeBand}): (5, 1). */
    public static byte[] buildFmBandCycle() {
        return buildFmAction((byte) 0x05, (byte) 0x01);
    }

    /** AMS (OEM {@code RadioManager.AMS}): (6, 1). */
    public static byte[] buildFmAms() {
        return buildFmAction((byte) 0x06, (byte) 0x01);
    }

    /** INTRO (OEM {@code RadioManager.INTRO}): (7, 1). */
    public static byte[] buildFmIntro() {
        return buildFmAction((byte) 0x07, (byte) 0x01);
    }

    /**
     * Near/DX-LOC (OEM {@code RadioManager.setNearOn}): actionType=8.
     * Nota: en el OEM el “valor” parece invertido (on ->0, off -> 1).
     */
    public static byte[] buildFmNearOn(boolean on) {
        return buildFmAction((byte) 0x08, (byte) (on ? 0x00 : 0x01));
    }

    /**
     * STEREO ON/OFF (experimental).
     *
     * En algunas unidades QS6, el control de estéreo está expuesto por la pila OEM; para ir "MCU first"
     * probamos un dataType dedicado con 1 byte on/off.
     *
     * Nota: si el firmware no soporta este dataType, el motor debe hacer fallback a AIDL.
     */
    public static byte[] buildFmSetStereoOn(boolean on) {
        // Experimental: len=4, dataType=0x10, payload[0]=1/0
        byte[] frame = generateNullProtocal(/*aLength*/ 0x04, /*type*/ TYPE_FM, /*dataType*/ (byte) 0x10);
        int off = getProtocalDataStartOffset();
        frame[off] = (byte) (on ? 1 : 0);
        calCheckSumAndWriteEndOfData(frame);
        return frame;
    }

    public static byte[] buildFmSetBackServiceOn(boolean on) {
        // OEM: len=4, dataType=0x00
        byte[] frame = generateNullProtocal(/*aLength*/ 0x04, /*type*/ TYPE_FM, /*dataType*/ (byte) 0x00);
        int off = getProtocalDataStartOffset();
        frame[off] = (byte) (on ? 1 : 0);
        calCheckSumAndWriteEndOfData(frame);
        return frame;
    }

    public static byte[] buildFmRequestRadioInfo() {
        // OEM: len=4, dataType=0x0E
        byte[] frame = generateNullProtocal(/*aLength*/ 0x04, /*type*/ TYPE_FM, /*dataType*/ (byte) 0x0E);
        calCheckSumAndWriteEndOfData(frame);
        return frame;
    }

    public static byte[] buildFmRequestRadioRdsShowState() {
        // OEM: len=4, dataType=0x0F
        byte[] frame = generateNullProtocal(/*aLength*/ 0x04, /*type*/ TYPE_FM, /*dataType*/ (byte) 0x0F);
        calCheckSumAndWriteEndOfData(frame);
        return frame;
    }

    public static byte[] buildFmTune(int freq, byte bandType, int prefebIndex) {
        // OEM: len=7, dataType=0x03
        byte[] frame = generateNullProtocal(/*aLength*/ 0x07, /*type*/ TYPE_FM, /*dataType*/ (byte) 0x03);
        int off = getProtocalDataStartOffset();
        frame[off] = bandType;
        frame[off + 1] = (byte) (prefebIndex & 0xFF);
        frame[off + 2] = (byte) (freq & 0xFF);
        frame[off + 3] = (byte) ((freq >> 8) & 0xFF);
        calCheckSumAndWriteEndOfData(frame);
        return frame;
    }

    public static byte[] buildFmSetRdsState(int rdsState) {
        // OEM: len=4, dataType=0x08
        byte[] frame = generateNullProtocal(/*aLength*/ 0x04, /*type*/ TYPE_FM, /*dataType*/ (byte) 0x08);
        int off = getProtocalDataStartOffset();
        frame[off] = (byte) (rdsState & 0xFF);
        calCheckSumAndWriteEndOfData(frame);
        return frame;
    }

    public static byte[] buildFmSetPtyIndex(int idx) {
        // OEM: len=4, dataType=0x09
        byte[] frame = generateNullProtocal(/*aLength*/ 0x04, /*type*/ TYPE_FM, /*dataType*/ (byte) 0x09);
        int off = getProtocalDataStartOffset();
        frame[off] = (byte) (idx & 0xFF);
        calCheckSumAndWriteEndOfData(frame);
        return frame;
    }

    public static byte[] buildFmSaveCurrentFrequency(byte bandType, int index, int freq) {
        // OEM: len=6, dataType=0x0C; payload[0]=(band<<4)+index, [1]=freq L, [2]=freq H
        byte[] frame = generateNullProtocal(/*aLength*/ 0x06, /*type*/ TYPE_FM, /*dataType*/ (byte) 0x0C);
        int off = getProtocalDataStartOffset();
        frame[off] = (byte) (((bandType & 0x0F) << 4) + (index & 0x0F));
        frame[off + 1] = (byte) (freq & 0xFF);
        frame[off + 2] = (byte) ((freq >> 8) & 0xFF);
        calCheckSumAndWriteEndOfData(frame);
        return frame;
    }

    // ====== implementación mínima KernelProtocal (según dump OEM) ======

    private static int getProtocalDataStartOffset() {
        return 5;
    }

    private static byte[] generateNullProtocal(int aLength, byte aType, byte aDataType) {
        // OEM: si aLength <= 0xFA: array len = aLength + 3
        // y data[1] = aLength.
        if (aLength > 0xFA) {
            // No lo necesitamos para radio; mantener simple.
            throw new IllegalArgumentException("aLength too large: " + aLength);
        }
        byte[] data = new byte[aLength + 3];
        data[0] = HEADCODE_F0;
        data[1] = (byte) (aLength & 0xFF);
        data[2] = aType;
        data[3] = aDataType;
        // data[4] reservado/0
        return data;
    }

    private static void calCheckSumAndWriteEndOfData(byte[] data) {
        if (data == null || data.length < 2) return;
        int last = data.length - 1;
        data[last] = calCheckSum(data, 0, data.length);
    }

    private static byte calCheckSum(byte[] data, int offset, int len) {
        byte result = 0;
        // OEM: suma bytes desde offset+1 hasta len-2 (excluye head y excluye checksum final)
        for (int i = offset + 1; i < len - 1; i++) {
            result = (byte) (result + data[i]);
        }
        return result;
    }
}

