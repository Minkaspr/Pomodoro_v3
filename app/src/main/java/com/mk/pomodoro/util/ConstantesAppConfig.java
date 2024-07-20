package com.mk.pomodoro.util;

public class ConstantesAppConfig {

    public static final String NOM_ARCHIVO_PREFERENCIAS = "pomodoroAppPref";
    // Para SharedPreferences - Claves
    public static final String C_TIEMPO_TRABAJO = "tiempoTrabajo";
    public static final String C_TIEMPO_DESCANSO = "tiempoDescanso";
    public static final String C_PESTANA_SELECCIONADA = "pestanaSeleccionada";
    public static final String C_OPCION_SELECCIONADA = "opcionSeleccionada";
    public static final String C_PERSONALIZADO_ACTIVADO = "estaPersonalizadoActivado";
    public static final String C_TIEMPO_TRABAJO_PERSONALIZADO = "tiempoTrabajoPersonalizado";
    public static final String C_TIEMPO_DESCANSO_PERSONALIZADO = "tiempoDescansoPersonalizado";
    public static final String C_SNACKBAR_PERSONALIZADO = "estaSnackbarPersonalizadoActivado";
    public static final String C_INFO_PERSONALIZADO = "estaInfoPersonalizadoActivado";
    public static final String C_OBJETIVO = "estaObjetivoActivado";
    public static final String C_SESION_AUTOMATICA = "estaSesionAutomaticaActivado";
    public static final String C_TIEMPO_OBJETIVO = "tiempoObjetivoEstablecido";
    public static final String C_TEMA = "temaSeleccionada";
    public static final String C_SONIDO = "estaSonidoActivado";
    public static final String C_VIBRACION = "estaVibracionActivado";
    public static final String C_NOTIFICACION = "estaNotificacionActivado";

    public static final String C_PERMISO_NOTIFICACION_LOCAL = "permisoNotificacionLocal";
    public static final String C_PERMISO_NOTIFICACION_SISTEMA_ACTUAL = "permisoNotificacionSistemaActual";
    public static final String C_PERMISO_NOTIFICACION_SISTEMA_ANTERIOR = "permisoNotificacionSistemaAnterior";

    public static final String C_DIALOGO_NOTIFICACION_BASE_MOSTRADO = "estaNotificacionBaseMostrado";
    public static final String C_DIALOGO_NOTIFICACION_PERSONALIZADO_MOSTRADO = "estaNotificacionMostrado";
    public static final String C_DIALOGO_INFO_TIPO_POMODORO = "estaDialogoInfoTipoPomodoroDesactivado";
    public static final String C_DIALOGO_INFO_TEMA = "estaDialogoInfoTemaDesactivado";

    // Para SharedPreferences - Valores - Predefinidos
    public static final int V_TIEMPO_TRABAJO_I = 25; // minutos
    public static final int V_TIEMPO_DESCANSO_I = 5; // minutos
    public static final int V_PESTANA_SELECCIONADA = 0; // Trabajo
    public static final int V_OPCION_SELECCIONADA = 2; // Clásico
    public static final boolean V_PERSONALIZADO_B = false;
    public static final int V_TIEMPO_TRABAJO_PERSONALIZADO_I = 0; // minutos
    public static final int V_TIEMPO_DESCANSO_PERSONALIZADO_I = 0; // minutos
    public static final boolean V_SNACKBAR_PERSONALIZADO_B = false;
    public static final boolean V_INFO_PERSONALIZADO_B = false;
    public static final boolean V_OBJETIVO_B = false;
    public static final boolean V_SESION_AUTOMATICA_B = false;
    public static final int V_TIEMPO_OBJETIVO_I = 0; // milisegundos
    public static final int V_TEMA_I = 0; // Sistema
    public static final boolean V_SONIDO_B = true;
    public static final boolean V_VIBRACION_B = true;
    public static final boolean V_NOTIFICACION_B = false;

    public static final int V_PERMISO_NOTIFICACION_LOCAL_I = 0; // -1 <> null | 0 <> false | 1 <> true
    public static final int V_PERMISO_NOTIFICACION_SISTEMA_ACTUAL_I = -1; // -1 <> null | 0 <> false | 1 <> true
    public static final int V_PERMISO_NOTIFICACION_SISTEMA_ANTERIOR_I = -1; // -1 <> null | 0 <> false | 1 <> true


    public static final boolean V_DIALOGO_NOTIFICACION_BASE_MOSTRADO_B = false;
    public static final boolean V_DIALOGO_NOTIFICACION_PERSONALIZADO_MOSTRADO_B = false;
    public static final boolean V_DIALOGO_INFO_TIPO_POMODORO_B = false;
    public static final boolean V_DIALOGO_INFO_TEMA_B = false;
}
