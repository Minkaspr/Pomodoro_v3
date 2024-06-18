package com.mk.pomodoro.ui;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
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
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.mk.pomodoro.R;
import com.mk.pomodoro.ui.viewmodel.GestorPomodoroViewModel;

public class AjustesFragment extends Fragment implements PersonalizarPomodoroBottomSheet.OnOptionChangeListener {

    private LinearLayoutCompat llcTema, llcSonido, llcVibracion, llcPersonalizadoInfo;
    private ConstraintLayout clClasico, clExtendido, clCorto, clPersonalizado;
    private ImageView ivClasico, ivExtendido, ivCorto, ivPersonalizado;
    private MaterialSwitch sSonido, sVibracion;

    private LinearLayoutCompat llObjetivoDiario;
    private MaterialSwitch switchObjetivoDiario;
    private TextView tvObjetivoDiarioDescription;
    private String tiempoSeleccionado = "";

    private SharedPreferences preferencias;
    private SharedPreferences.Editor actualizarPreferencias;
    private GestorPomodoroViewModel gestorPomodoro;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ajustes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        llcPersonalizadoInfo = view.findViewById(R.id.llInfo);
        llcTema = view.findViewById(R.id.llTema);
        llcTema.setOnClickListener(this::mostrarDialogoTema);
        llcSonido = view.findViewById(R.id.llSonido);
        llcVibracion = view.findViewById(R.id.llVibracion);

        // Instanciar los elementos de la interfaz
        llObjetivoDiario = view.findViewById(R.id.llObjetivoDiario);
        switchObjetivoDiario = view.findViewById(R.id.switchObjetivoDiario);
        tvObjetivoDiarioDescription = view.findViewById(R.id.tvObjetivoDiarioDescription);

        preferencias = requireActivity().getSharedPreferences("minka", MODE_PRIVATE);
        actualizarPreferencias = preferencias.edit();
        gestorPomodoro = new ViewModelProvider(requireActivity()).get(GestorPomodoroViewModel.class);

        // Asignar eventos onClick
        llObjetivoDiario.setOnClickListener(v -> {
            // Abrir el timepicker
            abrirTimePicker();
            Toast.makeText(getActivity(), "Toque para establecer un objetivo diario", Toast.LENGTH_SHORT).show();
        });

        boolean mostrarInfoPersonalizado = preferencias.getBoolean("mostrarInfoPersonalizado", false);
        gestorPomodoro.setMostrarInfoPersonalizado(mostrarInfoPersonalizado);
        gestorPomodoro.getMostrarInfoPersonalizado().observe(getViewLifecycleOwner(), mostrar -> {
            if (mostrar) {
                llcPersonalizadoInfo.setVisibility(View.VISIBLE);
            }
        });

        boolean snackbarMostrado = preferencias.getBoolean("snackbarMostrado", false);
        gestorPomodoro.getMostrarSnackbar().observe(getViewLifecycleOwner(), mostrar -> {
            if (mostrar && !snackbarMostrado) {
                View rootView = requireActivity().findViewById(android.R.id.content);
                Snackbar.make(rootView, "Mantén presionado para volver a editar los tiempos personalizados", Snackbar.LENGTH_LONG).show();
                actualizarPreferencias.putBoolean("snackbarMostrado", true);
                actualizarPreferencias.apply();
            }
        });

        sSonido = view.findViewById(R.id.switchSonido);
        sVibracion = view.findViewById(R.id.switchVibracion);
        verificarSonidoVibracion();
        sSonido.setClickable(false);
        sVibracion.setClickable(false);

        llcSonido.setOnClickListener(v -> {
            boolean sonidoHabilitado = preferencias.getBoolean("sonido", true);

            // Invierte el estado (activo a inactivo o viceversa)
            sonidoHabilitado = !sonidoHabilitado;
            actualizarSwitchSonido(sonidoHabilitado);
        });

        llcVibracion.setOnClickListener(v -> {
            boolean vibracionHabilitada = preferencias.getBoolean("vibracion", true);

            // Invierte el estado (activo a inactivo o viceversa)
            vibracionHabilitada = !vibracionHabilitada;
            actualizarSwitchVibracion(vibracionHabilitada);
        });

        ivClasico = view.findViewById(R.id.ivClasico);
        ivExtendido = view.findViewById(R.id.ivExtendido);
        ivCorto = view.findViewById(R.id.ivCorto);
        ivPersonalizado = view.findViewById(R.id.ivPersonalizado);

        clClasico = view.findViewById(R.id.clClasico);
        clExtendido = view.findViewById(R.id.clExtendido);
        clCorto = view.findViewById(R.id.clCorto);
        clPersonalizado = view.findViewById(R.id.clPersonalizado);

        clExtendido.setOnClickListener(v -> {
            activarVisibilidad(View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
            guardarTiempos(45, 15);
            guardarOpcionSeleccionada(1);
        });
        clClasico.setOnClickListener(v -> {
            activarVisibilidad(View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
            guardarOpcionSeleccionada(2);
            guardarTiempos(25, 5);
        });
        clCorto.setOnClickListener(v -> {
            activarVisibilidad(View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
            guardarOpcionSeleccionada(3);
            guardarTiempos(10, 2);
        });

        clPersonalizado.setOnClickListener(v -> {
            boolean isPersonalizadoConfigurado = preferencias.getBoolean("isPersonalizadoConfigurado", false);

            if (isPersonalizadoConfigurado) {
                // Si ya se han ingresado datos, recuperamos los datos y activamos la opción
                int tiempoTrabajo = preferencias.getInt("tiempoTrabajoPersonalizado", 0);
                int tiempoDescanso = preferencias.getInt("tiempoDescansoPersonalizado", 0);
                gestorPomodoro.setTiempoTrabajo(tiempoTrabajo);
                gestorPomodoro.setTiempoDescanso(tiempoDescanso);
                guardarOpcionSeleccionada(4);
                activarVisibilidad(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
            } else {
                abrirBottomSheetDialog();
            }
        });

        clPersonalizado.setOnLongClickListener(v -> {
            abrirBottomSheetDialog();
            return true;
        });


        Activity activity = getActivity();
        if (activity != null) {
            int opcionSeleccionada = preferencias.getInt("opcionSeleccionada", 2);
            actualizarVisibilidad(opcionSeleccionada);
        }
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

    // Método para actualizar el texto del objetivo diario
    private void actualizarTextoObjetivoDiario(String texto) {
        tvObjetivoDiarioDescription.setText(texto);
    }

    private void abrirTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(0)
                .setMinute(0)
                .setTitleText("Seleccione el tiempo objetivo")
                .build();

        picker.addOnPositiveButtonClickListener(dialog -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();

            if (hour > 12 || (hour == 12 && minute > 0)) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Error")
                        .setMessage("El tiempo máximo es de 12 horas")
                        .setPositiveButton("Aceptar", null)
                        .show();
                return;
            }

            tiempoSeleccionado = hour + "h " + minute + "min";
            switchObjetivoDiario.setChecked(true); // Activar el switch
            actualizarTextoObjetivoDiario("Objetivo diario establecido: " + tiempoSeleccionado + "\n• Mantener presionado para editar");
        });

        picker.show(getParentFragmentManager(), "MATERIAL_TIME_PICKER");
    }

    private void mostrarDialogoTema(View v) {
        String[] temas = {"Sistema", "Claro", "Oscuro"};

        Context context = getContext();
        if (context != null) {
            // Obtener la preferencia de tema actual
            int temaActual = preferencias.getInt("tema", 0);

            // Crear un nuevo diálogo
            new MaterialAlertDialogBuilder(context)
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
        }
    }

    private void actualizarTema(int temaSeleccionado) {

        gestorPomodoro.setTemaCambiado(true);

        // Guardar la preferencia de tema
        actualizarPreferencias.putInt("tema", temaSeleccionado);
        actualizarPreferencias.apply();

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
    }

    private void guardarOpcionSeleccionada(int opcion) {
        Activity activity = getActivity();
        if (activity != null) {
            // Actualizar la opción seleccionada en el SharedPreferences
            actualizarPreferencias.putInt("opcionSeleccionada", opcion);

            // Actualiza los valores en el ViewModel
            gestorPomodoro.setOpcionSeleccionada(opcion);

            actualizarPreferencias.apply();
        }
    }

    private void guardarTiempos(int tiempoTrabajo, int tiempoDescanso) {
        Activity activity = getActivity();
        if (activity != null) {
            // Actualizar los tiempos de trabajo y descanso en el SharedPreferences
            actualizarPreferencias.putInt("tiempoTrabajo", tiempoTrabajo);
            actualizarPreferencias.putInt("tiempoDescanso", tiempoDescanso);

            // Actualiza los valores en el ViewModel
            gestorPomodoro.setTiempoTrabajo(tiempoTrabajo);
            gestorPomodoro.setTiempoDescanso(tiempoDescanso);
            gestorPomodoro.setConfigurationChanged(true);
            actualizarPreferencias.apply();
        }
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

    private void verificarSonidoVibracion(){
        boolean sonidoHabilitado = preferencias.getBoolean("sonido", true);
        boolean vibracionHabilitada = preferencias.getBoolean("vibracion", true);
        if (sSonido != null) {
            sSonido.setChecked(sonidoHabilitado);
        }
        if (sVibracion != null) {
            sVibracion.setChecked(vibracionHabilitada);
        }
    }

    private void actualizarSwitchSonido(boolean activarSonido) {
        sSonido.setChecked(activarSonido);
        actualizarPreferencias.putBoolean("sonido", activarSonido);
        actualizarPreferencias.apply();
    }

    private void actualizarSwitchVibracion(boolean activarVibracion) {
        sVibracion.setChecked(activarVibracion);
        actualizarPreferencias.putBoolean("vibracion", activarVibracion);
        actualizarPreferencias.apply();
    }

    private void abrirBottomSheetDialog() {
        PersonalizarPomodoroBottomSheet bottomSheet = PersonalizarPomodoroBottomSheet.newInstance();
        boolean isPersonalizadoConfigurado = preferencias.getBoolean("isPersonalizadoConfigurado", false);
        if (isPersonalizadoConfigurado) {
            int tiempoTrabajo = preferencias.getInt("tiempoTrabajoPersonalizado", 0);
            int tiempoDescanso = preferencias.getInt("tiempoDescansoPersonalizado", 0);

            Bundle args = new Bundle();
            args.putInt("tiempoTrabajo", tiempoTrabajo);
            args.putInt("tiempoDescanso", tiempoDescanso);
            bottomSheet.setArguments(args);
        }
        bottomSheet.setOnOptionChangeListener(this);
        bottomSheet.setOnCloseListener(() -> {
            Activity activity = getActivity();
            if (activity != null) {
                // SharedPreferences sharedPreferences = activity.getSharedPreferences("minka", MODE_PRIVATE);
                int ultimaOpcionSeleccionada = preferencias.getInt("opcionSeleccionada", 2);
                simularClick(ultimaOpcionSeleccionada);
            }
        });
        if (!bottomSheet.isAdded() && bottomSheet.getShouldShowBottomSheet()) {
            bottomSheet.show(getParentFragmentManager(), "PersonalizarPomodoroBottomSheet");
        }
    }
}