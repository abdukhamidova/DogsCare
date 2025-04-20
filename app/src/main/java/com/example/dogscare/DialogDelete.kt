package com.example.dogscare

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.dogscare.databinding.DialogDeleteBinding
import com.google.firebase.firestore.DocumentReference

class DeleteDialog(
    private val context: Context,
    private val documentRef: DocumentReference
) {
    private lateinit var binding: DialogDeleteBinding
    fun show(){
        binding = DialogDeleteBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()

        binding.buttonConfirm.setOnClickListener{
            documentRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Pozycja została usunięta", Toast.LENGTH_SHORT).show()
                    if (context is Activity) {
                        context.finish()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Błąd podczas usuwania", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }
        dialog.show()
    }
}