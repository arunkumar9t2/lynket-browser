/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.data.apps.store;

import android.app.Application;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.apps.model.Provider;
import arun.com.chromer.data.common.App;
import arun.com.chromer.data.common.BookStore;
import arun.com.chromer.util.Utils;
import io.paperdb.Book;
import io.paperdb.Paper;
import rx.Observable;
import timber.log.Timber;

@Singleton
public class AppDiskStore implements AppStore, BookStore {
  private static final String APP_BOOK_NAME = "APPS";
  private final Application application;

  @Inject
  AppDiskStore(Application application) {
    this.application = application;
  }

  @NonNull
  @Override
  public Book getBook() {
    return Paper.book(APP_BOOK_NAME);
  }

  @NonNull
  @Override
  public Observable<App> getApp(@NonNull final String packageName) {
    return Observable.fromCallable(() -> {
      App app = Utils.createApp(application, packageName);
      try {
        app = getBook().read(packageName, app);
      } catch (Exception e) {
        try {
          getBook().delete(packageName);
        } catch (Exception ignored) {
        }
      }
      return app;
    });
  }

  @NonNull
  @Override
  public Observable<App> saveApp(@NonNull App app) {
    return Observable.just(app)
      .flatMap(app1 -> {
        getBook().write(app1.packageName, app1);
        Timber.d("Wrote %s to storage", app1.packageName);
        return Observable.just(app1);
      });
  }

  @Override
  public boolean isPackageBlacklisted(@NonNull String packageName) {
    return getBook().contains(packageName) && getApp(packageName).toBlocking().first().blackListed;
  }

  @NonNull
  @Override
  public Observable<App> setPackageBlacklisted(@NonNull final String packageName) {
    return getApp(packageName)
      .flatMap(app -> {
        app.blackListed = true;
        app.incognito = false;
        Timber.d("Set %s as blacklisted", app.packageName);
        return saveApp(app);
      });
  }

  @Override
  public int getPackageColorSync(@NonNull String packageName) {
    return getApp(packageName).toBlocking().first().color;
  }

  @NonNull
  @Override
  public Observable<Integer> getPackageColor(@NonNull String packageName) {
    return getApp(packageName)
      .map(app -> {
        Timber.d("Got %d color for %s from storage", app.color, app.packageName);
        return app.color;
      });
  }

  @NonNull
  @Override
  public Observable<App> setPackageColor(@NonNull final String packageName, final int color) {
    return getApp(packageName)
      .flatMap(app -> {
        app.color = color;
        Timber.d("Saved %d color for %s", color, app.packageName);
        return saveApp(app);
      });
  }

  @NonNull
  @Override
  public Observable<App> removeBlacklist(@NonNull final String packageName) {
    return getApp(packageName)
      .flatMap(app -> {
        app.blackListed = false;
        Timber.d("Blacklist removed %s", app.packageName);
        return saveApp(app);
      });
  }

  @NotNull
  @Override
  public Observable<App> removeIncognito(@NotNull String packageName) {
    return getApp(packageName)
      .flatMap(app -> {
        app.incognito = false;
        Timber.d("Incognito removed %s", app.packageName);
        return saveApp(app);
      });
  }

  @NotNull
  @Override
  public Observable<App> getInstalledApps() {
    return Observable.empty();
  }

  @Override
  public boolean isPackageIncognito(@NotNull String packageName) {
    return getBook().contains(packageName) && getApp(packageName).toBlocking().first().incognito;
  }

  @NotNull
  @Override
  public Observable<App> setPackageIncognito(@NotNull String packageName) {
    return getApp(packageName)
      .flatMap(app -> {
        app.incognito = true;
        app.blackListed = false;
        Timber.d("Set %s as incognito", app.packageName);
        return saveApp(app);
      });
  }

  @NotNull
  @Override
  public Observable<List<Provider>> allProviders() {
    return Observable.empty();
  }
}
