package com.mk.pomodoro.ui;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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

public class PersonalizarPomodoroBottomSheet extends BottomSheetDialogFragment {

    private TextInputLayout tilTiempoTrabajo, tilTiempoDescanso;
    private TextInputEditText tietTiempoTrabajo, tietTiempoDescanso;
    private Button btnCerrar, btnConfirmar;

    private Boolean isConfirming = false;
    private boolean shouldShowBottomSheet = true;

    public interface OnCloseListener {
        void onClose();
    }
    private OnCloseListener onCloseListener;

    public void setOnCloseListener(OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    public interface OnOptionChangeListener {
        void onOptionChange(int option);
    }

    private OnOptionChangeListener onOptionChangeListener;

    public void setOnOptionChangeListener(OnOptionChangeListener onOptionChangeListener) {
        this.onOptionChangeListener = onOptionChangeListener;
    }

    public static PersonalizarPomodoroBottomSheet newInstance() {
        return new PersonalizarPomodoroBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_personalizar_pomodoro, container, false);

        tilTiempoTrabajo = view.findViewById(R.id.tilWorkingTime);
        tietTiempoTrabajo = view.findViewById(R.id.tietWorkingTime);
        tilTiempoDescanso = view.findViewById(R.id.tilBreakTime);
        tietTiempoDescanso = view.findViewById(R.id.tietBreakTime);
        btnCerrar = view.findViewById(R.id.btnClose);
        btnConfirmar = view.findViewById(R.id.btnConfirm);
        btnCerrar.setOnClickListener(v -> new Handler().postDelayed(this::dismiss, 250));

        if (getArguments() != null) {
            int tiempoTrabajo = getArguments().getInt("tiempoTrabajo");
            int tiempoDescanso = getArguments().getInt("tiempoDescanso");

            tietTiempoTrabajo.setText(String.valueOf(tiempoTrabajo));
            tietTiempoDescanso.setText(String.valueOf(tiempoDescanso));
            tietTiempoTrabajo.setSelection(tietTiempoTrabajo.getText().length());
            tietTiempoDescanso.setSelection(tietTiempoDescanso.getText().length());
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
                Activity activity = getActivity();
                if (activity != null) {
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        btnConfirmar.setOnClickListener(v -> {
            isConfirming = true;
            shouldShowBottomSheet = false;
            // Obtener los tiempos ingresados por el usuario
            String tiempoTrabajoStr = tietTiempoTrabajo.getText() != null ? tietTiempoTrabajo.getText().toString() : "";
            String tiempoDescansoStr = tietTiempoDescanso.getText() != null ? tietTiempoDescanso.getText().toString() : "";
            int tiempoTrabajo = Integer.parseInt(tiempoTrabajoStr);
            int tiempoDescanso = Integer.parseInt(tiempoDescansoStr);

            guardarTiempos(tiempoTrabajo, tiempoDescanso);

            // Actualizar los valores en el ViewModel
            GestorPomodoroViewModel pomodoroType = new ViewModelProvider(requireActivity()).get(GestorPomodoroViewModel.class);
            pomodoroType.setTiempoTrabajo(tiempoTrabajo);
            pomodoroType.setTiempoDescanso(tiempoDescanso);
            pomodoroType.setOpcionSeleccionada(4);
            pomodoroType.setMostrarInfoPersonalizado(true);
            pomodoroType.setConfigurationChanged(true);

            // Recupera la última opción seleccionada de SharedPreferences
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("minka", MODE_PRIVATE);
            boolean snackbarMostrado = sharedPreferences.getBoolean("snackbarMostrado", false);
            if (!snackbarMostrado){
                pomodoroType.setMostrarSnackbar(true);
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity != null) {
            // Comprueba si el BottomSheet se está cerrando confirmando los cambios
            if (isConfirming) {
                // Recupera la última opción seleccionada de SharedPreferences
                SharedPreferences sharedPreferences = activity.getSharedPreferences("minka", MODE_PRIVATE);
                int ultimaOpcionSeleccionada = sharedPreferences.getInt("opcionSeleccionada", 2);
                if (onOptionChangeListener != null) {
                    onOptionChangeListener.onOptionChange(ultimaOpcionSeleccionada);
                }
            }
        }
        onCloseListener.onClose();
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
        Activity activity = getActivity();
        if (activity != null) {
            // Actualizar los tiempos de trabajo y descanso, y también la opcion en el SharedPreferences
            SharedPreferences sharedPreferences = activity.getSharedPreferences("minka", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("tiempoTrabajo", tiempoTrabajo);
            editor.putInt("tiempoDescanso", tiempoDescanso);
            editor.putInt("opcionSeleccionada", 4);
            editor.putInt("tiempoTrabajoPersonalizado", tiempoTrabajo);
            editor.putInt("tiempoDescansoPersonalizado", tiempoDescanso);
            editor.putBoolean("isPersonalizadoConfigurado", true);
            editor.putBoolean("mostrarInfoPersonalizado", true);
            editor.apply();
        }
    }

    public boolean getShouldShowBottomSheet() {
        return shouldShowBottomSheet;
    }
}
