package arun.com.chromer.data.website;

import android.support.annotation.NonNull;

import arun.com.chromer.data.website.model.WebColor;
import arun.com.chromer.data.website.model.WebSite;
import rx.Observable;

/**
 * Interface definition of Website repository which is responsible for providing
 * {@link WebSite} instances containing useful website information.
 * Will use a combination of disk cache and network parsing to provide requested website's data.
 */
interface BaseWebsiteRepository {
    @NonNull
    Observable<WebSite> getWebsite(@NonNull String url);

    int getWebsiteColorSync(@NonNull final String url);

    @NonNull
    Observable<WebColor> saveWebColor(final String url);
}