package com.example.openradiofm.data.source;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RootRDSSource {

    private int lastFreq = -1;
    private String lastRdsName = null;
    private long lastCheckTime = 0;

    private java.lang.Process mRootProcess;
    private java.io.DataOutputStream mDos;
    private java.io.BufferedReader mReader;

    public RootRDSSource() {
        initShell();
    }

    private void initShell() {
        try {
            mRootProcess = Runtime.getRuntime().exec("su");
            mDos = new java.io.DataOutputStream(mRootProcess.getOutputStream());
            mReader = new java.io.BufferedReader(new java.io.InputStreamReader(mRootProcess.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized String getRdsName(int freqKHz) {
        long now = System.currentTimeMillis();
        if (freqKHz == lastFreq && (now - lastCheckTime) < 2000) {
            return lastRdsName;
        }

        try {
            if (mRootProcess == null)
                initShell();
            if (mRootProcess == null)
                return null;

            lastFreq = freqKHz;
            lastCheckTime = now;

            mDos.writeBytes("cat /data/data/com.hcn.autoradio/shared_prefs/radio_rds.xml\n");
            mDos.writeBytes("echo __END__\n");
            mDos.flush();

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = mReader.readLine()) != null) {
                if (line.trim().equals("__END__"))
                    break;
                sb.append(line);
            }

            String xml = sb.toString();
            String search = "name=\"" + freqKHz + "\">";

            int start = xml.indexOf(search);
            if (start != -1) {
                start += search.length();
                int end = xml.indexOf("</string>", start);
                if (end != -1) {
                    lastRdsName = xml.substring(start, end);
                    return lastRdsName;
                }
            }
            lastRdsName = null;
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            mRootProcess = null;
            return null;
        }
    }
}
