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
 * Represents the result of an in-app billing operation.
 * A result is composed of a response code (an integer) and possibly a
 * message (String). You can get those by calling
 * {@link #getResponse} and {@link #getMessage()}, respectively. You
 * can also inquire whether a result is a success or a failure by
 * calling {@link #isSuccess()} and {@link #isFailure()}.
 */
@SuppressWarnings("ALL")
public class IabResult {
  private int mResponse;
  private String mMessage;

  public IabResult(int response, String message) {
    mResponse = response;
    if (message == null || message.trim().length() == 0) {
      mMessage = IabHelper.getResponseDesc(response);
    } else {
      mMessage = message + " (response: " + IabHelper.getResponseDesc(response) + ")";
    }
  }

  public int getResponse() {
    return mResponse;
  }

  public String getMessage() {
    return mMessage;
  }

  public boolean isSuccess() {
    return mResponse == IabHelper.BILLING_RESPONSE_RESULT_OK;
  }

  public boolean isFailure() {
    return !isSuccess();
  }

  public String toString() {
    return "IabResult: " + getMessage();
  }
}

