package com.igo.fluxcard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.igo.fluxcard.ui.card.CardFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Проверяем, не был ли уже добавлен фрагмент
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CardFragment.newInstance())
                .commit()
        }
    }
}
