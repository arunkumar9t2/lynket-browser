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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arun.com.chromer.halo.HaloViewModel
import com.deliveryhero.whetstone.viewmodel.ContributesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class HomeAction {
  data object Load : HomeAction()
}

sealed class HomeState {
  data object Loading : HomeState()
  data class Items(val items: List<String>) : HomeState()
}

@ContributesViewModel
class HomeViewModel
@Inject
constructor() : HaloViewModel<HomeAction, HomeState>() {

  private val dummyItems by lazy {
    flow {
      delay(2000)
      repeat(100) {
        emit(it)
      }
    }
  }

  @Composable
  override fun process(actions: Flow<HomeAction>): HomeState {
    var items by remember { mutableStateOf(emptyList<String>()) }
    LaunchedEffect(Unit) {
      withContext(Dispatchers.IO) {
        dummyItems.collect {
          items = items + it.toString()
        }
      }
    }
    return if (items.isEmpty()) {
      HomeState.Loading
    } else {
      HomeState.Items(items)
    }
  }
}
