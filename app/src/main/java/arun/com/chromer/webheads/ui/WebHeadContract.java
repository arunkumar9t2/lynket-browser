package arun.com.chromer.webheads.ui;

import android.support.annotation.NonNull;

import com.facebook.rebound.Spring;

import arun.com.chromer.webheads.ui.views.WebHead;

/**
 * Created by Arun on 08/08/2016.
 */
public interface WebHeadContract {
    void onWebHeadClick(@NonNull WebHead webHead);

    void onWebHeadDestroyed(@NonNull WebHead webHead, boolean isLastWebHead);

    void onMasterWebHeadMoved(int x, int y);

    @NonNull
    Spring newSpring();

    void onMasterLockedToTrashy();

    void onMasterReleasedFromTrashy();

    void closeAll();

    void onMasterLongClick();
}
