package com.rsschool.task_pomodoro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.rsschool.task_pomodoro.databinding.StopwatchItemBinding

class StopwatchAdapter(
    private val listener: StopwatchListener
): ListAdapter<Stopwatch, StopwatchViewHolder>(itemComparator) {

    // Раздуваем View, создаем биндинг-объект и передаем его в конструктор обработчика графического элемента, возвращаем объект ОГЭ
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopwatchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = StopwatchItemBinding.inflate(layoutInflater, parent, false)
        // Вместо View помещаем объект класса binding, он через root отдаст саму view и даст доступ к ссылкам
        // Так же передаем "ссылку на методы" MainActivity
        // Передаем ресурсы анимации
        return StopwatchViewHolder(binding, listener, binding.root.context.resources)
    }

    // для конкретного ViewHolder обновляем параметры.
    // onBindViewHolder вызывается в момент создания айтема, в моменты пересоздания (например, айтем вышел за пределы экрана, затем вернулся) и в моменты обновления айтемов (этим у нас занимается DiffUtil)
    override fun onBindViewHolder(holder: StopwatchViewHolder, position: Int) {
        holder.bind(getItem(position)) // protected T getItem (int position)
    }

    // Имплементация DiffUtil помогает понять RecyclerView
    // какой айтем изменился (был удален, добавлен) и контент какого айтема изменился
    // - чтобы правильно проиграть анимацию и показать результат пользователю.
    // В areContentsTheSame лучше проверять на равество только те параметры модели, которые влияют на её визуальное представление на экране.
    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<Stopwatch>() {

            override fun areItemsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.currentMs == newItem.currentMs &&
                        oldItem.isStarted == newItem.isStarted
            }

            //  В данном случае это уловка, чтобы айтем не бликовал (проигрывается анимация для всего айтема), когда мы нажимаем на кнопки.
            override fun getChangePayload(oldItem: Stopwatch, newItem: Stopwatch) = Any()
        }
    }
}