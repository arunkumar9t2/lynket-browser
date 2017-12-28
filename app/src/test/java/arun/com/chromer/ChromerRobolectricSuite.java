/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
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

package arun.com.chromer;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import arun.com.chromer.di.app.TestAppComponent;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = {16, 21},
        constants = BuildConfig.class,
        application = ChromerTestApplication.class)
public abstract class ChromerRobolectricSuite {
    protected TestAppComponent testAppComponent;

    @Before
    public final void setup() {
        MockitoAnnotations.initMocks(this);
        testAppComponent = (TestAppComponent) ((ChromerTestApplication) RuntimeEnvironment.application).getAppComponent();
    }

}

