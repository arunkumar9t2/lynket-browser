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

package arun.com.chromer.halo

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * UDF based ViewModel via Molecule
 */
abstract class HaloViewModel<Action, State> : ViewModel() {
  private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

  private val actions = MutableSharedFlow<Action>()

  val state: StateFlow<State> by lazy(LazyThreadSafetyMode.NONE) {
    scope.launchMolecule(mode = RecompositionMode.ContextClock) {
      process(actions.asSharedFlow())
    }
  }

  @Composable
  protected abstract fun process(actions: Flow<Action>): State
}
