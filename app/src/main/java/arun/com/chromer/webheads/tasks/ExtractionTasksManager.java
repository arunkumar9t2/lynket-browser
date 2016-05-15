package arun.com.chromer.webheads.tasks;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.jetwick.snacktory.JResult;

/**
 * Created by Arun on 15/05/2016.
 */
public class ExtractionTasksManager {
    private static ExtractionTasksManager sInstance = null;

    // A queue of Runnables for page extraction task
    private final BlockingQueue<Runnable> mDownloadWorkQueue;
    private final Queue<PageExtractTask> mPageExtractTaskQueue;
    // Pool executor
    private final ThreadPoolExecutor mExtractThreadPool;

    private static final int CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    static final int URL_UN_SHORTENED = 1;
    static final int EXTRACTION_COMPLETE = 2;

    private static ProgressListener mProgressListener;

    private Handler mHandler;

    public interface ProgressListener {
        void onUrlUnShortened(String originalUrl, String unShortenedUrl);

        void onUrlExtracted(String originalUrl, JResult result);
    }

    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sInstance = new ExtractionTasksManager();
    }

    private ExtractionTasksManager() {
        mDownloadWorkQueue = new LinkedBlockingQueue<>();
        mExtractThreadPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mDownloadWorkQueue);
        mPageExtractTaskQueue = new LinkedBlockingQueue<>();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                PageExtractTask pageExtractTask = (PageExtractTask) msg.obj;

                if (pageExtractTask == null) {
                    return;
                }

                switch (msg.what) {
                    case URL_UN_SHORTENED:
                        if (mProgressListener != null) {
                            mProgressListener.onUrlUnShortened(pageExtractTask.getRawUrl(), pageExtractTask.getUnShortenedUrl());
                        }
                        break;
                    case EXTRACTION_COMPLETE:
                        if (mProgressListener != null) {
                            mProgressListener.onUrlExtracted(pageExtractTask.getRawUrl(), pageExtractTask.getResult());
                        }
                        recycleTask(pageExtractTask);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    public static ExtractionTasksManager getInstance() {
        return sInstance;
    }

    /**
     * Cancels all Threads in the ThreadPool
     */
    public static void cancelAll() {
        PageExtractTask[] tasks = new PageExtractTask[sInstance.mDownloadWorkQueue.size()];

        sInstance.mDownloadWorkQueue.toArray(tasks);

        int arrayLen = tasks.length;

        synchronized (sInstance) {

            // Iterates over the array of tasks
            for (int taskArrayIndex = 0; taskArrayIndex < arrayLen; taskArrayIndex++) {

                // Gets the task's current thread
                Thread thread = tasks[taskArrayIndex].getCurrentThread();

                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }

    /**
     * Stops an extraction thread and removes it from the thread pool
     *
     * @param downloaderTask The page extract task associated with the Thread
     * @param pageUrl        The url to be extracted
     */
    static public void removeDownload(PageExtractTask downloaderTask, String pageUrl) {
        // If the Thread object still exists and the download matches the specified URL

        if (downloaderTask != null && downloaderTask.getRawUrl().equals(pageUrl)) {
            synchronized (sInstance) {
                // Gets the Thread that the downloader task is running on
                Thread thread = downloaderTask.getCurrentThread();

                // If the Thread exists, posts an interrupt to it
                if (null != thread)
                    thread.interrupt();

                sInstance.mExtractThreadPool.remove(downloaderTask.getPageExtractRunnable());
            }
        }
    }

    static public PageExtractTask startDownload(String url) {
        PageExtractTask downloadTask = sInstance.mPageExtractTaskQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == downloadTask) {
            downloadTask = new PageExtractTask();
        }

        // Initializes the task
        downloadTask.initializeDownloaderTask(url);

        sInstance.mExtractThreadPool.execute(downloadTask.getPageExtractRunnable());

        return downloadTask;
    }

    void recycleTask(PageExtractTask downloadTask) {
        downloadTask.recycle();

        // Puts the task object back into the queue for re-use.
        mPageExtractTaskQueue.offer(downloadTask);
    }

    static public void registerListener(ProgressListener listener) {
        mProgressListener = listener;
    }

    static public void unRegisterListener(ProgressListener listener) {
        mProgressListener = null;
    }

    public void handleState(PageExtractTask photoTask, int state) {
        switch (state) {
            case URL_UN_SHORTENED:
                // Later
                break;
            case EXTRACTION_COMPLETE:
                // Later
            default:
                break;
        }
        mHandler.obtainMessage(state, photoTask).sendToTarget();
    }
}
