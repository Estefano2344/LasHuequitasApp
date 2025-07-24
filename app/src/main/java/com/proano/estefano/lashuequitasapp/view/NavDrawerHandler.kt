package com.proano.estefano.lashuequitasapp.view

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager

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
        // Actualizar información del usuario en el header del drawer
        headerView.findViewById<TextView>(R.id.userName)?.text = sessionManager.getUserFullName()
        headerView.findViewById<TextView>(R.id.userEmail)?.text = sessionManager.getUserEmail()
    }

    private fun setupLogoutButton() {
        headerView.findViewById<MaterialButton>(R.id.btnCerrarSesion)?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        sessionManager.logout()
        val intent = Intent(activity, PantallaInicioDeSesionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}