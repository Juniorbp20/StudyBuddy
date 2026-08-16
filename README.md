# Study Buddy

Aplicación Android de gestión de tareas y hábitos de estudio, construida con **Kotlin + Views (XML)** y **Material 3**.

## Características

- **Gestión de tareas** con título, descripción, fecha/hora, categoría y prioridad (Alta, Media, Baja).
- **Categorías personalizables**: 4 por defecto (General, Estudio, Trabajo, Personal) con icono y color, más categorías propias creadas desde la app (8 iconos, paleta de colores) y borrado con reasignación automática de tareas a General.
- **Tareas recurrentes** (diarias/semanales) que se reprograman automáticamente al completarse.
- **Subtareas** con cascada al eliminar la tarea padre.
- **Etiquetas** (tags) con filtro por chips.
- **Búsqueda** y filtros combinados por categoría y prioridad.
- **Recordatorios** con notificaciones y canales dedicados (requiere permiso de notificaciones y alarmas exactas).
- **Resumen diario de tareas vencidas** vía WorkManager.
- **Calendario mensual** con indicador visual de días con tareas pendientes.
- **Modo enfoque (Pomodoro)** de 25 min con descanso, vibración y registro de sesiones.
- **Estadísticas**: progreso de completado, resumen (total, completadas, pendientes, vencidas, hoy, última semana), gráfico de los últimos 7 días y sesiones de enfoque.
- **Widget de escritorio** "Tareas de hoy" con lista actualizable.
- **Exportar/importar** tareas en JSON y CSV.
- **Tema oscuro y Material You** (colores dinámicos en Android 12+), tema claro/oscuro/sistema desde el menú.
- **Deshacer** al eliminar tareas (snackbar).
- **Multilingüe**: español (es) e inglés (en).

## Tecnologías

- Kotlin 2.0, corrutinas y Flow
- ViewBinding
- Room 2.6 (migraciones v1→v2→v3→v4 versionadas)
- Material Components 3, temas con Material You
- AlarmManager + WorkManager + notificaciones
- App Widgets (RemoteViewsService)
- Splash Screen API (androidx.core)

## Estructura

```
app/src/main/java/com/example/studybuddy/
├── MainActivity.kt          # Lista principal: búsqueda, filtros, chips, swipe, undo
├── AddTaskActivity.kt       # Crear/editar tarea
├── TaskDetailActivity.kt    # Detalle + subtareas
├── CalendarActivity.kt      # Calendario mensual
├── FocusActivity.kt         # Pomodoro
├── StatisticsActivity.kt    # Estadísticas y gráfico
├── adapter/                 # TaskAdapter, SubTaskAdapter
├── data/                    # ServiceLocator, TaskRepository (Flow)
├── model/                   # Task, SubTask, CategoryEntity, DAOs, AppDatabase + migraciones
├── notification/            # AlarmReceiver, AlarmManagerHelper, NotificationHelper, DailyOverdueWorker
├── ui/                      # TaskViewModel (Flow + LiveData)
├── util/                    # DateUtils, ExportImportHelper, BackupHelper, FocusSessionStore
├── view/                    # BarChartView, MonthCalendarView
└── widget/                  # TodayTasksWidgetProvider + Service
```

## Requisitos

- Android Studio (JDK 21)
- minSdk 23 · targetSdk 36

## Compilar y probar

```bash
./gradlew assembleDebug        # APK de debug
./gradlew lintDebug            # análisis estático
./gradlew testDebugUnitTest    # tests unitarios
./gradlew connectedDebugAndroidTest  # tests de instrumentación (migraciones Room)
```

Los tests de migración validan las rutas v1→v4, v2→v4 y v3→v4 usando los esquemas exportados en `app/schemas/`.

## CI

`.github/workflows/android.yml` ejecuta en cada push/PR a `main`/`master`: tests unitarios, lint y build del APK (subido como artefacto).

## Licencia

Uso personal/educativo.