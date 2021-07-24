package com.rsschool.task_pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.rsschool.task_pomodoro.databinding.StopwatchItemBinding

// передаем во ViewHolder сгенерированный класс байдинга для разметки элемента RecyclerView.
// Вместо кучи полей для каждого элемента, теперь мы храним один объект со всеми ссылками на элементы-view
// В родительский ViewHolder передаем bindig.root т.е. ссылку на View данного элемента RecyclerView
class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,    // передадим имплементацию интерфейса в качестве параметра в адаптер RecyclerView
    private val resources: Resources
): RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    // Такой подход, когда ViewHolder обрабатывает только визуальное представление айтема, который пришел ему в методе bind, и ничего не меняет напрямую,
    // а все колбэки обрабатываются снаружи (в нашем случае через listener) - является предпочтительным.
    // в метод bind передаем экземпляр Stopwatch, он приходит к нам из метода onBindViewHolder адаптера и содержит актуальные параметры для данного элемента списка.
    fun bind(stopwatch: Stopwatch) {
        // Останавливаем таймер переиспользованного холдера
        timer?.cancel()
        Log.i("bind", "Начало бинда")
        // Значение времени системного таймера
        val currentTime = SystemClock.uptimeMillis()

        Log.i("bind", "SystemClock.uptimeMillis() = ${SystemClock.uptimeMillis().displayTime()}")
        Log.i("bind", "stopwatch.globalMs = ${stopwatch.globalMs.displayTime()}")
        Log.i("bind", "stopwatch.currentMs = ${stopwatch.currentMs.displayTime()}")

        // Обновляем элементы нашей item
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.restartButton.text = "Restart"
        val circlePercent = 60000 - (stopwatch.currentMs / (stopwatch.taskMs / 100)) * 600  // Помянем мою математику за 5 класс
        binding.customViewOne.setCurrent(circlePercent)

        // Определяем коррекцию пройденного времени и устанавливаем действие холдера:
        if (stopwatch.isStarted) {

            val diffMs = currentTime - stopwatch.globalMs   // Временной промежуток работы таймера за видимостью экрана
            Log.i("Расчет времени", "Если => таймер включен, diffMs = ${diffMs.displayTime()}")

            if (stopwatch.taskMs > diffMs) {
                Log.i("Расчет времени", "Коррекция меньше, stopwatch.taskMs - diffMs = ${(stopwatch.taskMs - diffMs).displayTime()}")

                // Если время после корректировки осталось, то запускаем таймер
                if (diffMs > 900L) stopwatch.currentMs = stopwatch.taskMs - diffMs      // Скоректированное время

                Log.i("Расчет времени", "Коррекция меньше, new stopwatch.currentMs = ${stopwatch.currentMs.displayTime()}")
                Log.i("Расчет времени", "Коррекция меньше, stopwatch.taskMs - diffMs = ${(stopwatch.taskMs - diffMs).displayTime()}")
                Log.i("Расчет времени", "Коррекция меньше, startTimer(stopwatch)")

                startTimer(stopwatch)   // и рисуем иконки стоп и включаем кастом вью
            } else {
                stopwatch.currentMs = 0L
                stopTimer(stopwatch)

                Log.i("Расчет времени", "Коррекция больше, stopTimer = ${(stopwatch.taskMs - diffMs).displayTime()}")
                Log.i("Расчет времени", "Коррекция больше, stopTimer(stopwatch) currentMs = 0L")
            }
        } else {
            stopTimer(stopwatch)    // иначе, рисуем икноки старт и выключаем кастом вью

            Log.i("Расчет времени", "Таймер выкючен, то stopTimer(). осталось = ${stopwatch.currentMs.displayTime()}")
        }

        initButtonsListeners(stopwatch) // Настраиваем действия на кнопку-оборотень
    }

    // Настраиваем действия на кнопку-оборотень
    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            // Если таймер работает,
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs) // То на нажатие останавливаем и сохраняем время
                Log.i("isStarted?", "кнопка-оборотень listener.stop")

            } else {
                listener.start(stopwatch.id)    // Иначе, на нажатие запускаем таймер
                Log.i("isStarted?", "кнопка-оборотень listener.start")
            }
        }

        binding.restartButton.setOnClickListener { listener.reset(stopwatch.id) }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    // Запускаем таймер, рисуем иконку стоп и включаем кастом вью
    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = "Stop"
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        Log.i("fun startTimer", "timer?.start()")
        timer = getCountDownTimer(stopwatch)
        timer?.start()
    }

    // Выключаем кастом вью, рисуем иконки и кнопки
    private fun stopTimer(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        val circlePercent = 60000 - (stopwatch.currentMs / (stopwatch.taskMs / 100)) * 600
        binding.customViewOne.setCurrent(circlePercent)
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()

        // Продление таймера
        if (stopwatch.currentMs == stopwatch.taskMs) binding.startPauseButton.text = "Start"
        else binding.startPauseButton.text = "Resume"

        // Завершение таймера
        if (stopwatch.currentMs == 0L) {
            binding.startPauseButton.visibility = View.GONE
            binding.item.setBackgroundColor(resources.getColor(R.color.finish, null))
        }
        else {
            binding.startPauseButton.visibility = View.VISIBLE
            binding.item.setBackgroundColor(resources.getColor(R.color.white, null))
        }
    }

    // Объект пользовательского таймера для отсчета времени и изменения полей по секундам
    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs = millisUntilFinished
                Log.i("onTick", "осталось = ${stopwatch.currentMs.displayTime()}")

                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                val circlePercent = 60000 - (stopwatch.currentMs / (stopwatch.taskMs / 100)) * 600
                binding.customViewOne.setCurrent(circlePercent)
            }

            override fun onFinish() {
                Log.i("onFinish", "осталось = ${stopwatch.currentMs.displayTime()}")
                stopwatch.currentMs = 0L
                listener.stop(stopwatch.id, stopwatch.currentMs)
                stopTimer(stopwatch)
            }
        }
    }

    // данный метод расширения для Long конвертирует текущее значение таймера в миллисекундах в формат “HH:MM:SS:MsMs” и возвращает соответствующую строку
    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        // val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}" // :${displaySlot(ms)}
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val START_TIME = "00:00:00"   // :00
        private const val UNIT_TEN_MS = 1000L
    }

}