// app/src/main/java/com/gpstracker/app/MainActivity.kt
package com.gpstracker.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gpstracker.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            // Si tenemos permisos de ubicación, pedimos el de segundo plano si es necesario
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundPermission()
            } else {
                startTrackingService()
            }
        } else {
            showPermissionRequiredDialog()
        }
    }

    private val backgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // No importa si lo concede o no, iniciamos el servicio.
        // Android limitará el rastreo si no tiene este permiso, pero la app funcionará.
        startTrackingService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissionsAndStart()
    }

    private fun checkPermissionsAndStart() {
        val hasFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation && hasCoarseLocation) {
            startTrackingService()
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun requestBackgroundPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this)
                .setTitle("Permiso Adicional Requerido")
                .setMessage("Para que el rastreo funcione con la pantalla apagada, por favor elija 'Permitir todo el tiempo' en la siguiente pantalla.")
                .setPositiveButton("Entendido") { _, _ ->
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                .setNegativeButton("Ahora no") { _, _ ->
                    startTrackingService() // Iniciar de todas formas
                }
                .show()
        } else {
            startTrackingService()
        }
    }

    private fun startTrackingService() {
        binding.tvStatus.text = "Rastreo Activo.\nPuedes cerrar esta pantalla."
        val intent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun showPermissionRequiredDialog() {
        binding.tvStatus.text = "Error: Se necesitan permisos de ubicación para funcionar."
        AlertDialog.Builder(this)
            .setTitle("Permisos Requeridos")
            .setMessage("Esta app no puede funcionar sin permisos de ubicación. Por favor, actívalos en la configuración.")
            .setPositiveButton("Ir a Configuración") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cerrar", { _, _ -> finish() })
            .setCancelable(false)
            .show()
    }
}
