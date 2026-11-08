# Audix

**Audix** es una app de audio para **Android TV** y **celulares Android**, hecha en Kotlin con Jetpack Compose. Aplica ecualización y efectos de sonido a todo el audio del sistema, no solo a una app: Netflix, YouTube, Spotify o lo que esté sonando en la tele.

La hice porque los TV con Android traen pocas opciones de sonido y la mayoría de los ecualizadores de Play Store no están pensados para usarse con control remoto. Audix se maneja completo con el D-pad del control, y también funciona con pantalla táctil en el celular.

---

## ¿Qué hace?

### 🎚️ Ecualizador
- Ecualizador por bandas para controlar graves, medios y agudos.
- Las **frecuencias de cada banda se pueden mover** (← / →), no solo la ganancia.
- Usa `DynamicsProcessing` cuando el dispositivo lo soporta, que es más preciso. Si no, usa el `Equalizer` clásico de Android.

### 🔊 Efectos
| Efecto | Para qué sirve |
|---|---|
| **Bass Boost** | Refuerza los graves |
| **Loudness** | Sube el volumen percibido sin cambiar el balance |
| **Bass Stabilizer** | Compresor en graves para que no "retumben" ni saturen |
| **Vocal Clarity** | Destaca las voces, útil en películas con diálogos bajos |
| **Smart Volume** | Empareja escenas muy bajas con escenas muy fuertes |
| **Stereo Wide** | Abre la imagen estéreo para un sonido más amplio |
| **Limiter** | Pone un techo para evitar distorsión |

Los efectos de dinámica usan un **compresor multibanda** de 4 bandas (graves, medios-graves, medios y agudos) y un limitador al final de la cadena.

### 📈 Volumen+
Sube el volumen **por encima del máximo normal**: hasta un 300 %, que equivale a unos +9,5 dB. Mientras está activo, el limitador se enciende solo y baja su techo para que el sonido no se rompa.

### 🎵 Presets
Configuraciones listas con un toque:
`Flat` · `Rock` · `Jazz` · `Pop` · `Cinema` · `Cine Barra` · `Voz` · `Bass+` · `Agudos+`

**Cine Barra** está ajustado para una barra de sonido 2.0 sin subwoofer. También se puede guardar un preset **Personalizado**.

### 💾 Perfiles
Guarda **toda** la configuración (EQ, efectos y Volumen+) con un nombre propio. Los perfiles se pueden cargar, renombrar, duplicar, reordenar y eliminar.

### 🎨 Temas
Cinco temas visuales: **Oscuro**, **Claro**, **Negro**, **Océano** y **Atardecer**.

### ⚙️ Siempre activo
- Corre como **servicio en primer plano**, así los efectos siguen aplicados aunque cierres la app.
- **Se inicia sola al encender** el TV o el celular.
- Al abrir, muestra qué efectos soporta tu dispositivo (✓ / ✗).

---

## Interfaz

- **En TV:** diseño adaptado a pantalla grande con pestañas **Inicio**, **Efectos** y **Perfiles**. Navegas con el D-pad, presionas **OK** para empezar a ajustar un control, usas **← / →** para cambiar el valor y **BACK** para salir.
- **En celular:** diseño vertical con control táctil.

---

## Estructura del proyecto

```
app/src/main/java/app/audix/
├── MainActivity.kt         # Punto de entrada, inicia el servicio y la UI
├── AudioEngine.kt          # Motor de audio: efectos, EQ, presets, perfiles y guardado
├── AudioEffectService.kt   # Servicio en primer plano que mantiene los efectos vivos
├── BootReceiver.kt         # Arranca el servicio al encender el dispositivo
└── Ui.kt                   # Toda la interfaz en Compose (TV y celular) y los temas
```

## Tecnologías

- **Kotlin** + **Jetpack Compose** (Material 3)
- APIs de audio de Android: `Equalizer`, `BassBoost`, `LoudnessEnhancer`, `Virtualizer` y `DynamicsProcessing`
- Configuración guardada con `SharedPreferences` (los perfiles en JSON)

## Requisitos

- Android **8.0 (API 26)** o superior
- Android TV (Leanback) o teléfono Android
- Algunos efectos dependen del hardware: si tu dispositivo no los soporta, la app lo indica

## Compilar

```bash
git clone https://github.com/Argomedo/Audix.git
cd Audix
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/`. También puedes abrir el proyecto en Android Studio y ejecutarlo directo.

## Instalar en el TV

1. Activa **Opciones de desarrollador** y **Depuración ADB** en el TV.
2. Desde el PC:
   ```bash
   adb connect IP_DEL_TV
   adb install Audix.apk
   ```

También puedes descargar el APK desde la sección **Releases**.

---

## Estado

🚧 En desarrollo (versión **0.2**).

Pendiente:
- Mejorar algunos apartados de la interfaz
- Corregir un corte de audio que ocurre en algunos celulares

---
