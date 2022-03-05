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

package arun.com.chromer.data.history

import android.app.Application
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import arun.com.chromer.data.history.model.HistoryTable.*
import arun.com.chromer.data.website.model.Website
import com.jakewharton.rxrelay2.PublishRelay
import rx.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Arunkumar on 03-03-2017.
 */
@Singleton
class HistorySqlDiskStore
@Inject
internal constructor(application: Application) : SQLiteOpenHelper(
  application,
  TABLE_NAME,
  null,
  DATABASE_VERSION
), HistoryStore {

  private lateinit var database: SQLiteDatabase

  private val isOpen @Synchronized get() = ::database.isInitialized && database.isOpen

  private val changesRelay = PublishRelay.create<Int>()

  override fun changes(): io.reactivex.Observable<Int> = changesRelay.hide()

  private fun <T> changesTransformer() = Observable.Transformer<T, T> { upstream ->
    upstream.map {
      changesRelay.accept(0)
      it
    }
  }

  private fun <T> Observable<T>.broadcastChanges(): Observable<T> = compose(changesTransformer())

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(DATABASE_CREATE)
    Timber.d("onCreate called")
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

  }

  @Synchronized
  fun open() {
    if (!isOpen) {
      database = writableDatabase
    }
  }

  @Synchronized
  override fun close() {
    if (isOpen) {
      database.close()
    }
  }

  override fun get(website: Website): Observable<Website> {
    return Observable.fromCallable {
      open()
      val cursor =
        database.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_URL=?", arrayOf(website.url))
      when {
        cursor == null -> return@fromCallable null
        cursor.count == 0 -> {
          cursor.close()
          return@fromCallable null
        }
        else -> {
          cursor.moveToFirst()
          val savedSite = Website.fromCursor(cursor)
          cursor.close()
          return@fromCallable savedSite
        }
      }
    }
  }

  override fun insert(website: Website): Observable<Website> {
    return exists(website)
      .flatMap<Website> { exists ->
        if (exists!!) {
          return@flatMap update(website)
        } else {
          val values = ContentValues()
          values.put(COLUMN_URL, website.url)
          values.put(COLUMN_TITLE, website.title)
          values.put(COLUMN_FAVICON, website.faviconUrl)
          values.put(COLUMN_CANONICAL, website.canonicalUrl)
          values.put(COLUMN_COLOR, website.themeColor)
          values.put(COLUMN_AMP, website.ampUrl)
          values.put(COLUMN_BOOKMARKED, website.bookmarked)
          values.put(COLUMN_CREATED_AT, System.currentTimeMillis())
          values.put(COLUMN_VISITED, 1)
          return@flatMap if (database.insert(TABLE_NAME, null, values) != -1L) {
            Observable.just(website)
          } else {
            Observable.just(null)
          }
        }
      }.broadcastChanges()
  }

  override fun update(website: Website): Observable<Website> {
    return get(website).flatMap { saved ->
      if (saved != null) {
        val values = ContentValues()
        values.put(COLUMN_URL, saved.url)
        values.put(COLUMN_TITLE, saved.title)
        values.put(COLUMN_FAVICON, saved.faviconUrl)
        values.put(COLUMN_CANONICAL, saved.canonicalUrl)
        values.put(COLUMN_COLOR, saved.themeColor)
        values.put(COLUMN_AMP, saved.ampUrl)
        values.put(COLUMN_BOOKMARKED, saved.bookmarked)
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis())
        values.put(COLUMN_VISITED, ++saved.count)

        val whereClause = "$COLUMN_URL=?"
        val whereArgs = arrayOf(saved.url)

        if (database.update(TABLE_NAME, values, whereClause, whereArgs) > 0) {
          Timber.d("Updated %s in db", website.url)
          return@flatMap Observable.just(saved)
        } else {
          Timber.e("Update failed for %s", website.url)
          return@flatMap Observable.just(website)
        }
      } else {
        return@flatMap Observable.just(website)
      }
    }.broadcastChanges()
  }

  override fun delete(website: Website): Observable<Website> {
    return Observable.fromCallable {
      open()
      val whereClause = "$COLUMN_URL=?"
      val whereArgs = arrayOf(website.url)
      if (database.delete(TABLE_NAME, whereClause, whereArgs) > 0) {
        Timber.d("Deletion successful for %s", website.url)
      } else {
        Timber.e("Deletion failed for %s", website.url)
      }
      website
    }.broadcastChanges()
  }

  override fun exists(website: Website): Observable<Boolean> {
    return Observable.fromCallable {
      open()
      val selection = " $COLUMN_URL=?"
      val selectionArgs = arrayOf(website.url)
      val cursor = database.query(
        TABLE_NAME,
        ALL_COLUMN_PROJECTION,
        selection,
        selectionArgs, null, null, null
      )
      var exists = false
      if (cursor != null && cursor.count > 0) {
        exists = true
      }
      cursor?.close()
      exists
    }
  }

  override fun deleteAll(): Observable<Int> {
    return Observable.fromCallable {
      open()
      database.delete(TABLE_NAME, "1", null)
    }.broadcastChanges()
  }

  override fun recents(): io.reactivex.Observable<List<Website>> {
    val recentObservable = io.reactivex.Observable.fromCallable<List<Website>> {
      open()
      val websites = ArrayList<Website>()
      database.query(
        TABLE_NAME,
        ALL_COLUMN_PROJECTION,
        null,
        null,
        null,
        null,
        ORDER_BY_TIME_DESC,
        "8"
      )?.use {
        while (it.moveToNext()) {
          websites.add(Website.fromCursor(it))
        }
      }
      websites
    }
    return changes().switchMap { recentObservable }.startWith(recentObservable)
  }

  override fun search(text: String): Observable<List<Website>> {
    return Observable.fromCallable {
      open()
      val websites = ArrayList<Website>()
      database.query(
        true,
        TABLE_NAME,
        ALL_COLUMN_PROJECTION,
        "($COLUMN_URL like '%$text%' OR $COLUMN_TITLE like '%$text%')",
        null,
        null,
        null,
        ORDER_BY_TIME_DESC,
        "5"
      )?.use { cursor ->
        while (cursor.moveToNext()) {
          websites.add(Website.fromCursor(cursor))
        }
      }
      websites
    }
  }

  override fun loadHistoryRange(limit: Int, offset: Int): List<Website> {
    open()
    val cursor = database.rawQuery(
      "SELECT * FROM $TABLE_NAME ORDER BY $ORDER_BY_TIME_DESC LIMIT $limit OFFSET $offset",
      null
    )
    cursor.moveToFirst()
    val websites = ArrayList<Website>()
    while (!cursor.isAfterLast) {
      websites += Website.fromCursor(cursor)
      cursor.moveToNext()
    }
    cursor.close()
    return websites
  }

  override fun pagedHistory(): LiveData<PagedList<Website>> = MutableLiveData()

  companion object {
    private const val DATABASE_VERSION = 1
  }
}
