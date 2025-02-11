package com.klavs.bindle.helper.startdestination

import android.content.Intent

interface StartDestinationProvider {

    fun determineStartDestination(intent: Intent?): Any?

}