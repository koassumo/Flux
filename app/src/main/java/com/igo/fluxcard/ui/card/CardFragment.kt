package com.igo.fluxcard.ui.card

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.igo.fluxcard.R
import com.igo.fluxcard.databinding.FragmentCardBinding
import com.igo.fluxcard.BuildConfig
import java.util.Locale

class CardFragment : Fragment(), TextToSpeech.OnInitListener {


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

    private var isRemembered = false

    private var textToSpeech: TextToSpeech? = null


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

        // Инициализация TextToSpeech
        textToSpeech = TextToSpeech(requireContext(), this)

        // Наблюдаем за изменениями данных в ViewModel
        viewModel.note.observe(viewLifecycleOwner, Observer { note ->
            binding.textStreak.text = note.correctStreak.toString()
            updateProgressSquares(note.correctStreak)
            binding.textOrigin.text = note.origin
            binding.textTranslate.visibility = View.INVISIBLE
            binding.textTranslate.text = note.translate
            binding.imageView.setImageDrawable(null)
            binding.imageView.visibility = View.INVISIBLE
            binding.btnAnswer.isEnabled = true
            binding.btnRemember.isEnabled = true
            binding.btnRemember.alpha = 1f
            binding.btnDontRemember.isEnabled = true
            binding.btnDontRemember.alpha = 1f
            //binding.btnNext.isEnabled = false
            isRemembered = false

            speakOut(note.origin)

        })


        binding.btnAnswer.setOnClickListener {
            binding.textTranslate.visibility = View.VISIBLE
            binding.btnAnswer.isEnabled = false
            viewModel.searchImageUrl()
            binding.imageView.visibility = View.VISIBLE
        }
        binding.btnRemember.setOnClickListener {
            isRemembered = true
            updateRememberedBtnClick()
            viewModel.writeLocalbase(isRemembered)
        }
        binding.btnDontRemember.setOnClickListener {
            isRemembered = false
            updateRememberedBtnClick()
            viewModel.writeLocalbase(isRemembered)
        }

        binding.btnNext.setOnClickListener {
            viewModel.moveToNextNote()
        }

        viewModel.imageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (imageUrl != null) {
                binding.imageView.load(imageUrl)
            } else {
                Log.d("CardFragment", "Не удалось загрузить изображение")
            }
        }
    }

    private fun updateRememberedBtnClick() {
        binding.textTranslate.visibility = View.VISIBLE
        binding.btnAnswer.isEnabled = false
        if (isRemembered) { // Понижаем прозрачность, чтобы показать, что кнопка была нажата
            binding.btnDontRemember.isEnabled = false
            binding.btnRemember.alpha = 0.6f
        } else {
            binding.btnRemember.isEnabled = false
            binding.btnDontRemember.alpha = 0.6f
        }
        binding.btnNext.isEnabled = true
    }

    private fun updateProgressSquares(correctStreak: Int) {
        val progressViews = listOf(
            binding.progress1,
            binding.progress2,
            binding.progress3,
            binding.progress4,
            binding.progress5
        )
        progressViews.forEachIndexed { index, view ->
            if (index < correctStreak) {
                view.setBackgroundResource(R.drawable.progress_square_filled)
            } else {
                view.setBackgroundResource(R.drawable.progress_square_empty)
            }
        }
    }

    private fun speakOut(text: String) {
        textToSpeech?.let {
            it.setPitch(0.6f) // Настройка высоты голоса (1.0 - нормальная высота, можно уменьшить или увеличить)
            it.setSpeechRate(0.9f) // Настройка скорости речи (1.0 - нормальная скорость, можно уменьшить или увеличить)
            // Установка мужского голоса, если он доступен
            val availableVoices = it.voices
            Log.d("CardFragment", "Доступные голоса: ${availableVoices.joinToString { voice -> voice.name }}")
            val maleVoice = availableVoices.find { voice ->
                voice.name.contains("male", ignoreCase = true)
            }
            if (maleVoice != null) {
                it.voice = maleVoice
            } else {
                Log.d("CardFragment", "Мужской голос не найден, используется голос по умолчанию")
            }
            it.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UniqueID")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("CardFragment", "Язык не поддерживается")
            }

            // Установка слушателя для контроля за прогрессом произношения
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("CardFragment", "Начало произношения")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d("CardFragment", "Произношение завершено")
                }

                override fun onError(utteranceId: String?) {
                    Log.e("CardFragment", "Ошибка при произношении")
                }
            })

        } else {
            Log.e("CardFragment", "Инициализация TTS не удалась")
        }
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}


