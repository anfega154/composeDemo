This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [GitHub](https://github.com/JetBrains/compose-multiplatform/issues).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.

ComposeDemo

ComposeDemo es una aplicación multiplataforma desarrollada utilizando Kotlin Multiplatform (KMP) que integra un módulo Android llamado composeApp. Este proyecto está diseñado para ser ejecutado en múltiples plataformas, incluyendo Android e iOS, y utiliza diversas librerías y herramientas modernas para su desarrollo.

Estructura del Proyecto

	•	settings.gradle: Configura el proyecto raíz y gestiona la resolución de dependencias.
	•	build.gradle (Proyecto raíz): Define las configuraciones globales y las dependencias del proyecto.
	•	composeApp: Módulo principal del proyecto que contiene la implementación específica para Android e iOS, así como las configuraciones comunes.

Configuración de Entorno

	1.	Requisitos Previos:
	•	Android Studio con soporte para Kotlin Multiplatform.
	•	JDK 17 instalado y configurado.
	•	Conexión a internet para resolver las dependencias.
	2.	Dependencias Principales:
	•	Kotlin Multiplatform: Para compartir código entre Android e iOS.
	•	Jetpack Compose: Para la interfaz de usuario declarativa en Android.
	•	Realm: Base de datos local para la persistencia de datos.
	•	Firebase: Servicios de backend como Crashlytics, Performance Monitoring, y Analytics.
	•	Ktor: Cliente HTTP para realizar solicitudes de red.
	•	Koin: Inyección de dependencias.

Configuración de Android

En el módulo composeApp, se ha configurado el target Android con las siguientes opciones:

	•	namespace: com.mantum.demo
	•	compileSdk: 33
	•	minSdk: 21
	•	targetSdk: 33
	•	jvmTarget: 17

Las dependencias específicas para Android incluyen:

	•	Jetpack Compose y su ecosistema.
	•	SQLDelight para manejar bases de datos SQLite.
	•	Firebase y Realm para la gestión de datos y monitoreo.

Configuración de iOS

Para la parte de iOS, se utilizan las siguientes configuraciones:

	•	BaseName: ComposeApp
	•	Framework: Estático para mejor compatibilidad con proyectos iOS.

Las dependencias específicas para iOS incluyen:

	•	SQLDelight para bases de datos.
	•	Ktor para la conectividad de red.

Ejecución del Proyecto

	1.	Android:
	•	Ejecuta el proyecto desde Android Studio.
	•	Asegúrate de tener configuradas las herramientas necesarias para la compilación de Kotlin Multiplatform.
	2.	iOS:
	•	Abre el proyecto en Xcode utilizando la configuración proporcionada.
	•	Asegúrate de tener configurado el entorno de desarrollo para iOS.