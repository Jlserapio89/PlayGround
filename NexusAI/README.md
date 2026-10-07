# NexusAI — Asistente Autónomo Open Source para Android
**Nexus entre usuario, IA local (Termux), y sistema (Shizuku).**

App nativa Kotlin + Jetpack Compose (Material3), 100% offline y libre.

## Módulos
- `ui/chat` — Chat bidireccional con Markdown, sugerencias, favoritos
- `ui/history` — Historial + búsqueda + favoritos (Room)
- `ui/settings` — Termux / Shizuku / permisos / personalidad
- `integration/termux` — RUN_COMMAND oficial + bootstrap `llama-server`
- `integration/shizuku` — API v13 rikka.shizuku, estado + solicitud permiso
- `integration/llm` — Cliente OpenAI-compatible (`/v1/chat/completions`) vs `llama-server`/`ollama` en Termux
- `data/local` — Room + DataStore + BackupAgent
- `widget` — Acceso rápido `nexusai://chat`

## Requisitos
Android Studio Hedgehog+, JDK 17, minSdk 26.

## Puesta en marcha
1. Abrir `NexusAI/` en Android Studio, sync Gradle.
2. Instalar en el móvil **Termux** + **Termux:API** + **Shizuku Manager**.
3. En Termux: activar *Allow external apps*, luego en NexusAI → Ajustes → *Instalar backend*.
   Equivale a:
   ```bash
   pkg install -y llama-cpp curl termux-api
   llama-server -m ~/.nexusai/tinyllama.gguf -c 2048 --port 8080
   ```
4. En Shizuku Manager: iniciar servicio (ADB Wi-Fi/root) → autorizar NexusAI.
5. Compilar `app` y probar: el chat funciona incluso sin modelo (modo guía).

## Diseño
Estética propia oscura/violeta + teal, Material3 adaptativo, ES/EN (values-es), modo oscuro sistema.

## Libertad
Cambia el `.gguf` en `~/.nexusai/` y reinicia `llama-server` — sin reinstalar. Ningún dato sale del dispositivo.
