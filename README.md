# EstrucDinami

App de estudio para el parcial teórico de **Dinámica Estructural y Diseño Sismorresistente (NSR-10)**: guía de estudio, banco de preguntas, práctica guiada, preguntas con gráfica, ejercicios numéricos con datos nuevos, simulacro cronometrado, calculadoras y seguimiento del progreso.

## Instalar en Android

1. En el celular, abre la sección **Releases** de este repositorio.
2. Descarga **EstrucDinami.apk** y ábrelo.
3. Si Android lo pide, permite **Instalar apps desconocidas** para el navegador o el gestor de archivos.

La app funciona sin internet; con conexión carga además las tipografías originales. El progreso se guarda en el celular. En **Mi progreso → Copia de seguridad** puedes copiarlo o cargarlo desde un archivo `.json`.

En el celular la app añade:
- **Botón atrás:** cierra el cuadro abierto o vuelve a Inicio; desde Inicio deja la app en segundo plano.
- **Imprimir o guardar PDF** (Herramientas → Todo el banco) con el sistema de impresión de Android.
- **Copiar** la copia de seguridad al portapapeles del teléfono.
- Barras del sistema del mismo color que el tema claro u oscuro.

## Cómo se compila

Cada cambio en la rama `main` dispara `.github/workflows/apk.yml`, que en los servidores de GitHub:

1. compila el proyecto de `android/`, que empaqueta `web/index.html` dentro del APK;
2. lo firma con `android/app/estrucdinami.keystore`;
3. lo publica en **Releases** con un número de versión nuevo.

Para actualizar el contenido (preguntas, guía, etc.) basta con cambiar `web/index.html` y subir el cambio. El APK nuevo se instala encima del anterior sin perder el progreso, porque siempre se firma con la misma clave.

> La clave de firma y su contraseña están en el repositorio para que la compilación funcione sin configuración extra. Está bien para una app personal; para publicarla en Google Play, crea una clave nueva y guárdala como *secret* de GitHub.

## Estructura

```
web/index.html           la app de estudio completa (un solo archivo)
android/                 proyecto Android (WebView)
.github/workflows/       compilación automática del APK
```
