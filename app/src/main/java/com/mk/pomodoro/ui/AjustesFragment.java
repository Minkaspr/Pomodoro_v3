package com.mk.pomodoro.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.mk.pomodoro.R;
import com.mk.pomodoro.controller.Temporizador;
import com.mk.pomodoro.dao.DaoIntervalo;
import com.mk.pomodoro.dao.impl.DaoIntervaloImpl;
import com.mk.pomodoro.ui.viewmodel.GestorPomodoroViewModel;
import com.mk.pomodoro.util.ConstantesAppConfig;
import com.mk.pomodoro.util.PomodoroAppDB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AjustesFragment extends Fragment implements PersonalizarPomodoroBottomSheet.OnOptionChangeListener {

    private LinearLayoutCompat llcTema, llcSonido, llcVibracion, llcPersonalizadoInfo, llObjetivoDiario;
    private ConstraintLayout clClasico, clExtendido, clCorto, clPersonalizado;
    private ImageView ivClasico, ivExtendido, ivCorto, ivPersonalizado;
    private MaterialSwitch sObjetivoDiario, sSonido, sVibracion;

    private TextView tvObjetivoDiarioDescription;
    private String tiempoSeleccionado = "";

    private SharedPreferences preferencias;
    private SharedPreferences.Editor actualizarPreferencias;
    private GestorPomodoroViewModel gestorPomodoro;
    private DaoIntervalo daoIntervalo;

    boolean objetivoHabilitado, sonidoHabilitado, vibracionHabilitada;
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

        llObjetivoDiario = vista.findViewById(R.id.llObjetivoDiario);
        sObjetivoDiario = vista.findViewById(R.id.switchObjetivoDiario);
        tvObjetivoDiarioDescription = vista.findViewById(R.id.tvObjetivoDiarioDescription);

        llcTema = vista.findViewById(R.id.llTema);
        llcSonido = vista.findViewById(R.id.llSonido);
        sSonido = vista.findViewById(R.id.switchSonido);
        llcVibracion = vista.findViewById(R.id.llVibracion);
        sVibracion = vista.findViewById(R.id.switchVibracion);

        // Configuracion Predeterminada
        int opcionSeleccionada = preferencias.getInt(ConstantesAppConfig.C_OPCION_SELECCIONADA, ConstantesAppConfig.V_OPCION_SELECCIONADA);
        actualizarVisibilidad(opcionSeleccionada);
        objetivoHabilitado = preferencias.getBoolean(ConstantesAppConfig.C_OBJETIVO, ConstantesAppConfig.V_OBJETIVO_B);
        sObjetivoDiario.setChecked(objetivoHabilitado);

        sSonido.setClickable(false);
        sVibracion.setClickable(false);
        sonidoHabilitado = preferencias.getBoolean(ConstantesAppConfig.C_SONIDO, ConstantesAppConfig.V_SONIDO_B);
        sSonido.setChecked(sonidoHabilitado);
        vibracionHabilitada = preferencias.getBoolean(ConstantesAppConfig.C_VIBRACION, ConstantesAppConfig.V_VIBRACION_B);
        sVibracion.setChecked(vibracionHabilitada);

        clExtendido.setOnClickListener(v -> {
            guardarTiempos(45, 15);
            guardarOpcionSeleccionada(1);
            activarVisibilidad(View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        });
        clClasico.setOnClickListener(v -> {
            guardarTiempos(25, 5);
            guardarOpcionSeleccionada(2);
            activarVisibilidad(View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
        });
        clCorto.setOnClickListener(v -> {
            guardarTiempos(10, 2);
            guardarOpcionSeleccionada(3);
            activarVisibilidad(View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
        });
        clPersonalizado.setOnClickListener(v -> {
            boolean estaPersonalizadoActivado = preferencias.getBoolean(ConstantesAppConfig.C_PERSONALIZADO_ACTIVADO, ConstantesAppConfig.V_PERSONALIZADO_B);

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
        clPersonalizado.setOnLongClickListener(v -> {
            abrirBottomSheetDialog();
            return true;
        });

        llObjetivoDiario.setOnClickListener(v -> abrirTimePicker());

        llcTema.setOnClickListener(this::mostrarDialogoTema);

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
        sObjetivoDiario.setOnCheckedChangeListener((vistaBoton, estaSeleccionado) -> {
            int tiempoObjetivo = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_OBJETIVO, ConstantesAppConfig.V_TIEMPO_OBJETIVO_I);
            objetivoHabilitado = estaSeleccionado;
            if (tiempoObjetivo != 0) {
                gestorPomodoro.setObjetivoCambiado(true);
                actualizarPreferencias.putBoolean(ConstantesAppConfig.C_OBJETIVO, objetivoHabilitado);
                actualizarPreferencias.apply();
            } else {
                abrirTimePicker();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        // Vuelve a observar los cambios en la opción seleccionada en tiempo real
        gestorPomodoro.getOpcionSeleccionada().observe(getViewLifecycleOwner(), this::actualizarVisibilidad);
    }

    @Override
    public void onOptionChange(int option) {
        simularClick(option);
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

                    picker.addOnPositiveButtonClickListener(dialogoInterno  -> {
                        int horas = picker.getHour();
                        int minutos = picker.getMinute();

                        if (horas > 12 || (horas == 12 && minutos > 0)) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Error")
                                    .setMessage("El tiempo máximo es de 12 horas")
                                    .setPositiveButton("Aceptar", null)
                                    .show();
                            return;
                        }
                        tiempoSeleccionado = horas + "h " + minutos + "min";
                        tvObjetivoDiarioDescription.setText("Objetivo diario establecido: " + tiempoSeleccionado + "\n• Mantener presionado para editar");
                        sObjetivoDiario.setChecked(true); // Activar el switch
                        int tiempoEnMilisegundos = (horas * 60 + minutos) * 60 * 1000;
                        gestorPomodoro.setObjetivoCambiado(true);

                        actualizarPreferencias.putInt(ConstantesAppConfig.C_TIEMPO_OBJETIVO, tiempoEnMilisegundos);
                        actualizarPreferencias.putBoolean(ConstantesAppConfig.C_OBJETIVO, true);
                        actualizarPreferencias.apply();
                    });
                    picker.show(getParentFragmentManager(), "MATERIAL_TIME_PICKER");
                })
                .show();
    }


    /*private void mostrarDialogoTema(View v) {
        String[] temas = {"Sistema", "Claro", "Oscuro"};

        int temaActual = preferencias.getInt(ConstantesAppConfig.C_TEMA, ConstantesAppConfig.V_TEMA_I);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tema")
                .setSingleChoiceItems(temas, temaActual, null)
                .setPositiveButton("Aplicar", (dialogInterface, i) -> {
                    int selectedPosition = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();

                    if (selectedPosition != -1) {
                        actualizarTema(selectedPosition);
                    }
                    v.setPressed(false);
                })
                .setNegativeButton("Cancelar", (dialogInterface, i) -> v.setPressed(false))
                .show();
    }*/

    private void mostrarDialogoTema(View v) {
        String[] temas = {"Sistema", "Claro", "Oscuro"};

        int temaActual = preferencias.getInt(ConstantesAppConfig.C_TEMA, ConstantesAppConfig.V_TEMA_I);
        Boolean temporizadorIniciado = gestorPomodoro.getTemporizadorIniciado().getValue();
        if (Boolean.TRUE.equals(temporizadorIniciado)) {
            mostrarDialogoInformacion(v, temas, temaActual);
        } else {
            mostrarDialogoTemaSeleccionado(temas, temaActual);
        }
    }

    private void mostrarDialogoInformacion(View v, String[] temas, int temaActual) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Información")
                .setMessage("Cambiar de tema detendrá el temporizador en curso. ¿Deseas continuar?")
                .setPositiveButton("Continuar", (dialogInterface, i) -> {
                    // Aquí puedes mostrar el diálogo del tema
                    mostrarDialogoTemaSeleccionado(temas, temaActual);
                })
                .setNegativeButton("Volver", (dialogInterface, i) -> {
                    // No hagas nada si el usuario selecciona "Volver"
                })
                .show();
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
                    // No hagas nada si el usuario selecciona "Cancelar"
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

    private void simularClick(int opcion) {
        switch (opcion) {
            case 1:
                clExtendido.performClick();  // Simula un clic en clExtendido
                break;
            case 2:
                clClasico.performClick();  // Simula un clic en clClasico
                break;
            case 3:
                clCorto.performClick();  // Simula un clic en clCorto
                break;
            case 4:
                // clPersonalizado.performClick();  // Simula un clic en clPersonalizado
                break;
        }
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
        bottomSheet.setOnOptionChangeListener(this);
        bottomSheet.setOnCloseListener(() -> {
            int ultimaOpcionSeleccionada = preferencias.getInt(ConstantesAppConfig.C_OPCION_SELECCIONADA, ConstantesAppConfig.V_OPCION_SELECCIONADA);
            simularClick(ultimaOpcionSeleccionada);
        });
        if (!bottomSheet.isAdded() && bottomSheet.getShouldShowBottomSheet()) {
            bottomSheet.show(getParentFragmentManager(), "PersonalizarPomodoroBottomSheet");
        }
    }
}