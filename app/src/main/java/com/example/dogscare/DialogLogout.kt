package com.example.dogscare

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import com.example.dogscare.databinding.DialogLogoutBinding
import com.google.firebase.auth.FirebaseAuth

class LogoutDialog (
    private val activity: Activity,
) {
    private lateinit var binding: DialogLogoutBinding

    fun show() {
        binding = DialogLogoutBinding.inflate(LayoutInflater.from(activity))
        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        binding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.buttonLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(activity, ActivitySignIn::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()

            dialog.dismiss()
        }

        dialog.show()
    }
}