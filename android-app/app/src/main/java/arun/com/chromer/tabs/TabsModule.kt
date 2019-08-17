package arun.com.chromer.tabs

import dagger.Binds
import dagger.Module

@Module
abstract class TabsModule {

    @Binds
    abstract fun tabsManager(defaultTabsManager: DefaultTabsManager): TabsManager
}