package arun.com.chromer.browsing.backgroundloading

interface BackgroundLoadingStrategy {

    fun perform(url: String)
}