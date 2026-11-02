package rocks.wasabee.android

import android.app.Application
import rocks.wasabee.android.data.SessionStore
import rocks.wasabee.android.data.WasabeeRepository

class WasabeeApp : Application() {
    val repository by lazy { WasabeeRepository(SessionStore(this)) }
}
