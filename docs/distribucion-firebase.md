# Distribuir el APK por Firebase App Distribution

Cada push a `master` (o un disparo manual) compila el APK `release` y lo sube a Firebase.
Firebase envía a los testers un correo con el enlace de descarga; ese correo es el aviso.
Workflow: `.github/workflows/distribuir.yml`. No hay secretos en el repositorio.

## Configuración única (unos 10 minutos)

1. **Firebase**: el proyecto `signaco-5f2dc` y la app `com.ufps.signaco` ya existen
   (están en `app/google-services.json`). En la consola, **App Distribution → Comenzar**.
2. **Cuenta de servicio**: Consola de Google Cloud (proyecto `signaco-5f2dc`) → IAM → Cuentas de
   servicio → Crear. Rol **Firebase App Distribution Admin**. Luego Claves → Agregar clave → JSON.
3. **GitHub** → repo `SignaCo` → Settings → Secrets and variables → Actions:
   - Secret `FIREBASE_SERVICE_ACCOUNT`: pega el contenido completo del JSON.
   - Variable `FIREBASE_TESTERS`: tu correo (varios, separados por coma).
   - Secret `DEBUG_KEYSTORE_BASE64` (recomendado): en tu PC con Java,
     `keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"`
     y luego `base64 -w0 debug.keystore` (en Windows PowerShell:
     `[Convert]::ToBase64String([IO.File]::ReadAllBytes("debug.keystore"))`). Pega el resultado.
     Sin él, cada APK lleva otra firma y hay que desinstalar la app antes de instalar la nueva.
4. **Probar**: Actions → "Distribuir a Firebase" → Run workflow.
5. **En el celular**: abre el correo de Firebase, instala la app "App Tester" o descarga el APK
   desde el enlace, y permite instalar de orígenes desconocidos.

## Decisiones
- Build `release` y no `debug`: ya está configurado así en `app/build.gradle.kts` y es más
  parecido a lo que se entrega. Va firmado con la clave de depuración (APK académico, sin Play Store).
- `versionCode` = n.º de ejecución + 100 (`-PciVersionCode`), para que cada APK se pueda instalar encima del anterior.
