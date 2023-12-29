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

package arun.com.chromer.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arun.com.chromer.di.HasInjector
import arun.com.chromer.theme.LynketTheme
import com.deliveryhero.whetstone.activity.ContributesActivityInjector
import com.deliveryhero.whetstone.compose.injectedViewModel

@ContributesActivityInjector
class HomeActivity : ComponentActivity(), HasInjector {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      LynketTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          ListItems()
        }
      }
    }
  }
}

@Composable
fun ListItems(
  viewModel: HomeViewModel = injectedViewModel()
) {
  val state by viewModel.state.collectAsState()
  when (state) {
    is HomeState.Loading -> {
      Text(text = "Loading")
    }

    is HomeState.Items -> {
      val items = (state as HomeState.Items).items
      LazyColumn(contentPadding = PaddingValues(all = 16.dp)) {
        items(items, key = { it }) { item ->
          Text(text = item)
        }
      }
    }
  }
}
