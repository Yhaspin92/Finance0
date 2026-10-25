# 💳 Finance0

> **Aplicación Android de Finanzas Personales con Lector Inteligente de Notificaciones Bancarias, Control de Tarjetas, Meses Sin Intereses (MSI), Nómina y Actualizaciones Automáticas vía GitHub.**

---

## 🌟 Características Principales

### 1. 🤖 Lector Inteligente de Notificaciones Bancarias (`NotificationListenerService`)
- Detecta y clasifica en tiempo real:
  - **Cargos y Compras** (débito y crédito).
  - **Depósitos y Abonos** (sueldo, nómina, transferencias SPEI recibidas).
  - **Transferencias entre cuentas** y traspasos.
  - **Pagos de Tarjetas de Crédito** (reduciendo automáticamente la deuda registrada).
- **Soporte especializado para bancos y fintechs de México**:
  - BBVA México, Nu México (Cuenta y Tarjeta de Crédito), Mercado Pago, Santander, Citibanamex, Banorte, Hey Banco / Banregio, Banco Azteca, HSBC, Scotiabank, Spin by OXXO, Klar, RappiCard, Stori, Ualá.
- **Extractor Heurístico Universal**: Detecta montos (`$`, `MXN`), comercios y números de tarjeta/cuenta (`*1234`) incluso en notificaciones con formatos novedosos.
- **Simulador Interactivo en Vivo**: Pantalla de prueba para simular notificaciones bancarias con plantillas predefinidas o texto personalizado.

### 2. 💳 Control de Tarjetas de Crédito y Meses Sin Intereses (MSI)
- Monitoreo de límite de crédito, deuda acumulada y saldo disponible.
- Indicador visual de porcentaje de uso de crédito (% de riesgo).
- **Cuenta regresiva inteligente**:
  - Días restantes para la **Fecha de Corte**.
  - Días restantes para la **Fecha Límite de Pago**.
- **Módulo de Compras a Meses Sin Intereses (MSI)**:
  - Registro manual de compras a plazos (3, 6, 9, 12, 18, 24 MSI).
  - Cálculo instantáneo de mensualidades fijas (`$Monto / Meses`).
  - Barra de progreso por compra: Avance de meses pagados (`Mes X de Y`) y saldo remanente.
  - Botón de un toque para avanzar mensualidad pagada (`+1`).

### 3. ✍️ Registro Manual Completo
- Alta rápida de:
  - **Gastos**: Alimentos, Restaurantes, Gasolina, Servicios, Suscripciones, Salud, Compras, etc.
  - **Ingresos**: Nómina, Ventas, Rendimientos.
  - **Transferencias**: Entre cuentas propias (débito a débito, débito a inversión).
  - **Pago de Tarjetas de Crédito**: Deduce de la cuenta de débito y abona a la tarjeta elegida.

### 4. 📅 Calendario Financiero y Nómina
- Configuración por defecto: **Quincenal** (días 15 y fin de mes).
- Banner con cuenta regresiva: *"¡Faltan X días para la quincena!"* y monto esperado.
- Cuadrícula interactiva mensual con puntos de colores:
  - 🟢 Nómina / Día de pago.
  - 🟣 Fecha de corte de tarjeta.
  - 🟡 Fecha límite de pago y servicios fijos (CFE, Internet, Netflix, etc.).

### 5. 🚀 Actualizaciones Automáticas OTA vía GitHub (Sin Google Play)
- Diseñado como proyecto personal e independiente.
- `GitHubUpdateManager` integrado que consulta la API de **GitHub Releases**.
- Compara la versión instalada contra la última versión en GitHub.
- Descarga en segundo plano e inicia la instalación directa del `.apk` mediante Android `FileProvider`.
- Incluye flujo automatizado de **GitHub Actions** (`.github/workflows/build-apk.yml`) para compilar y publicar releases al crear un tag (ej. `v1.0.1`).

---

## 🎨 Estilo Visual: Dark Fintech Premium
- **Fondo**: Grafito oscuro y carbón (`#0D0F14`, `#131722`).
- **Superficies**: Tarjetas estilo *Glassmorphism* con bordes sutiles y gradientes.
- **Acentos**:
  - 🟢 **Verde Esmeralda Neón** (`#00E676`) para ingresos, balances positivos y acciones principales.
  - 🟣 **Violeta Neón** (`#7C4DFF`) para transferencias, tarjetas de crédito y MSI.
  - 🔵 **Cyan Neón** (`#00E5FF`) para notificaciones automáticas y simulador.
  - 🔴 **Coral Red** (`#FF5252`) para deudas y egresos.

---

## 🛠️ Tecnologías Empleadas

- **Lenguaje**: Kotlin 2.2 / 2.3
- **UI Toolkit**: Jetpack Compose con Material 3 y Material Icons Extended
- **Arquitectura**: Clean Architecture + Reactive StateFlows + AndroidViewModel
- **Persistencia**: Kotlin Serialization JSON local con caché reactiva
- **Servicio Android**: `NotificationListenerService` (`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`)
- **Instalador OTA**: `androidx.core.content.FileProvider` (`android.permission.REQUEST_INSTALL_PACKAGES`)

---

## 📦 Compilación y Generación del APK

### Compilar localmente (Debug APK):
```powershell
# En Windows PowerShell con JDK de Android Studio:
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
./gradlew.bat assembleDebug
```
El archivo APK generado se ubica en:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Publicar una Actualización Automática a GitHub:
1. Sube tu código a tu repositorio de GitHub:
   ```bash
   git add .
   git commit -m "Lanzamiento de Finance0 v1.0.0"
   git remote add origin https://github.com/TU_USUARIO/Finance0.git
   git push -u origin master
   ```
2. Para publicar una actualización OTA que todos los dispositivos con Finance0 descarguen automáticamente:
   ```bash
   git tag v1.0.1
   git push origin v1.0.1
   ```
   *El flujo de GitHub Actions compilará el APK y creará el release de forma 100% automatizada.*
