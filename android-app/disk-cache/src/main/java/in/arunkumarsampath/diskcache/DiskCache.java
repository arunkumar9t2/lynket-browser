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

package in.arunkumarsampath.diskcache;

import java.util.List;

interface DiskCache<T> {

  /**
   * Sets the value to {@code value}.
   */
  T set(String key, T value);

  /**
   * Returns a value by {@code key}, or null if it doesn't
   * exist is not currently readable. If a value is returned, it is moved to
   * the head of the LRU queue.
   */
  T get(String key);

  /**
   * Drops the entry for {@code key} if it exists and can be removed. Entries
   * actively being edited cannot be removed.
   *
   * @return true if an entry was removed.
   */
  boolean remove(String key);

  /**
   * Returns all values from cache directory if all files are same type
   *
   * @return
   */
  List<T> getAll();

  /**
   * Deletes all file from cache directory
   */
  void clear();

  /**
   * Returns true if there is file by {@code key} in cache folder
   *
   * @param key
   * @return
   */
  boolean exists(String key);

  /**
   * Closes this cache. Stored values will remain on the filesystem.
   */
  void close();

}
