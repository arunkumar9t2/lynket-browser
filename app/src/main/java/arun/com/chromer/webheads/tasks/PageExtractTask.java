package arun.com.chromer.webheads.tasks;

import de.jetwick.snacktory.JResult;

/**
 * Created by Arun on 15/05/2016.
 */
public class PageExtractTask implements PageExtractRunnable.PageExtractTaskMethods {
    private static ExtractionTasksManager sTaskManager;

    private String mRawUrl;
    private String mUnShortenedUrl;
    private JResult mExtractionResult;

    private Thread mCurrentThread;

    private Runnable mPageExtractRunnable;

    PageExtractTask() {
        // Create the runnables
        mPageExtractRunnable = new PageExtractRunnable(this);
        sTaskManager = ExtractionTasksManager.getInstance();
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

    void handleState(int state) {
        sTaskManager.handleState(this, state);
    }

    @Override
    public void handleDownloadState(int state) {
        handleState(state);
    }

    public String getRawUrl() {
        return mRawUrl;
    }

    @Override
    public void setUnShortenedUrl(String url) {
        mUnShortenedUrl = url;
    }

    @Override
    public String getUnShortenedUrl() {
        return mUnShortenedUrl;
    }

    public Thread getCurrentThread() {
        synchronized (sTaskManager) {
            return mCurrentThread;
        }
    }

    public void setCurrentThread(Thread thread) {
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
}
