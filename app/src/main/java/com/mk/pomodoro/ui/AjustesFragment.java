package com.mk.pomodoro.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.mk.pomodoro.R;
import com.mk.pomodoro.dao.DaoIntervalo;
import com.mk.pomodoro.dao.impl.DaoIntervaloImpl;
import com.mk.pomodoro.ui.viewmodel.GestorPomodoroViewModel;
import com.mk.pomodoro.util.ConstantesAppConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AjustesFragment extends Fragment  {

    private LinearLayoutCompat llcPersonalizadoInfo, llcObjetivoDiario, llcSesionAutomatica, llcTema, llcSonido, llcVibracion, llcNotificiacion;
    private ConstraintLayout clClasico, clExtendido, clCorto, clPersonalizado;
    private ImageView ivClasico, ivExtendido, ivCorto, ivPersonalizado;
    private MaterialSwitch sObjetivoDiario, sSesionAutomatica, sSonido, sVibracion, sNotificacion;
    private TextView tvObjetivoDiarioDescription;

    private SharedPreferences preferencias;
    private SharedPreferences.Editor actualizarPreferencias;
    private GestorPomodoroViewModel gestorPomodoro;
    private DaoIntervalo daoIntervalo;

    private boolean sesionAutomaticaHabilitado, objetivoHabilitado, sonidoHabilitado, vibracionHabilitada, notificacionHabilitado;
    private boolean tiempoObjetivoEstablecido, desdeSwitch;
    boolean siguiente = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencias = requireActivity().getSharedPreferences(ConstantesAppConfig.NOM_ARCHIVO_PREFERENCIAS, MODE_PRIVATE);
        actualizarPreferencias = preferencias.edit();
        gestorPomodoro = new ViewModelProvider(requireActivity()).get(GestorPomodoroViewModel.class);
        daoIntervalo = new DaoIntervaloImpl(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ajustes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);

        clExtendido = vista.findViewById(R.id.clExtendido);
        ivExtendido = vista.findViewById(R.id.ivExtendido);
        clClasico = vista.findViewById(R.id.clClasico);
        ivClasico = vista.findViewById(R.id.ivClasico);
        clCorto = vista.findViewById(R.id.clCorto);
        ivCorto = vista.findViewById(R.id.ivCorto);
        clPersonalizado = vista.findViewById(R.id.clPersonalizado);
        ivPersonalizado = vista.findViewById(R.id.ivPersonalizado);
        llcPersonalizadoInfo = vista.findViewById(R.id.llInfo);

        llcObjetivoDiario = vista.findViewById(R.id.llObjetivoDiario);
        sObjetivoDiario = vista.findViewById(R.id.sObjetivoDiario);
        tvObjetivoDiarioDescription = vista.findViewById(R.id.tvObjetivoDiarioDescription);
        tiempoObjetivoEstablecido = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_OBJETIVO, ConstantesAppConfig.V_TIEMPO_OBJETIVO_I) != 0;
        llcSesionAutomatica = vista.findViewById(R.id.llSesionAutomatica);
        sSesionAutomatica = vista.findViewById(R.id.sSesionAutomatica);

        llcTema = vista.findViewById(R.id.llTema);
        llcSonido = vista.findViewById(R.id.llSonido);
        sSonido = vista.findViewById(R.id.sSonido);
        llcVibracion = vista.findViewById(R.id.llVibracion);
        sVibracion = vista.findViewById(R.id.sVibracion);
        llcNotificiacion = vista.findViewById(R.id.llNotificacion);
        sNotificacion = vista.findViewById(R.id.sNotificacion);

        // Configuracion Predeterminada
        int opcionSeleccionada = preferencias.getInt(ConstantesAppConfig.C_OPCION_SELECCIONADA, ConstantesAppConfig.V_OPCION_SELECCIONADA);
        actualizarVisibilidad(opcionSeleccionada);

        sesionAutomaticaHabilitado = preferencias.getBoolean(ConstantesAppConfig.C_SESION_AUTOMATICA, ConstantesAppConfig.V_SESION_AUTOMATICA_B);
        sSesionAutomatica.setChecked(sesionAutomaticaHabilitado);
        sSesionAutomatica.setClickable(false);
        objetivoHabilitado = preferencias.getBoolean(ConstantesAppConfig.C_OBJETIVO, ConstantesAppConfig.V_OBJETIVO_B);
        sObjetivoDiario.setChecked(objetivoHabilitado);
        actualizarDescripcionObjetivoDiario();

        sSonido.setClickable(false);
        sVibracion.setClickable(false);
        sNotificacion.setClickable(false);
        sonidoHabilitado = preferencias.getBoolean(ConstantesAppConfig.C_SONIDO, ConstantesAppConfig.V_SONIDO_B);
        sSonido.setChecked(sonidoHabilitado);
        vibracionHabilitada = preferencias.getBoolean(ConstantesAppConfig.C_VIBRACION, ConstantesAppConfig.V_VIBRACION_B);
        sVibracion.setChecked(vibracionHabilitada);
        notificacionHabilitado = preferencias.getBoolean(ConstantesAppConfig.C_NOTIFICACION, ConstantesAppConfig.V_NOTIFICACION_B);
        sNotificacion.setChecked(notificacionHabilitado);

        // --

        clExtendido.setOnClickListener(v -> manejarCambioTipoPomodoro(() -> {
            guardarTiempos(45, 15);
            guardarOpcionSeleccionada(1);
            activarVisibilidad(View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        },true));

        clClasico.setOnClickListener(v -> manejarCambioTipoPomodoro(() -> {
            guardarTiempos(25, 5);
            guardarOpcionSeleccionada(2);
            activarVisibilidad(View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
        },true));

        clCorto.setOnClickListener(v -> manejarCambioTipoPomodoro(() -> {
            guardarTiempos(10, 2);
            guardarOpcionSeleccionada(3);
            activarVisibilidad(View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
        },true));

        clPersonalizado.setOnClickListener(v -> {
            boolean estaPersonalizadoActivado = preferencias.getBoolean(ConstantesAppConfig.C_PERSONALIZADO_ACTIVADO, ConstantesAppConfig.V_PERSONALIZADO_B);
            manejarCambioTipoPomodoro(() -> {
                if (estaPersonalizadoActivado) {
                    // Si ya se han ingresado datos, recuperamos los datos y activamos la opción
                    int tiempoTrabajo = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_TRABAJO_PERSONALIZADO, ConstantesAppConfig.V_TIEMPO_TRABAJO_PERSONALIZADO_I);
                    int tiempoDescanso = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_DESCANSO_PERSONALIZADO, ConstantesAppConfig.V_TIEMPO_DESCANSO_PERSONALIZADO_I);
                    gestorPomodoro.setTiempoTrabajo(tiempoTrabajo);
                    gestorPomodoro.setTiempoDescanso(tiempoDescanso);
                    guardarOpcionSeleccionada(4);
                    activarVisibilidad(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                } else {
                    abrirBottomSheetDialog();
                }
            }, estaPersonalizadoActivado);
        });

        clPersonalizado.setOnLongClickListener(v -> {
            manejarCambioTipoPomodoro(this::abrirBottomSheetDialog, false);
            return true;
        });

        boolean estaSnackbarActivado = preferencias.getBoolean(ConstantesAppConfig.C_SNACKBAR_PERSONALIZADO, ConstantesAppConfig.V_SNACKBAR_PERSONALIZADO_B);
        gestorPomodoro.getMostrarSnackbar().observe(getViewLifecycleOwner(), mostrar -> {
            if (mostrar && !estaSnackbarActivado) {
                View rootView = requireActivity().findViewById(android.R.id.content);
                Snackbar.make(rootView, "Mantén presionado para volver a editar los tiempos personalizados", Snackbar.LENGTH_LONG).show();
                actualizarPreferencias.putBoolean(ConstantesAppConfig.C_SNACKBAR_PERSONALIZADO, true);
                actualizarPreferencias.apply();
            }
        });
        boolean estaInfoActivado = preferencias.getBoolean(ConstantesAppConfig.C_INFO_PERSONALIZADO, ConstantesAppConfig.V_INFO_PERSONALIZADO_B);
        gestorPomodoro.setMostrarInfoPersonalizado(estaInfoActivado);
        gestorPomodoro.getMostrarInfoPersonalizado().observe(getViewLifecycleOwner(), mostrar -> {
            if (mostrar) {
                llcPersonalizadoInfo.setVisibility(View.VISIBLE);
            }
        });

        llcSesionAutomatica.setOnClickListener(v-> {
            sSesionAutomatica.setChecked(!sSesionAutomatica.isChecked());
            actualizarPreferencias.putBoolean(ConstantesAppConfig.C_SESION_AUTOMATICA, sSesionAutomatica.isChecked());
            actualizarPreferencias.apply();
        });

        llcObjetivoDiario.setOnClickListener(v -> {
            abrirTimePicker();
            desdeSwitch = false;
        });
        sObjetivoDiario.setOnCheckedChangeListener((vistaBoton, estaSeleccionado) -> {
            objetivoHabilitado = estaSeleccionado;
            if (estaSeleccionado) {
                if (!tiempoObjetivoEstablecido) {
                    desdeSwitch = true;
                    abrirTimePicker();
                    return;
                }
            }
            actualizarPreferencias.putBoolean(ConstantesAppConfig.C_OBJETIVO, objetivoHabilitado);
            actualizarPreferencias.apply();
            gestorPomodoro.setObjetivoCambiado(true);
        });

        llcTema.setOnClickListener(v -> mostrarDialogoTema());

        llcSonido.setOnClickListener(v -> {
            sonidoHabilitado = !sonidoHabilitado; // Invierte el estado (activo a inactivo o viceversa)
            sSonido.setChecked(sonidoHabilitado);
            actualizarPreferencias.putBoolean(ConstantesAppConfig.C_SONIDO, sonidoHabilitado);
            actualizarPreferencias.apply();
        });
        llcVibracion.setOnClickListener(v -> {
            vibracionHabilitada = !vibracionHabilitada;// Invierte el estado (activo a inactivo o viceversa)
            sVibracion.setChecked(vibracionHabilitada);
            actualizarPreferencias.putBoolean(ConstantesAppConfig.C_VIBRACION, vibracionHabilitada);
            actualizarPreferencias.apply();
        });
        llcNotificiacion.setOnClickListener(v-> {
            /*sNotificacion.setChecked(!sNotificacion.isChecked());
            actualizarPreferencias.putBoolean(ConstantesAppConfig.C_NOTIFICACION,sNotificacion.isChecked());
            actualizarPreferencias.apply();*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !estaNotificacionHabilitado()) {
                // Mostrar el BottomSheet
                ActivarNotificacionBottomSheet bottomSheet = ActivarNotificacionBottomSheet.newInstance();
                bottomSheet.show(getChildFragmentManager(), "ActivarNotificacionBottomSheet");
            } else {
                sNotificacion.setChecked(!sNotificacion.isChecked());
                actualizarPreferencias.putBoolean(ConstantesAppConfig.C_NOTIFICACION, sNotificacion.isChecked()).apply();
                gestorPomodoro.setNotificacionActivada(sNotificacion.isChecked());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        gestorPomodoro.getOpcionSeleccionada().observe(getViewLifecycleOwner(), this::actualizarVisibilidad);
    }

    private void manejarCambioTipoPomodoro(Runnable accionAceptar, boolean contieneDatos) {
        Boolean temporizadorIniciado = gestorPomodoro.getTemporizadorIniciado().getValue();
        if (Boolean.TRUE.equals(temporizadorIniciado)) {
            if (!preferencias.getBoolean(ConstantesAppConfig.C_DIALOGO_INFO_TIPO_POMODORO, ConstantesAppConfig.V_DIALOGO_INFO_TIPO_POMODORO_B)) {
                mostrarDialogoInformacionCambioTipoPomodoro(accionAceptar, contieneDatos);
            } else {
                if(contieneDatos) {
                    gestorPomodoro.setTipoPomodoroCambiado(true);
                }
                accionAceptar.run();
            }
        } else {
            accionAceptar.run();
        }
    }

    private void mostrarDialogoInformacionCambioTipoPomodoro(Runnable accionAceptar, boolean contieneDatos){

        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(56, 40, 56, 10);

        TextView mensaje = new TextView(requireContext());
        mensaje.setText(R.string.frag_aju_dialogo_tipo_pomodoro_info_Mensaje);
        mensaje.setPadding(8, 0, 0, 8);
        contenedor.addView(mensaje);

        CheckBox cbNoMostrar = new CheckBox(requireContext());
        cbNoMostrar.setText(R.string.frag_aju_dialogo_no_mostrar);
        cbNoMostrar.setPadding(0,8,0,8);
        contenedor.addView(cbNoMostrar);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Información")
                .setView(contenedor)
                .setPositiveButton("Continuar", (dialogInterface, i) -> {
                    if (accionAceptar != null) {
                        if(contieneDatos) {
                            gestorPomodoro.setTipoPomodoroCambiado(true);
                        }
                        accionAceptar.run();
                    }
                })
                .setNegativeButton("Volver", (dialogInterface, i) -> {
                })
                .setOnDismissListener(dialog -> {
                    if(cbNoMostrar.isChecked()){
                        actualizarPreferencias.putBoolean(ConstantesAppConfig.C_DIALOGO_INFO_TIPO_POMODORO, cbNoMostrar.isChecked());
                        actualizarPreferencias.apply();
                    }
                })
                .show();

    }

    private void activarVisibilidad(int visibilityExtendido, int visibilityClasico, int visibilityCorto, int visibilityPersonalizado) {
        animarVisibilidad(ivExtendido, visibilityExtendido);
        animarVisibilidad(ivClasico, visibilityClasico);
        animarVisibilidad(ivCorto, visibilityCorto);
        animarVisibilidad(ivPersonalizado, visibilityPersonalizado);
    }

    private void animarVisibilidad(ImageView imageView, int visibility) {
        if (visibility == View.VISIBLE && imageView.getVisibility() != View.VISIBLE) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setAlpha(0f);
            imageView.animate().alpha(1f).setDuration(250);
        } else if (visibility != View.VISIBLE) {
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    private void actualizarDescripcionObjetivoDiario(){
        if (tiempoObjetivoEstablecido) {
            int tiempoEnMilisegundos = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_OBJETIVO, ConstantesAppConfig.V_TIEMPO_OBJETIVO_I);
            tvObjetivoDiarioDescription.setText(getString(R.string.frag_aju_sec_conf_pom_op_objetivo_diario_desc_estab, formatearTiempo(tiempoEnMilisegundos)));
        } else {
            tvObjetivoDiarioDescription.setText(getString(R.string.frag_aju_sec_conf_pom_op_objetivo_diario_desc_def));
        }
    }

    private void abrirTimePicker() {

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(0)
                .setMinute(0)
                .setTitleText("Seleccione el tiempo objetivo")
                .build();

        String mensajeInformativo = "El tiempo máximo es de 12 horas.";

        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int tiempoTotalMilisegundos = daoIntervalo.obtenerTiempoTotalPorFecha(true,fechaHoy);

        if (tiempoTotalMilisegundos > 0) {
            mensajeInformativo += "\n\nPara superarte, elige un tiempo mayor al trabajo de hoy.";
        } else {
            mensajeInformativo += "\n\n¡Empieza con un buen tiempo! Elige un objetivo para hoy.";
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Información")
                .setMessage(mensajeInformativo)
                .setPositiveButton("Entendido", (dialogo, boton) -> {

                    siguiente = true;
                    picker.addOnPositiveButtonClickListener(dialogoInterno  -> {
                        int horas = picker.getHour();
                        int minutos = picker.getMinute();

                        if (horas > 12 || (horas == 12 && minutos > 0)) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Error")
                                    .setMessage("El tiempo máximo es de 12 horas")
                                    .setPositiveButton("Aceptar", null)
                                    .show();
                            if (desdeSwitch) {
                                sObjetivoDiario.setChecked(false); // Desactivar el switch
                            }
                            return;
                        }
                        sObjetivoDiario.setChecked(true); // Activar el switch
                        int tiempoEnMilisegundos = (horas * 60 + minutos) * 60 * 1000;
                        tvObjetivoDiarioDescription.setText(getString(R.string.frag_aju_sec_conf_pom_op_objetivo_diario_desc_estab, formatearTiempo(tiempoEnMilisegundos)));
                        actualizarPreferencias.putInt(ConstantesAppConfig.C_TIEMPO_OBJETIVO, tiempoEnMilisegundos);
                        actualizarPreferencias.putBoolean(ConstantesAppConfig.C_OBJETIVO, true);
                        actualizarPreferencias.apply();
                        gestorPomodoro.setObjetivoCambiado(true);
                        tiempoObjetivoEstablecido = true;
                        if (desdeSwitch) {
                            sObjetivoDiario.setChecked(true); // Mantener el switch activo
                        }

                    });
                    picker.show(getParentFragmentManager(), "MATERIAL_TIME_PICKER");

                    picker.addOnDismissListener(dialog -> {
                        siguiente = false;
                        if (desdeSwitch) {
                            int tiempoObjetivo = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_OBJETIVO, ConstantesAppConfig.V_TIEMPO_OBJETIVO_I);
                            if (tiempoObjetivo == 0) {
                                sObjetivoDiario.setChecked(false);
                            }
                        }
                    });
                })
                .setOnDismissListener(dialog -> {
                    if (desdeSwitch & !siguiente) {
                        int tiempoObjetivo = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_OBJETIVO, ConstantesAppConfig.V_TIEMPO_OBJETIVO_I);
                        if (tiempoObjetivo == 0) {
                            sObjetivoDiario.setChecked(false);
                        }
                    }
                })
                .show();
    }

    private void mostrarDialogoTema() {
        String[] temas = {"Sistema", "Claro", "Oscuro"};

        int temaActual = preferencias.getInt(ConstantesAppConfig.C_TEMA, ConstantesAppConfig.V_TEMA_I);
        Boolean temporizadorIniciado = gestorPomodoro.getTemporizadorIniciado().getValue();
        if (Boolean.TRUE.equals(temporizadorIniciado)) {
            if(!preferencias.getBoolean(ConstantesAppConfig.C_DIALOGO_INFO_TEMA, ConstantesAppConfig.V_DIALOGO_INFO_TEMA_B)){
                mostrarDialogoInformacionTema(temas, temaActual);
            } else {
                mostrarDialogoTemaSeleccionado(temas, temaActual);
            }
        } else {
            mostrarDialogoTemaSeleccionado(temas, temaActual);
        }
    }

    /*private void mostrarDialogoInformacion(String[] temas, int temaActual) {
        final String[] opcion = {"No volver a mostrar"};
        final boolean[] opcionSeleccionado = {false};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Información")
                .setMessage("Cambiar de tema detendrá el temporizador en curso. ¿Deseas continuar?")
                .setMultiChoiceItems(opcion, opcionSeleccionado, (dialog, which, isChecked) -> opcionSeleccionado[which] = isChecked)
                .setPositiveButton("Continuar", (dialogInterface, i) -> {
                    // Aquí puedes mostrar el diálogo del tema
                    mostrarDialogoTemaSeleccionado(temas, temaActual);
                })
                .setNegativeButton("Volver", (dialogInterface, i) -> {
                    // No hagas nada si el usuario selecciona "Volver"
                })
                .show();
    }*/

    private void mostrarDialogoInformacionTema(String[] temas, int temaActual) {

        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(56, 40, 56, 10);

        TextView mensaje = new TextView(requireContext());
        mensaje.setText(R.string.frag_aju_dialogo_tema_info_Mensaje);
        mensaje.setPadding(8, 0, 0, 8);
        contenedor.addView(mensaje);

        CheckBox cbNoMostrar = new CheckBox(requireContext());
        cbNoMostrar.setText(R.string.frag_aju_dialogo_no_mostrar);
        cbNoMostrar.setPadding(0,8,0,8);

        TypedValue valorFondo = new TypedValue();
        requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, valorFondo, true);
        cbNoMostrar.setForeground(ContextCompat.getDrawable(requireContext(), valorFondo.resourceId));
        contenedor.addView(cbNoMostrar);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        dialogBuilder.setTitle("Información");
        dialogBuilder.setView(contenedor);
        dialogBuilder.setPositiveButton("Continuar", (dialogInterface, i) -> mostrarDialogoTemaSeleccionado(temas, temaActual));
        dialogBuilder.setNegativeButton("Volver", (dialogInterface, i) -> {
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            if(cbNoMostrar.isChecked()){
                actualizarPreferencias.putBoolean(ConstantesAppConfig.C_DIALOGO_INFO_TEMA, cbNoMostrar.isChecked());
                actualizarPreferencias.apply();
            }
        });
        dialog.show();
    }

    private void mostrarDialogoTemaSeleccionado(String[] temas, int temaActual) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tema")
                .setSingleChoiceItems(temas, temaActual, null)
                .setPositiveButton("Aplicar", (dialogInterface, i) -> {
                    int selectedPosition = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
                    if (selectedPosition != -1) {
                        actualizarTema(selectedPosition);
                    }
                })
                .setNegativeButton("Cancelar", (dialogInterface, i) -> {
                })
                .show();
    }

    private void actualizarTema(int temaSeleccionado) {
        switch (temaSeleccionado) {
            case 0: // Sistema
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1: // Claro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2: // Oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        gestorPomodoro.setTemaCambiado(true);

        actualizarPreferencias.putInt(ConstantesAppConfig.C_TEMA, temaSeleccionado);
        actualizarPreferencias.apply();
    }

    private void guardarOpcionSeleccionada(int opcion) {
        gestorPomodoro.setOpcionSeleccionada(opcion);

        actualizarPreferencias.putInt(ConstantesAppConfig.C_OPCION_SELECCIONADA, opcion);
        actualizarPreferencias.apply();
    }

    private void guardarTiempos(int tiempoTrabajo, int tiempoDescanso) {
        gestorPomodoro.setTiempoTrabajo(tiempoTrabajo);
        gestorPomodoro.setTiempoDescanso(tiempoDescanso);
        gestorPomodoro.setTiemposActualizados(true);

        actualizarPreferencias.putInt(ConstantesAppConfig.C_TIEMPO_TRABAJO, tiempoTrabajo);
        actualizarPreferencias.putInt(ConstantesAppConfig.C_TIEMPO_DESCANSO, tiempoDescanso);
        actualizarPreferencias.apply();
    }

    private void actualizarVisibilidad(int opcion) {
        switch (opcion) {
            case 1:
                activarVisibilidad(View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case 2:
                activarVisibilidad(View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case 3:
                activarVisibilidad(View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case 4:
                activarVisibilidad(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                break;
        }
    }

    private void abrirBottomSheetDialog() {
        PersonalizarPomodoroBottomSheet bottomSheet = PersonalizarPomodoroBottomSheet.newInstance();
        boolean estaPersonalizadoActivado = preferencias.getBoolean(ConstantesAppConfig.C_PERSONALIZADO_ACTIVADO, ConstantesAppConfig.V_PERSONALIZADO_B);
        if (estaPersonalizadoActivado) {
            int tiempoTrabajo = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_TRABAJO_PERSONALIZADO, ConstantesAppConfig.V_TIEMPO_TRABAJO_PERSONALIZADO_I);
            int tiempoDescanso = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_DESCANSO_PERSONALIZADO, ConstantesAppConfig.V_TIEMPO_DESCANSO_PERSONALIZADO_I);

            Bundle args = new Bundle();
            args.putInt("tiempoTrabajo", tiempoTrabajo);
            args.putInt("tiempoDescanso", tiempoDescanso);
            bottomSheet.setArguments(args);
        }
        // Mostrar solo una vez el bottomSheetDialog
        if (!bottomSheet.isAdded() && bottomSheet.getMostrarBottomSheet()) {
            bottomSheet.show(getParentFragmentManager(), "PersonalizarPomodoroBottomSheet");
        }
    }

    public boolean estaNotificacionHabilitado(){
        return NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();
    }

    private String formatearTiempo(long milisegundos) {
        int horas = (int) ((milisegundos / (1000 * 60 * 60)) % 24);
        int minutos = (int) ((milisegundos / (1000 * 60)) % 60);

        if (horas > 0) {
            if (minutos > 0) {
                return String.format(Locale.getDefault(), "%dh %dmin", horas, minutos);
            } else {
                return String.format(Locale.getDefault(), (horas == 1) ? "%d hora" : "%d horas", horas);
            }
        } else {
            // if (minutos > 0)
            return String.format(Locale.getDefault(), (minutos == 1) ? "%d minuto" : "%d minutos", minutos);
        }
    }
}