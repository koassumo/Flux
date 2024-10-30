package com.igo.fluxcard.ui.card
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil.Coil
import coil.load
import com.igo.fluxcard.R
import com.igo.fluxcard.databinding.FragmentCardBinding
import com.igo.fluxcard.BuildConfig

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

    val apiKey = BuildConfig.SPLASH_API_KEY

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


        // Наблюдаем за изменениями данных в ViewModel
        viewModel.note.observe(viewLifecycleOwner, Observer { note ->
            binding.textStreak.text = note.correctStreak.toString()
            updateProgressSquares(note.correctStreak)
            binding.textOrigin.text = note.origin
            binding.textTranslate.visibility = View.INVISIBLE
            binding.textTranslate.text = note.translate
            binding.imageView.setImageDrawable(null)
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

            binding.btnAnswer.setOnClickListener {
                binding.textTranslate.visibility = View.VISIBLE
                binding.btnAnswer.isEnabled = false
                viewModel.searchImage(note.origin)
                binding.imageView.visibility = View.VISIBLE
            }

            binding.btnDontRemember.setOnClickListener {
                isRemembered = false
                updateRememberedBtnClick()
            }

        })

        viewModel.imageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (imageUrl != null) {
                // Используем Coil для загрузки изображения напрямую на imageView
                binding.imageView.load(imageUrl)
            } else {
                Log.d("CardFragment", "Не удалось загрузить изображение")
            }
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


