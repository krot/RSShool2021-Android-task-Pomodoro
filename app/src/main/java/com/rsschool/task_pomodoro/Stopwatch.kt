package com.rsschool.task_pomodoro

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    val taskMs: Long,
    var isStarted: Boolean,
    var globalMs: Long
    )
