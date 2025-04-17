package com.example.dogscare

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.dogscare.databinding.DialogArchiveDogBinding

class ArchiveDialog(
    private val context: Context,
    private val onArchiveSelected: (ArchiveType) -> Unit
) {
    private lateinit var binding: DialogArchiveDogBinding
    fun show() {
        binding = DialogArchiveDogBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()

        binding.buttonConfirm.setOnClickListener {
            val selectedType = when {
                binding.radioButtonAdoption.isChecked -> ArchiveType.ADOPTION
                binding.radioButtonDeath.isChecked -> ArchiveType.DEATH
                binding.radioButtonTransfer.isChecked -> ArchiveType.TRANSFER
                binding.radioButtonOther.isChecked -> ArchiveType.OTHER
                else -> null
            }

            if (selectedType != null) {
                onArchiveSelected(selectedType)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Wybierz jedną z opcji", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
