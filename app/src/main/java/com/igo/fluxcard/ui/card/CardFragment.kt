package com.igo.fluxcard.ui.card

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.igo.fluxcard.R
import com.igo.fluxcard.databinding.FragmentCardBinding

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

    var isRemembered = false

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
        binding.webView.visibility = View.INVISIBLE // Скрываем WebView вначале
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Прокрутка вниз, чтобы игнорировать шапку
                binding.webView.scrollTo(0, 900) // Значение 500 можно настроить в зависимости от страницы
                // Добавляем плавное появление после скроллинга
                Handler(Looper.getMainLooper()).postDelayed({
                    // Устанавливаем видимость и начинаем анимацию плавного появления
                    binding.webView.alpha = 0f
                    binding.webView.animate()
                        .alpha(0.3f)
                        .setDuration(1000) // Длительность анимации в миллисекундах
                        .start()
                    binding.webView.visibility = View.VISIBLE
                }, 1000) // Задержка в 500 мс, чтобы завершить прокрутку
            }
        }

        // Наблюдаем за изменениями данных в ViewModel
        viewModel.note.observe(viewLifecycleOwner, Observer { note ->
            binding.webView.visibility = View.INVISIBLE // Скрываем WebView вначале
            // По умолчанию скрываем перевод
            binding.textTranslate.visibility = View.INVISIBLE
            binding.textOrigin.text = note.origin
            binding.textTranslate.text = note.translate
            updateProgressSquares(note.correctStreak)
            binding.btnAnswer.isEnabled = true
            binding.btnRemember.isEnabled = true
            binding.btnRemember.alpha = 1f
            binding.btnDontRemember.isEnabled = true
            binding.btnDontRemember.alpha = 1f
            //binding.btnNext.isEnabled = false

            isRemembered = false
            binding.btnRemember.setOnClickListener {
                isRemembered = true
                updateRememberedBtnClick()
            }
            binding.btnDontRemember.setOnClickListener {
                isRemembered = false
                updateRememberedBtnClick()
            }

        })

        binding.btnAnswer.setOnClickListener {
            binding.textTranslate.visibility = View.VISIBLE
//            binding.webView.loadUrl("https://www.google.com/search?tbm=isch&q=" + binding.textOrigin.text)
            binding.webView.loadUrl("https://yandex.com/images/search?text=" + binding.textOrigin.text)
            binding.btnAnswer.isEnabled = false
        }

        binding.btnNext.setOnClickListener { nextBtnClick() }

    }

    private fun updateRememberedBtnClick() {
        binding.textTranslate.visibility = View.VISIBLE
        binding.btnAnswer.isEnabled = false
        binding.btnRemember.setOnClickListener(null)
        binding.btnDontRemember.setOnClickListener(null)

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

    private fun nextBtnClick() {
        viewModel.nextBtnClick(isRemembered)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


