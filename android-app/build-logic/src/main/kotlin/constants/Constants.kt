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

/*
 * Copyright 2021 Arunkumar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public const val ANDROID_COMPILE_SDK: Int = 31
public const val ANDROID_MIN_SDK: Int = 23
public const val ANDROID_TARGET_SDK: Int = 31

public const val ANDROID_PACKAGE_NAME: String = "arun.com.chromer"
public const val ANDROID_RELEASE_VARIANT: String = "release"
public const val ANDROID_DEBUG_VARIANT: String = "debug"
public const val ANDROID_VERSION_CODE: Int = 56
public const val ANDROID_VERSION_NAME: String = "2.1.3"

public typealias ModuleVersion = Map<String, String>

@OptIn(ExperimentalStdlibApi::class)
public val ModuleVersions: ModuleVersion = buildMap {
  // Key - gradle module name
  // Value - publishing version name
}
