/*
 *
 *  Lynket
 *
 *  Copyright (C) 2023 Arunkumar
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

package publish

// TODO Ensure local.properties is present with the following variables for maven publishing
internal const val OSSRH_USERNAME = "OSSRH_USERNAME"
internal const val OSSRH_PASSWORD = "OSSRH_PASSWORD"
internal const val SONATYPE_STAGING_PROFILE_ID = "SONATYPE_STAGING_PROFILE_ID"
internal const val SIGNING_KEY_ID = "SIGNING_KEY_ID"
internal const val SIGNING_KEY = "SIGNING_KEY"
internal const val SIGNING_PASSWORD = "SIGNING_PASSWORD"


internal val PublishVariables = listOf(
  OSSRH_USERNAME,
  OSSRH_PASSWORD,
  SONATYPE_STAGING_PROFILE_ID,
  SIGNING_KEY_ID,
  SIGNING_KEY,
  SIGNING_PASSWORD,
)
