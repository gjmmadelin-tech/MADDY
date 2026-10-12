# MADDY Android Starter (v0.1)

Proyecto fuente para crear una app Android nativa con Kotlin + Jetpack Compose y un backend de generación de texto usando Supabase Edge Functions + Gemini API.

## Incluye
- Aplicación Android con entrada de un párrafo, selección de plataformas y estilo.
- Llamada HTTPS a un backend para generar borradores con IA.
- Sala de aprobación manual: los borradores no se publican automáticamente.
- Función Supabase `maddy-generate` que llama a Gemini desde el servidor.
- Sin claves secretas dentro del APK.

## Estado real de esta entrega
Este es un **starter de desarrollo**, no un APK compilado ni una app de producción. La generación real funcionará tras desplegar la función, añadir el secreto y configurar `MADDY_API_URL`. Las aprobaciones de esta versión se guardan en memoria y desaparecen al cerrar la app. No hay aún generación de imágenes/audio/video ni publicación en redes sociales.

## Requisitos
- Android Studio reciente con JDK 17 y SDK Android.
- Cuenta de Supabase (plan gratuito sujeto a límites).
- Clave API de Gemini (el acceso gratuito tiene límites y disponibilidad por modelo).
- Acceso a internet.

## Pasos para ejecutar
1. Descarga y descomprime el ZIP en una computadora.
2. Abre la carpeta `MADDY_Android_Starter` desde Android Studio.
3. Espera a que Gradle sincronice; instala SDK 35 si Android Studio lo solicita.
4. Crea un proyecto Supabase y usa Supabase CLI para iniciar/iniciar sesión.
5. En el proyecto Supabase, configura el secreto:
   `supabase secrets set GEMINI_API_KEY=TU_CLAVE`
6. Despliega la función desde la carpeta que contiene `supabase/`:
   `supabase functions deploy maddy-generate`
7. Copia la URL `https://TU_PROYECTO.supabase.co/functions/v1/maddy-generate`.
8. Sustituye el valor de `MADDY_API_URL` en `app/build.gradle.kts` por esa URL.
9. Para el primer test, configura la función para que requiera autenticación Supabase y añade autenticación de usuario en la app antes de compartirla públicamente. El ejemplo simplificado de la función no autentica usuarios y no debe dejarse abierto en producción.
10. Ejecuta la app en un teléfono Android conectado por USB o en un emulador.

## Para crear un APK
En Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**. Para distribuirlo, genera una versión firmada desde **Build > Generate Signed Bundle / APK**. No puedo proporcionar un APK firmado sin compilar y probar el proyecto en un entorno Android SDK.

## Próximas tareas obligatorias para producción
1. Autenticación, autorización por usuario, cuotas/rate limiting y validación de entrada.
2. Persistencia en PostgreSQL para borradores, revisiones, aprobación por versión y auditoría.
3. Generación multimedia:
   - Imagen: API de generación de imágenes o recurso con licencia.
   - Voz: servicio TTS o síntesis del dispositivo.
   - Video 1: ensamblar imágenes + narración + subtítulos con FFmpeg en un worker compatible.
   - Video 2: API de generación de clips; no asumir que existe un nivel gratuito permanente.
4. OAuth oficial por plataforma, tokens cifrados en servidor y adaptadores independientes.
5. Cola de publicación que solo acepte contenido aprobado; cualquier edición posterior invalida la aprobación anterior.
6. Estados de publicación reales, reintentos idempotentes, logs y manejo de expiración de tokens.

## Publicación oficial
- Meta: configurar Meta for Developers, Facebook Page y cuenta profesional de Instagram, permisos y revisión aplicable.
- TikTok: Content Posting API, OAuth, scopes aprobados y auditoría para quitar restricciones de visibilidad de clientes no auditados. En algunos flujos el usuario debe finalizar la publicación dentro de TikTok.
- YouTube: Google Cloud project, YouTube Data API v3, OAuth 2.0 y `youtube.upload`; revisar cuota y auditoría aplicable.

No pidas ni guardes contraseñas de redes sociales. Usa OAuth oficial. No pongas claves API en el APK, repositorio ni mensajes de chat.

## Documentación oficial
- Android Compose: https://developer.android.com/develop/ui/compose/setup
- Supabase Edge Functions: https://supabase.com/docs/guides/functions
- Supabase secrets: https://supabase.com/docs/guides/functions/secrets
- Gemini API pricing/tiers: https://ai.google.dev/gemini-api/docs/pricing
- TikTok Direct Post: https://developers.tiktok.com/docs/en/content-posting-api-get-started
- Instagram Platform: https://developers.facebook.com/docs/instagram-platform/
- YouTube upload: https://developers.google.com/youtube/v3/docs/videos/insert
