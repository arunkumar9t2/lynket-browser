/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package in.arunkumarsampath.diskcache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cache implementation to store {@link Parcelable} objects.
 * <p>
 * Implementation modified from https://gist.github.com/VladSumtsov/c4af1f4b8fe5099ca809
 *
 * @param <T> Parcelable type.
 */
public final class ParcelDiskCache<T extends Parcelable> implements DiskCache<T> {

    private static final String LIST = "list";
    private static final String PARCELABLE = "parcelable";
    private static final String VALIDATE_KEY_REGEX = "[a-z0-9_-]{1,5}";
    private static final int MAX_KEY_SYMBOLS = 120;
    private final ClassLoader classLoader;
    private final Executor storeExecutor;
    private DiskLruCache cache;
    private boolean saveInUI = true;

    private ParcelDiskCache(Context context, ClassLoader classLoader, String name, long maxSize) throws IOException {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        this.classLoader = classLoader;
        storeExecutor = Executors.newSingleThreadExecutor();
        File dir = new File(cacheDir, name);
        int version = getVersionCode(context) + Build.VERSION.SDK_INT;
        this.cache = DiskLruCache.open(dir, version, 1, maxSize);
    }

    public static <T extends Parcelable> ParcelDiskCache<T> open(Context context, ClassLoader classLoader, String name, long maxSize) throws IOException {
        return new ParcelDiskCache<>(context, classLoader, name, maxSize);
    }

    private static void saveValue(DiskLruCache cache, Parcel value, String key) {
        if (cache == null) return;
        key = key.toLowerCase();
        try {
            final String skey = key.intern();
            synchronized (skey) {
                DiskLruCache.Editor editor = cache.edit(key);
                OutputStream outputStream = editor.newOutputStream(0);
                writeBytesToStream(outputStream, value.marshall());
                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            value.recycle();
        }
    }

    private static byte[] getBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            byte[] data = new byte[1024];
            int count;
            while ((count = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, count);
            }
            buffer.flush();
            return buffer.toByteArray();
        } finally {
            is.close();
            buffer.close();

        }
    }

    public static void writeBytesToStream(OutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }

    public static int getVersionCode(Context context) {
        int result = 0;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            result = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public T set(String key, T value) {
        key = validateKey(key);
        Parcel parcel = Parcel.obtain();
        parcel.writeString(PARCELABLE);
        parcel.writeParcelable(value, 0);
        if (saveInUI) {
            saveValue(cache, parcel, key);
        } else {
            storeExecutor.execute(new StoreParcelableValueTask(cache, parcel, key));
        }
        return value;
    }

    public void set(String key, List<T> values) {
        key = validateKey(key);
        Parcel parcel = Parcel.obtain();
        parcel.writeString(LIST);
        parcel.writeList(values);
        if (saveInUI) {
            saveValue(cache, parcel, key);
        } else {
            storeExecutor.execute(new StoreParcelableValueTask(cache, parcel, key));
        }
    }

    public T get(String key) {
        key = validateKey(key);
        Parcel parcel = getParcel(key);
        if (parcel != null) {
            try {
                final String type = parcel.readString();
                if (type != null && !type.equals(PARCELABLE)) {
                    throw new IllegalAccessError("Parcel doesn't contain parcelable data");
                }
                if (type != null && type.equals(LIST)) {
                    throw new IllegalAccessError("get list data with getList method");
                }
                return parcel.readParcelable(classLoader);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                parcel.recycle();
            }
        }
        return null;
    }

    private Parcel getParcel(String key) {
        key = validateKey(key);
        byte[] value = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = cache.get(key);
            if (snapshot == null) {
                return null;
            }
            value = getBytesFromStream(snapshot.getInputStream(0));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        Parcel parcel = Parcel.obtain();
        if (value != null) {
            parcel.unmarshall(value, 0, value.length);
            parcel.setDataPosition(0);
        }
        return parcel;
    }

    private String validateKey(String key) {
        Matcher keyMatcher = getPattern(VALIDATE_KEY_REGEX).matcher(key);
        StringBuilder newKey = new StringBuilder();
        while (keyMatcher.find()) {
            String group = keyMatcher.group();
            if (newKey.length() + group.length() > MAX_KEY_SYMBOLS) {
                break;
            }

            newKey.append(group);
        }
        return newKey.toString().toLowerCase();
    }

    public Pattern getPattern(String bodyRegex) {
        int flags = Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE;
        return Pattern.compile(bodyRegex, flags);
    }

    public List<T> getList(String key, Class itemClass) {
        key = validateKey(key);
        ArrayList<T> res = new ArrayList<>();
        Parcel parcel = getParcel(key);
        if (parcel != null) {
            try {
                String type = parcel.readString();
                if (type.equals(PARCELABLE)) {
                    throw new IllegalAccessError("Get not a list data with get method");
                }
                if (!type.equals(LIST)) {
                    throw new IllegalAccessError("Parcel doesn't contain list data");
                }
                parcel.readList(res, itemClass != null ? itemClass.getClassLoader() : ArrayList.class.getClassLoader());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                parcel.recycle();
            }
        }
        return res;
    }

    public List<T> getList(String key) {
        return getList(key, null);
    }

    public boolean remove(String key) {
        key = validateKey(key);
        try {
            return cache.remove(key.toLowerCase());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<T> getAll() {
        return getAll(null);
    }

    public List<T> getAll(String prefix) {
        List<T> list = new ArrayList<>(1);
        File dir = cache.getDirectory();
        File[] files = dir.listFiles();
        if (files != null) {
            list = new ArrayList<>(files.length);
            for (File file : files) {
                String fileName = file.getName();
                if ((!TextUtils.isEmpty(prefix) && fileName.startsWith(prefix) && fileName.indexOf(".") > 0)
                        || (TextUtils.isEmpty(prefix) && fileName.indexOf(".") > 0)) {
                    String key = fileName.substring(0, fileName.indexOf("."));
                    T value = get(key);
                    list.add(value);
                }
            }
        }
        return list;
    }

    public void clear() {
        try {
            cache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(String key) {
        key = validateKey(key);
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = cache.get(key.toLowerCase());
            return snapshot != null && snapshot.getLength(0) > 0;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        return false;
    }

    @Override
    public void close() {
        try {
            cache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shouldSaveInUI() {
        this.saveInUI = true;
    }

    private static class StoreParcelableValueTask implements Runnable {

        private final DiskLruCache cache;
        private final Parcel value;
        private final String key;

        StoreParcelableValueTask(DiskLruCache cache, Parcel value, String key) {
            this.value = value;
            this.key = key;
            this.cache = cache;
        }

        @Override
        public void run() {
            saveValue(cache, value, key);
        }
    }
}