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
public class PageExtractTasksManager {

    private static final PageExtractTasksManager sInstance;

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    static final int URL_UN_SHORTENED = 1;
    static final int EXTRACTION_COMPLETE = 2;

    private static ProgressListener mProgressListener = new ProgressListener() {
        @Override
        public void onUrlUnShortened(String originalUrl, String unShortenedUrl) {
            // no op
        }

        @Override
        public void onUrlExtracted(String originalUrl, JResult result) {
            // no op
        }
    };

    static {
        sInstance = new PageExtractTasksManager();
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final BlockingQueue<Runnable> mParseWorkQueue;
    private final Queue<PageExtractTask> mPageExtractTaskQueue;
    // Pool executor
    private final ThreadPoolExecutor mParseThreadPool;
    private final Handler mHandler;

    private PageExtractTasksManager() {
        mParseWorkQueue = new LinkedBlockingQueue<>();
        mParseThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                mParseWorkQueue);

        mPageExtractTaskQueue = new LinkedBlockingQueue<>();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                final PageExtractTask pageExtractTask = (PageExtractTask) msg.obj;
                if (pageExtractTask == null) {
                    return;
                }
                switch (msg.what) {
                    case URL_UN_SHORTENED:
                        mProgressListener.onUrlUnShortened(pageExtractTask.getRawUrl(), pageExtractTask.getUnShortenedUrl());
                        break;
                    case EXTRACTION_COMPLETE:
                        mProgressListener.onUrlExtracted(pageExtractTask.getRawUrl(), pageExtractTask.getResult());
                        recycleTask(pageExtractTask);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    public static PageExtractTasksManager getInstance() {
        return sInstance;
    }

    /**
     * Cancels all Threads in the ThreadPool
     */
    @SuppressWarnings("SameParameterValue")
    public static void cancelAll(boolean alsoRemove) {
        final PageExtractTask[] tasks = new PageExtractTask[sInstance.mPageExtractTaskQueue.size()];

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
    static public void removeExtraction(PageExtractTask pageExtractTask, String pageUrl) {
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

    static public void startExtraction(String url) {
        PageExtractTask pageExtractTask = sInstance.mPageExtractTaskQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == pageExtractTask) {
            pageExtractTask = new PageExtractTask();
        }

        // Initializes the task
        pageExtractTask.initializeDownloaderTask(url);

        sInstance.mParseThreadPool.execute(pageExtractTask.getPageExtractRunnable());
    }

    private void recycleTask(PageExtractTask pageExtractTask) {
        pageExtractTask.recycle();
        // Puts the task object back into the queue for re-use.
        mPageExtractTaskQueue.offer(pageExtractTask);
    }

    void handleState(PageExtractTask photoTask, int state) {
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

    static public void registerListener(ProgressListener listener) {
        mProgressListener = listener;
    }

    static public void unRegisterListener() {
        mProgressListener = new ProgressListener() {
            @Override
            public void onUrlUnShortened(String originalUrl, String unShortenedUrl) {

            }

            @Override
            public void onUrlExtracted(String originalUrl, JResult result) {

            }
        };
    }

    public interface ProgressListener {
        void onUrlUnShortened(final String originalUrl, final String unShortenedUrl);

        void onUrlExtracted(final String originalUrl, final JResult result);
    }
}
