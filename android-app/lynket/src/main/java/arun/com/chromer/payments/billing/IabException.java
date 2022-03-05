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

package arun.com.chromer.payments.billing;

/**
 * Exception thrown when something went wrong with in-app billing.
 * An IabException has an associated IabResult (an error).
 * To get the IAB result that caused this exception to be thrown,
 * call {@link #getResult()}.
 */
class IabException extends Exception {
  private final IabResult mResult;

  private IabException(IabResult r) {
    this(r, null);
  }

  public IabException(int response, String message) {
    this(new IabResult(response, message));
  }

  private IabException(IabResult r, Exception cause) {
    super(r.getMessage(), cause);
    mResult = r;
  }

  public IabException(int response, String message, Exception cause) {
    this(new IabResult(response, message), cause);
  }

  /**
   * Returns the IAB result (error) that this exception signals.
   */
  public IabResult getResult() {
    return mResult;
  }
}
