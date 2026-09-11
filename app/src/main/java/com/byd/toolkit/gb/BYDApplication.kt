package com.byd.toolkit.gb

import android.app.Application
import com.byd.toolkit.gb.data.repository.PreferencesRepository
import com.byd.toolkit.gb.data.repository.SnapshotRepository

class BYDApplication : Application() {

    lateinit var preferencesRepository: PreferencesRepository
        private set

    lateinit var snapshotRepository: SnapshotRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferencesRepository = PreferencesRepository(this)
        snapshotRepository = SnapshotRepository(this, preferencesRepository)

        // Capture initial stock baseline if first run
        snapshotRepository.ensureBaselineSnapshot()
    }

    companion object {
        lateinit var instance: BYDApplication
            private set
    }
}
