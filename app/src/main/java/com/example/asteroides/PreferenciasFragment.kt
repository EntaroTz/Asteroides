package com.example.asteroides

import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.EditTextPreference

class PreferenciasFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferencias, rootKey)

        val fragmentos = findPreference<EditTextPreference>("maximo")
        val valor = fragmentos?.text ?: "12" // Valor predeterminado si no hay ningún valor guardado

        fragmentos?.summary = "Limita en número de valores que se muestran ($valor)"

        fragmentos?.setOnPreferenceChangeListener { preference, newValue ->
            var nuevoValor: Int
            try {
                nuevoValor = (newValue as String).toInt()
            } catch (e: Exception) {
                Toast.makeText(requireActivity(), "Ha de ser un número", Toast.LENGTH_SHORT).show()
                return@setOnPreferenceChangeListener false
            }
            if (nuevoValor in 0..99) {
                fragmentos?.summary = "Limita en número de valores que se muestran ($nuevoValor)"
                true
            } else {
                Toast.makeText(requireActivity(), "Valor Máximo 99", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
}
