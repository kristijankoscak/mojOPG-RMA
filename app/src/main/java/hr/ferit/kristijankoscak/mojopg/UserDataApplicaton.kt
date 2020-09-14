package hr.ferit.kristijankoscak.mojopg

import android.app.Application
import android.content.Context

class UserDataApplicaton : Application() {
    companion object {
        lateinit var ApplicationContext: Context
            private set
    }
    override fun onCreate() {
        super.onCreate()
        ApplicationContext = this
    }
}