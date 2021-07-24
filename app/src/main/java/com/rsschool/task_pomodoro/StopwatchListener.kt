package com.rsschool.task_pomodoro

// Создаём соответствующий интерфейс, имплементируем который в MainActivity (поскольку именно в этом классе у нас логика управления списком таймеров),
// и передадим эту имплементацию в качестве параметра в адаптер RecyclerView:
interface StopwatchListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long)

    fun reset(id: Int)

    fun delete(id: Int)
}