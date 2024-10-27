package com.igo.fluxcard.ui.card

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.igo.fluxcard.databinding.FragmentCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardFragment : Fragment() {

    // Используем by viewModels для упрощенного создания ViewModel
    // Делегат by viewModels() автоматически привязывает ViewModel к Fragment
    // и следит за жизненным циклом фрагмента
    private val viewModel: CardViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = CardFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инфлейтим макет для этого фрагмента
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // По умолчанию скрываем перевод
        binding.textTranslate.visibility = View.INVISIBLE

        // Наблюдаем за изменениями данных в ViewModel
        viewModel.note.observe(viewLifecycleOwner, Observer { note ->
            binding.textOrigin.text = note.origin
            binding.textTranslate.text = note.translate
        })

        // Показываем перевод при нажатии на кнопку "Answer"
        binding.btnAnswer.setOnClickListener {
            binding.textTranslate.visibility = View.VISIBLE
        }

        // Обрабатываем нажатие кнопок "Remember" и "Not Remember"
        binding.btnRemember.setOnClickListener { rememberBtnClick(true) }
        binding.btnDontRemember.setOnClickListener { rememberBtnClick(false) }

        // Обрабатываем нажатие на FAB для инспекции базы данных
        binding.fab.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val notes = viewModel.noteList.value ?: emptyList()
                withContext(Dispatchers.Main) {
                    val message = if (notes.isNotEmpty()) {
                        notes.joinToString(separator = "\n\n") {
                            "ID: ${it.id}, Origin: ${it.origin}, Translate: ${it.translate}, Correct Streak: ${it.correctStreak}, Last Shown: ${it.lastShownTimestamp}, ShowFirst: ${it.showFirst}, ShowSecond: ${it.showSecond}, ShowThird: ${it.showThird}, ShowFourth: ${it.showFourth}, ShowFifth: ${it.showFifth}, ShowFirstTimestamp: ${it.showFirstTimestamp}, ShowSecondTimestamp: ${it.showSecondTimestamp}, ShowThirdTimestamp: ${it.showThirdTimestamp}, ShowFourthTimestamp: ${it.showFourthTimestamp}, ShowFifthTimestamp: ${it.showFifthTimestamp}"
                        }
                    } else {
                        "База данных пуста"
                    }
                    val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG * 55) // Увеличиваем время отображения тоста в 5 раз
                    val textView = TextView(requireContext()).apply {
                        text = message
                        textSize = 8f // Уменьшаем размер шрифта в два раза
                        gravity = Gravity.CENTER
                        setPadding(16, 16, 16, 16)
                    }
                    toast.view = textView
                    toast.setGravity(Gravity.FILL, 0, 0)
                    toast.show()
                }
            }
        }
    }

    private fun rememberBtnClick(isRemembered: Boolean) {
        viewModel.rememberBtnClick(isRemembered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
