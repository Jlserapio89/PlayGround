package com.nexusai.integration.shizuku

import android.content.pm.PackageManager
import android.os.Build
import com.nexusai.domain.model.ShizukuStatus
import rikka.shizuku.Shizuku

/**
 * Integración Shizuku API v13 (rick.shizuku).
 * Permite a NexusAI ejecutar operaciones con permisos elevados ADB/root
 * delegados por Shizuku, p.ej. dumpsys, control de paquetes, modo silencioso.
 *
 * Flujo UX:
 *  1. Detectar si Shizuku Manager está instalado y corriendo.
 *  2. Solicitar permiso con Shizuku.requestPermission(0).
 *  3. Si denegado / no corriendo -> alerta con instrucciones (ADB wireless o root).
 */
object ShizukuManager {

    fun status(pm: PackageManager): ShizukuStatus {
        return try {
            val ping = Shizuku.pingBinder()
            val granted = try { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED } catch (_: Exception) { false }
            ShizukuStatus(
                available = ping,
                permissionGranted = granted,
                version = try { Shizuku.getVersion() } catch (_: Exception) { 0 },
                needsUserAction = !ping || !granted
            )
        } catch (_: Exception) {
            ShizukuStatus(available = false, permissionGranted = false, needsUserAction = true)
        }
    }

    fun requestPermission(code: Int = 0) {
        try { Shizuku.requestPermission(code) } catch (_: Exception) { /* Shizuku no activo */ }
    }

    /** Ejemplo de comando privilegiado: lista paquetes suspendidos (requiere permiso Shizuku). */
    fun privilegedExample(): String {
        if (Build.VERSION.SDK_INT < 23) return "API < 23 no soportada"
        return if (statusNoThrow().permissionGranted) "Shizuku listo: puedes invocar APIs del sistema desde NexusAI."
        else "Shizuku sin permiso: abre Shizuku Manager y autoriza a NexusAI."
    }

    private fun statusNoThrow() = try {
        ShizukuStatus(Shizuku.pingBinder(), Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
    } catch (_: Exception) { ShizukuStatus() }
}
