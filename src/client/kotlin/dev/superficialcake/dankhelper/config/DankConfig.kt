package dev.superficialcake.dankhelper.config

import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config

@Config(name = "dankhelper")
class DankConfig : ConfigData {

    //Positioning
    var hudX: Int = 10
    var hudY: Int = 60

    //Toggles
    var showSessionTime: Boolean = true
    var showMPM: Boolean = true
    var showASMPM: Boolean = false
    var showTPM: Boolean = true
    var showCPM: Boolean = true
    var showKPM: Boolean = true
    var showSPM: Boolean = true
    var showBPM: Boolean = true
    var showBM: Boolean = true
    var showFortune: Boolean = true

    //Graph Toggles
    var showMoneyGraph: Boolean = true
    var showSpentGraph: Boolean = false
    var showTokenGraph: Boolean = true
    var showSwingsGraph: Boolean = true
}