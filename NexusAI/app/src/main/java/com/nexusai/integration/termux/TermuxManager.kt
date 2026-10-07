package com.nexusai.integration.termux

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.nexusai.domain.model.TermuxStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Integración oficial Termux vía intent RUN_COMMAND.
 * Docs: https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent
 *
 * Requisitos en Termux:
 *  1. Instalar Termux + Termux:API
 *  2. Ejecutar una vez `termux-setup-storage` (opcional)
 *  3. Activar "Allow external apps": en Termux, Ajustes > Termux > Allow external apps
 *     o crear ~/.termux/termux.properties con `allow-external-apps=true`.
 *  4. Levantar el LLM: p.ej. `pkg install llama-cpp && llama-server -m modelo.gguf -c 4096 --port 8080`
 */
class TermuxManager @Inject constructor(@ApplicationContext private val ctx: Context) {

    companion object {
        const val TERMUX_PKG = "com.termux"
        const val API_PKG = "com.termux.api"
        const val RUN_COMMAND_ACTION = "com.termux.app.RUN_COMMAND"
        const val EXTRA_COMMAND_PATH = "com.termux.app.RUN_COMMAND_PATH"
        const val EXTRA_ARGUMENTS = "com.termux.app.RUN_COMMAND_ARGUMENTS"
        const val EXTRA_WORKDIR = "com.termux.app.RUN_COMMAND_WORKDIR"
        const val EXTRA_BACKGROUND = "com.termux.app.RUN_COMMAND_BACKGROUND"
        const val BOOTSTRAP_SCRIPT = "nexus-bootstrap.sh"
    }

    fun isInstalled(pkg: String): Boolean = try {
        ctx.packageManager.getPackageInfo(pkg, 0); true
    } catch (_: PackageManager.NameNotFoundException) { false }

    /** Envía un comando a Termux para ejecución en sesión background. */
    fun runCommand(commandPath: String, args: Array<String>, workDir: String = "/data/data/com.termux/files/home", background: Boolean = true) {
        val intent = Intent(RUN_COMMAND_ACTION).apply {
            setClassName(TERMUX_PKG, "com.termux.app.RunCommandService")
            putExtra(EXTRA_COMMAND_PATH, commandPath)
            putExtra(EXTRA_ARGUMENTS, args)
            putExtra(EXTRA_WORKDIR, workDir)
            putExtra(EXTRA_BACKGROUND, background)
        }
        ctx.startService(intent)
    }

    /** Script oficial de instalación del backend LLM libre (llama.cpp server + modelo TinyLlama por defecto). */
    fun bootstrapLlm(endpointPort: Int = 8080) {
        val script = """
            pkg update -y && pkg install -y llama-cpp curl termux-api
            mkdir -p ~/.nexusai && cd ~/.nexusai
            [ -f tinyllama.gguf ] || curl -L -o tinyllama.gguf https://huggingface.co/TinyLlama/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf
            pkill -f llama-server || true
            nohup llama-server -m ~/.nexusai/tinyllama.gguf -c 2048 --port $endpointPort > ~/.nexusai/llm.log 2>&1 &
        """.trimIndent()
        runCommand("/data/data/com.termux/files/usr/bin/bash", arrayOf("-lc", script))
    }

    suspend fun checkStatus(endpoint: String): TermuxStatus = withContext(Dispatchers.IO) {
        val reachable = try {
            val client = OkHttpClient.Builder().callTimeout(3, TimeUnit.SECONDS).build()
            val req = Request.Builder().url("$endpoint/health").get().build()
            client.newCall(req).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            // llama.cpp expone /health; ollama expone /api/tags -> probamos ambos
            try {
                val client = OkHttpClient.Builder().callTimeout(3, TimeUnit.SECONDS).build()
                client.newCall(Request.Builder().url("$endpoint/api/tags").get().build()).execute().use { it.isSuccessful }
            } catch (_: Exception) { false }
        }
        TermuxStatus(
            installed = isInstalled(TERMUX_PKG),
            allowExternalApps = isInstalled(TERMUX_PKG), // la concesión real se valida al enviar el intent sin SecurityException
            apiInstalled = isInstalled(API_PKG),
            llmEndpointReachable = reachable,
            lastCheck = System.currentTimeMillis()
        )
    }

    fun openTermuxSettings(): Intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent"))
}
