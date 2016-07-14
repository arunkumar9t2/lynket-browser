package arun.com.chromer.webheads.tasks;

import de.jetwick.snacktory.JResult;

/**
 * Created by Arun on 15/05/2016.
 */
public class PageExtractTask implements PageExtractRunnable.PageExtractTaskMethods {
    private static PageExtractTasksManager sTaskManager;
    private final Runnable mPageExtractRunnable;
    private String mRawUrl;
    private String mUnShortenedUrl;
    private JResult mExtractionResult;
    private Thread mCurrentThread;

    PageExtractTask() {
        // Create the runnables
        mPageExtractRunnable = new PageExtractRunnable(this);
        sTaskManager = PageExtractTasksManager.getInstance();
    }

    @Override
    public void setDownloadThread(Thread currentThread) {
        setCurrentThread(currentThread);
    }

    @Override
    public JResult getResult() {
        return mExtractionResult;
    }

    @Override
    public void setResult(JResult result) {
        mExtractionResult = result;
    }

    private void handleState(int state) {
        sTaskManager.handleState(this, state);
    }

    @Override
    public void handleExtractionState(int state) {
        handleState(state);
    }

    public String getRawUrl() {
        return mRawUrl;
    }

    @Override
    public String getUnShortenedUrl() {
        return mUnShortenedUrl;
    }

    @Override
    public void setUnShortenedUrl(String url) {
        mUnShortenedUrl = url;
    }

    public Thread getCurrentThread() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (sTaskManager) {
            return mCurrentThread;
        }
    }

    private void setCurrentThread(Thread thread) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (sTaskManager) {
            mCurrentThread = thread;
        }
    }

    public Runnable getPageExtractRunnable() {
        return mPageExtractRunnable;
    }

    public void initializeDownloaderTask(String pageUrl) {
        mRawUrl = pageUrl;
        mUnShortenedUrl = pageUrl;
    }

    public void recycle() {
        mRawUrl = null;
        mUnShortenedUrl = null;
        mExtractionResult = null;
    }

    @Override
    public String toString() {
        return mRawUrl == null ? null : "empty";
    }
}
