import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class Window(val id: Int) {
    val events: Flow<Event> = flow { }

    fun configureWindow() {
    }

    fun sendEvent(event: Request) {
    }
}