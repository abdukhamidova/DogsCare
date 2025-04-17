package com.example.dogscare

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.dogscare.databinding.DialogFilterArchiveBinding

class FilterArchiveDialog (
    private val context: Context,
    private val selectedButton: ArchiveType,
    private val onArchiveSelected: (ArchiveType) -> Unit
){
    private lateinit var binding: DialogFilterArchiveBinding
    fun show(){
        binding = DialogFilterArchiveBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()

        when(selectedButton){
            ArchiveType.ADOPTION -> binding.radioButtonAdoption.isChecked = true
            ArchiveType.DEATH -> binding.radioButtonDeath.isChecked = true
            ArchiveType.TRANSFER -> binding.radioButtonTransfer.isChecked = true
            ArchiveType.OTHER -> binding.radioButtonOther.isChecked = true
            else -> binding.radioButtonAll.isChecked = true
        }


        binding.buttonConfirm.setOnClickListener{
            val selectedType = when{
                binding.radioButtonAll.isChecked -> ArchiveType.ARCHIVED
                binding.radioButtonAdoption.isChecked -> ArchiveType.ADOPTION
                binding.radioButtonDeath.isChecked -> ArchiveType.DEATH
                binding.radioButtonTransfer.isChecked -> ArchiveType.TRANSFER
                binding.radioButtonOther.isChecked -> ArchiveType.OTHER
                else -> null
            }

            if(selectedType != null){
                onArchiveSelected(selectedType)
                dialog.dismiss()
            }else {
                Toast.makeText(context, "Wybierz jedną z opcji", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}