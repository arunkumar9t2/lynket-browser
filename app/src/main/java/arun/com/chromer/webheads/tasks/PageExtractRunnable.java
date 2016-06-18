package arun.com.chromer.webheads.tasks;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;
import timber.log.Timber;

/**
 * Created by Arun on 15/05/2016.
 */
@SuppressWarnings("WeakerAccess")
public class PageExtractRunnable implements Runnable {

    private final PageExtractTaskMethods mPageTask;

    PageExtractRunnable(PageExtractTaskMethods pageTask) {
        mPageTask = pageTask;
    }

    @Override
    public void run() {
        mPageTask.setDownloadThread(Thread.currentThread());

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        try {
            cancelIfNeeded();

            HtmlFetcher fetcher = new HtmlFetcher();
            String url = fetcher.unShortenUrl(mPageTask.getRawUrl());
            mPageTask.setUnShortenedUrl(url);
            mPageTask.handleDownloadState(PageExtractTasksManager.URL_UN_SHORTENED);

            JResult res = fetcher.fetchAndExtract(url, 1000 * 10, false);
            cancelIfNeeded();

            mPageTask.setResult(res);
            mPageTask.handleDownloadState(PageExtractTasksManager.EXTRACTION_COMPLETE);

            cancelIfNeeded();
        } catch (InterruptedException ignore) {
            Timber.v("Thread interrupted");
        } catch (Exception e) {
            Timber.e(e.getMessage());
        }
    }

    private void cancelIfNeeded() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    interface PageExtractTaskMethods {

        /**
         * Sets the Thread that this instance is running on
         *
         * @param currentThread the current Thread
         */
        void setDownloadThread(Thread currentThread);

        JResult getResult();

        void setResult(JResult result);

        void handleDownloadState(int state);

        String getRawUrl();

        String getUnShortenedUrl();

        void setUnShortenedUrl(String url);
    }
}
