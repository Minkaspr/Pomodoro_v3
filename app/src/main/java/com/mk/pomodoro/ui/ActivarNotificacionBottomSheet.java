package com.mk.pomodoro.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.mk.pomodoro.R;

public class ActivarNotificacionBottomSheet extends BottomSheetDialogFragment {

    public static ActivarNotificacionBottomSheet newInstance() {
        return new ActivarNotificacionBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_activar_notificacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);
        MaterialButton btnContinuar = vista.findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(v -> {
            /* Informacion de la aplicacion
            Intent rintent = new Intent();
            rintent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package",  requireActivity().getPackageName(), null);
            rintent.setData(uri);
            startActivity(rintent);*/

            // Notificaciones de aplicacion
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().getPackageName());
            startActivity(intent);
            dismiss();
        });
    }
}
