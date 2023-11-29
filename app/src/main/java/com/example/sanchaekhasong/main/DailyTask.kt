package com.example.sanchaekhasong.main

import java.io.Serializable

class DailyTask (
    var mission: String? = null,
    var point: Int? = null,
    var completed: Boolean? = null
) : Serializable