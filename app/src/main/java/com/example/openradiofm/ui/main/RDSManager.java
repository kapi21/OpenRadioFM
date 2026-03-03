package com.example.openradiofm.ui.main;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.openradiofm.R;
import com.example.openradiofm.data.source.RdsDatabase;
import com.example.openradiofm.utils.MetadataUtils;
import com.example.openradiofm.utils.PtyManager;

/**
 * Fase 3: Gestor de RDS y PTY.
 * Centraliza la visualización de nombres de emisoras, RT (Radio Text) y códigos PTY.
 */
public class RDSManager {
    private static final String TAG = "RDSManager";

    private final Context mContext;
    private final TextView tvRdsName;
    private final TextView tvRdsInfo;
    private final TextView tvPty;
    private final ImageView ivPtyIcon;
    private final RdsDatabase mRdsDb;
    private final RDSListener mListener;

    private String mCurrentPi;
    private String mCurrentPty;
    private boolean mHasRdsLock = false;

    public interface RDSListener {
        void onRdsNameConfirmed(String name);
        void onRdsMetadataUpdated();
        int getCurrentFrequency();
        int getCurrentBand();
    }

    public RDSManager(Context context, View rootView, RdsDatabase rdsDb, RDSListener listener) {
        this.mContext = context;
        this.mRdsDb = rdsDb;
        this.mListener = listener;

        this.tvRdsName = rootView.findViewById(R.id.tvRdsName);
        this.tvRdsInfo = rootView.findViewById(R.id.tvRdsInfo);
        this.tvPty = rootView.findViewById(R.id.tvPty);
        this.ivPtyIcon = rootView.findViewById(R.id.ivPtyIcon);
    }

    public void onRdsName(String name) {
        if (tvRdsName != null && name != null && !name.isEmpty()) {
            tvRdsName.setText(name);
            tvRdsName.setVisibility(View.VISIBLE);
            mHasRdsLock = true;

            if (mCurrentPi != null && mRdsDb != null) {
                mRdsDb.savePiName(mCurrentPi, name);
            }

            if (mListener != null) {
                mListener.onRdsNameConfirmed(name);
            }
        }
    }

    public void onRdsText(String text) {
        String cleanedText = MetadataUtils.cleanRdsText(text);
        if (tvRdsInfo != null) {
            String targetText = (cleanedText == null || cleanedText.isEmpty()) ? "Sin datos RDS RT" : cleanedText;
            String current = tvRdsInfo.getText().toString();
            if (!current.equals(targetText)) {
                tvRdsInfo.setText(targetText);
                tvRdsInfo.setSelected(true);
                tvRdsInfo.setVisibility(View.VISIBLE);
            }
            mHasRdsLock = (cleanedText != null && !cleanedText.isEmpty());
        }
    }

    public void onRdsPty(String pty) {
        mCurrentPty = pty;
        updatePtyUI(pty);
    }

    public void onRdsPi(String piCode) {
        this.mCurrentPi = piCode;
        if (mRdsDb != null) {
            String savedName = mRdsDb.getNameForPi(piCode);
            if (savedName != null && tvRdsName != null) {
                tvRdsName.setText(savedName);
                tvRdsName.setVisibility(View.VISIBLE);
                if (mListener != null) {
                    mListener.onRdsNameConfirmed(savedName);
                }
            }
        }
    }

    public void updatePtyUI(String pty) {
        int ptyCode = 0;
        String displayLabel = mContext.getString(R.string.pty_none);

        if (pty != null && !pty.isEmpty()) {
            try {
                ptyCode = Integer.parseInt(pty.trim());
                if (ptyCode > 0 && ptyCode <= 31) {
                    displayLabel = PtyManager.getPtyLabel(mContext, ptyCode);
                } else {
                    ptyCode = 0;
                }
            } catch (NumberFormatException ignored) {}
        }

        if (tvPty != null) {
            tvPty.setText(displayLabel);
            tvPty.setVisibility(View.VISIBLE);
            // Log.d(TAG, "PTY UI Updated: " + displayLabel + " (Code: " + ptyCode + ")");
        }

        if (ivPtyIcon != null) {
            int iconRes = (ptyCode > 0) ? PtyManager.getPtyIconResource(ptyCode) : 0;
            if (iconRes != 0) {
                ivPtyIcon.setImageResource(iconRes);
                ivPtyIcon.setVisibility(View.VISIBLE);
            } else {
                ivPtyIcon.setVisibility(View.GONE);
            }
        }
    }

    public void reset(boolean clearTexts) {
        mCurrentPi = null;
        mCurrentPty = null;
        mHasRdsLock = false;

        if (clearTexts) {
            if (tvRdsName != null) tvRdsName.setText("");
            if (tvRdsInfo != null) tvRdsInfo.setText("Sin datos RDS RT");
            if (tvPty != null) tvPty.setText(mContext.getString(R.string.pty_none));
            if (ivPtyIcon != null) ivPtyIcon.setVisibility(View.GONE);
        }
    }

    public String getCurrentPty() { return mCurrentPty; }
    public boolean hasRdsLock() { return mHasRdsLock; }
}
