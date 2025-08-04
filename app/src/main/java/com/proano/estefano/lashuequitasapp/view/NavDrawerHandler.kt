package com.proano.estefano.lashuequitasapp.view

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.button.MaterialButton
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager
import java.io.File

class NavDrawerHandler(
    private val activity: Activity,
    private val headerView: View,
    private val sessionManager: SessionManager
) {

    init {
        setupUserInfo()
        setupLogoutButton()
    }

    private fun setupUserInfo() {
        // Nombre y correo
        headerView.findViewById<TextView>(R.id.userName)?.text = sessionManager.getUserFullName()
        headerView.findViewById<TextView>(R.id.userEmail)?.text = sessionManager.getUserEmail()

        // Imagen de perfil
        val imageView = headerView.findViewById<ImageView>(R.id.userImage)
        val photoPath = sessionManager.getUserPhotoPath()
        if (!photoPath.isNullOrEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                Glide.with(activity)
                    .load(file)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.perfilexam) // Imagen por defecto
                    .into(imageView)
            } else {
                // Si el archivo no existe, mostrar imagen por defecto
                imageView.setImageResource(R.drawable.perfilexam)
            }
        } else {
            // Si no hay ruta, mostrar imagen por defecto
            imageView.setImageResource(R.drawable.perfilexam)
        }
    }

    private fun setupLogoutButton() {
        headerView.findViewById<MaterialButton>(R.id.btnCerrarSesion)?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ -> logout() }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(activity.resources.getColor(R.color.orange_buttons_filledStars))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(activity.resources.getColor(R.color.orange_buttons_filledStars))
        }

        dialog.show()
    }

    private fun logout() {
        sessionManager.logout()
        val intent = Intent(activity, PantallaInicioDeSesionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}