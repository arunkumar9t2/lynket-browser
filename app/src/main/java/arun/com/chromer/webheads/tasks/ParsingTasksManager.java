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
import timber.log.Timber;

/**
 * Created by Arun on 15/05/2016.
 */
public class ParsingTasksManager {
    private static final ParsingTasksManager sInstance;

    @SuppressWarnings("FieldCanBeLocal")
    private final BlockingQueue<Runnable> mParseWorkQueue;
    private final Queue<PageExtractTask> mPageExtractTaskQueue;

    // Pool executor
    private final ThreadPoolExecutor mParseThreadPool;

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    static final int URL_UN_SHORTENED = 1;
    static final int EXTRACTION_COMPLETE = 2;

    private static ProgressListener mProgressListener;

    private final Handler mHandler;

    public interface ProgressListener {
        void onUrlUnShortened(String originalUrl, String unShortenedUrl);

        void onUrlExtracted(String originalUrl, JResult result);
    }

    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sInstance = new ParsingTasksManager();
    }

    private ParsingTasksManager() {
        mParseWorkQueue = new LinkedBlockingQueue<>();
        mParseThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES + 1,
                NUMBER_OF_CORES + 1,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mParseWorkQueue);
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

    public static ParsingTasksManager getInstance() {
        return sInstance;
    }

    /**
     * Cancels all Threads in the ThreadPool
     */
    @SuppressWarnings("SameParameterValue")
    public static void cancelAll(boolean alsoRemove) {
        PageExtractTask[] tasks = new PageExtractTask[sInstance.mPageExtractTaskQueue.size()];

        //noinspection SuspiciousToArrayCall
        sInstance.mPageExtractTaskQueue.toArray(tasks);

        synchronized (sInstance) {

            // Iterates over the array of tasks
            for (PageExtractTask task : tasks) {

                // Gets the task's current thread
                Thread thread = task.getCurrentThread();

                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }

                if (alsoRemove) {
                    sInstance.mParseThreadPool.remove(task.getPageExtractRunnable());
                    Timber.d("Removed task %s", task.toString());
                }
            }
        }
    }

    /**
     * Stops an extraction thread and removes it from the thread pool
     *
     * @param pageExtractTask The page extract task associated with the Thread
     * @param pageUrl         The url to be extracted
     */
    static public void removeDownload(PageExtractTask pageExtractTask, String pageUrl) {
        // If the Thread object still exists and the download matches the specified URL

        if (pageExtractTask != null && pageExtractTask.getRawUrl().equals(pageUrl)) {
            synchronized (sInstance) {
                // Gets the Thread that the downloader task is running on
                Thread thread = pageExtractTask.getCurrentThread();

                // If the Thread exists, posts an interrupt to it
                if (null != thread)
                    thread.interrupt();

                sInstance.mParseThreadPool.remove(pageExtractTask.getPageExtractRunnable());
                Timber.d("Removed task %s", pageExtractTask.toString());
            }
        }
    }

    static public void startDownload(String url) {
        PageExtractTask downloadTask = sInstance.mPageExtractTaskQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == downloadTask) {
            downloadTask = new PageExtractTask();
        }

        // Initializes the task
        downloadTask.initializeDownloaderTask(url);

        sInstance.mParseThreadPool.execute(downloadTask.getPageExtractRunnable());
    }

    private void recycleTask(PageExtractTask downloadTask) {
        downloadTask.recycle();

        // Puts the task object back into the queue for re-use.
        mPageExtractTaskQueue.offer(downloadTask);
    }

    static public void registerListener(ProgressListener listener) {
        mProgressListener = listener;
    }

    static public void unRegisterListener() {
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
