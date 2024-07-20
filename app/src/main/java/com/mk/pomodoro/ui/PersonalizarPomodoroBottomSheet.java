package com.mk.pomodoro.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mk.pomodoro.R;
import com.mk.pomodoro.ui.viewmodel.GestorPomodoroViewModel;
import com.mk.pomodoro.util.ConstantesAppConfig;

import java.util.Objects;

public class PersonalizarPomodoroBottomSheet extends BottomSheetDialogFragment {

    private TextInputLayout tilTiempoTrabajo, tilTiempoDescanso;
    private TextInputEditText tietTiempoTrabajo, tietTiempoDescanso;
    private Button btnConfirmar;

    private boolean mostrarBottomSheet = true;

    private SharedPreferences preferencias;
    private SharedPreferences.Editor actualizarPreferencias;
    private GestorPomodoroViewModel gestorPomodoro;

    public static PersonalizarPomodoroBottomSheet newInstance() {
        return new PersonalizarPomodoroBottomSheet();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencias = requireActivity().getSharedPreferences(ConstantesAppConfig.NOM_ARCHIVO_PREFERENCIAS, MODE_PRIVATE);
        actualizarPreferencias = preferencias.edit();
        gestorPomodoro = new ViewModelProvider(requireActivity()).get(GestorPomodoroViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_personalizar_pomodoro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);

        tilTiempoTrabajo = vista.findViewById(R.id.tilWorkingTime);
        tietTiempoTrabajo = vista.findViewById(R.id.tietWorkingTime);
        tilTiempoDescanso = vista.findViewById(R.id.tilBreakTime);
        tietTiempoDescanso = vista.findViewById(R.id.tietBreakTime);
        Button btnCerrar = vista.findViewById(R.id.btnClose);
        btnCerrar.setOnClickListener(v -> new Handler().postDelayed(this::dismiss, 250));
        btnConfirmar = vista.findViewById(R.id.btnConfirm);

        if (getArguments() != null) {
            int tiempoTrabajo = getArguments().getInt("tiempoTrabajo");
            int tiempoDescanso = getArguments().getInt("tiempoDescanso");

            tietTiempoTrabajo.setText(String.valueOf(tiempoTrabajo));
            tietTiempoDescanso.setText(String.valueOf(tiempoDescanso));
            tietTiempoTrabajo.setSelection(Objects.requireNonNull(tietTiempoTrabajo.getText()).length());
            tietTiempoDescanso.setSelection(Objects.requireNonNull(tietTiempoDescanso.getText()).length());
        }

        tietTiempoTrabajo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence userInput, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence userInput, int start, int before, int count) {
                validarEntrada(userInput, 5, 90, tilTiempoTrabajo);
            }
            @Override
            public void afterTextChanged(Editable userInput) {}
        });

        tietTiempoTrabajo.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                tietTiempoDescanso.requestFocus();
                return true;
            }
            return false;
        });

        tietTiempoDescanso.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence userInput, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence userInput, int start, int before, int count) {
                validarEntrada(userInput, 1, 30, tilTiempoDescanso);
            }
            @Override
            public void afterTextChanged(Editable userInput) {}
        });

        tietTiempoDescanso.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                tietTiempoDescanso.clearFocus();
                btnConfirmar.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        btnConfirmar.setOnClickListener(v -> {
            mostrarBottomSheet = false;
            // Obtener los tiempos ingresados por el usuario
            String tiempoTrabajoStr = tietTiempoTrabajo.getText() != null ? tietTiempoTrabajo.getText().toString() : "";
            String tiempoDescansoStr = tietTiempoDescanso.getText() != null ? tietTiempoDescanso.getText().toString() : "";
            int tiempoTrabajo = Integer.parseInt(tiempoTrabajoStr);
            int tiempoDescanso = Integer.parseInt(tiempoDescansoStr);

            guardarTiempos(tiempoTrabajo, tiempoDescanso);

            // Actualizar los valores en el ViewModel
            gestorPomodoro.setMostrarInfoPersonalizado(true);
            gestorPomodoro.setTipoPomodoroCambiado(true);
            gestorPomodoro.setTiemposActualizados(true);
            gestorPomodoro.setTiempoTrabajo(tiempoTrabajo);
            gestorPomodoro.setTiempoDescanso(tiempoDescanso);
            gestorPomodoro.setOpcionSeleccionada(4);
            // Recupera la última opción seleccionada de SharedPreferences
            boolean estaSnackbarActivado = preferencias.getBoolean(ConstantesAppConfig.C_SNACKBAR_PERSONALIZADO, ConstantesAppConfig.V_SNACKBAR_PERSONALIZADO_B);
            if (!estaSnackbarActivado){
                gestorPomodoro.setMostrarSnackbar(true);
            }
            dismiss();
        });
    }

    private void validarEntrada(CharSequence entradaUsuario , int minimo, int maximo, TextInputLayout til) {
        String input = entradaUsuario != null ? entradaUsuario.toString() : "";
        if (!input.isEmpty()) {
            int numero = Integer.parseInt(input);
            if (numero < minimo || numero > maximo) {
                til.setError("Ingresa un número entre " + minimo + " y " + maximo);
            } else {
                til.setError(null);
                String tiempoTrabajo = tietTiempoTrabajo.getText() != null ? tietTiempoTrabajo.getText().toString() : "";
                String tiempoDescanso = tietTiempoDescanso.getText() != null ? tietTiempoDescanso.getText().toString() : "";
                if (tilTiempoTrabajo.getError() == null && tilTiempoDescanso.getError() == null
                        && !tiempoTrabajo.isEmpty()
                        && !tiempoDescanso.isEmpty()) {
                    btnConfirmar.setEnabled(true);
                    return;
                }
            }
        } else {
            til.setError("No puede estar vacío");
        }
        btnConfirmar.setEnabled(false);
    }

    private void guardarTiempos(int tiempoTrabajo, int tiempoDescanso) {
        actualizarPreferencias.putInt(ConstantesAppConfig.C_TIEMPO_TRABAJO, tiempoTrabajo);
        actualizarPreferencias.putInt(ConstantesAppConfig.C_TIEMPO_DESCANSO, tiempoDescanso);
        actualizarPreferencias.putInt(ConstantesAppConfig.C_OPCION_SELECCIONADA, 4);
        actualizarPreferencias.putInt(ConstantesAppConfig.C_TIEMPO_TRABAJO_PERSONALIZADO, tiempoTrabajo);
        actualizarPreferencias.putInt(ConstantesAppConfig.C_TIEMPO_DESCANSO_PERSONALIZADO, tiempoDescanso);
        actualizarPreferencias.putBoolean(ConstantesAppConfig.C_PERSONALIZADO_ACTIVADO, true);
        actualizarPreferencias.putBoolean(ConstantesAppConfig.C_INFO_PERSONALIZADO, true);
        actualizarPreferencias.apply();
    }

    public boolean getMostrarBottomSheet() {
        return mostrarBottomSheet;
    }
}
