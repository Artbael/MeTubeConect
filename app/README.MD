# 🚀 MeTubeConect

**MeTubeConect** (antes *MeTube Sender*) es una aplicación nativa para Android desarrollada en **Kotlin** y **Jetpack Compose**. Su objetivo principal es automatizar y simplificar el envío de enlaces multimedia desde el dispositivo móvil hacia una instancia autohospedada del servidor **MeTube** en un entorno de Home Lab.

![Android](https://img.shields.io/badge/Android-Kotlin-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Retrofit](https://img.shields.io/badge/Network-Retrofit2-000000?style=for-the-badge)
![DataStore](https://img.shields.io/badge/Storage-DataStore-4285F4?style=for-the-badge)

---

## 🌟 Características Principales

- ⚡ **Lectura Automática del Portapapeles:** Detecta y procesa automáticamente enlaces multimedia al abrir la app mediante la escucha de foco de ventana (`onWindowFocusChanged`), superando las restricciones de privacidad modernas de Android.
- 🎯 **Enrutamiento Inteligente por Reglas:**
    - **Regla A (TikTok / Instagram / Reels):** Envío instantáneo con un solo toque utilizando parámetros optimizados (`best` / `mp4`).
    - **Regla B (YouTube y otros sitios):** Despliega un modal interactivo para personalizar la calidad (4K, 1080p, 720p, etc.), formato (MP4, MKV) o modo de extracción (Video + Audio / Solo Audio).
- ⚙️ **Configuración de Red Ágil:** Pantalla de ajustes con 5 casillas independientes (4 octetos de IP + Puerto) con formato automático, salto automático entre campos y soporte nativo para teclado numérico.
- 🧹 **Gestión Eficiente de Memoria:** Destrucción limpia de tareas en segundo plano mediante `finishAndRemoveTask()`, garantizando cero consumo de batería residual.
- 🎨 **Interfaz Moderna:** Diseñada 100% bajo los estándares de **Material Design 3** con modo oscuro nativo.

---

## 🛠️ Arquitectura y Tecnologías

La aplicación sigue los principios de arquitectura limpia recomendados por Android (**MVVM**):

* **Lenguaje:** [Kotlin](https://kotlinlang.org/) (v1.9.24)
* **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) con Material 3
* **Conectividad HTTP:** [Retrofit 2](https://square.github.io/retrofit/) & OkHttp
* **Persistencia Local:** [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) (para guardar la IP, puerto y preferencias predeterminadas)
* **Gestión de Estado:** `StateFlow` & Corrutinas de Kotlin

---

## 📸 Capturas de Pantalla



| Pantalla de Ajustes (IP/Puerto) | Modal de Configuración (YouTube) |
|:---:|:---:|
| ![Settings](screenshots/settings.jpg) | ![Modal](screenshots/modal.jpg) |

---

## 🚀 Instalación y Compilación

### Requisitos
* Android Studio Koala (o posterior)
* JDK 17 (Java Development Kit)
* Android SDK 24 (Android 7.0) o superior

### Pasos
1. Clona este repositorio:
   ```bash
   git clone [https://github.com/TuUsuario/MeTubeConect.git](https://github.com/TuUsuario/MeTubeConect.git)
   Abre el proyecto en Android Studio.

⚙️ Configuración Inicial

* Abre MeTubeConect en tu dispositivo.

* Toca el icono de Ajustes ⚙️ en la esquina superior derecha.

* Ingresa la Dirección IP y Puerto de tu servidor local de MeTube.

* Presiona Guardar.



👨‍💻 Autor

Desarrollado con ❤️ por Artbael.