package com.klavs.bindle.util.startdestination

import android.content.Intent

interface StartDestinationProvider {

    fun determineStartDestination(intent: Intent?): String?

}