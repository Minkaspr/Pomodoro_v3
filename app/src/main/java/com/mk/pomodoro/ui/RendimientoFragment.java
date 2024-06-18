package com.mk.pomodoro.ui;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mk.pomodoro.R;
import com.mk.pomodoro.dao.DaoIntervalo;
import com.mk.pomodoro.util.PomodoroAppDB;

import java.util.Locale;

public class RendimientoFragment extends Fragment {

    private TextView tvTiempo;
    private DaoIntervalo daoIntervalo;
    private PomodoroAppDB pomodoroDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pomodoroDB = new PomodoroAppDB(getActivity());
        daoIntervalo = new DaoIntervalo(pomodoroDB.getWritableDatabase());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rendimiento, container, false);
        tvTiempo = view.findViewById(R.id.tvTiempo);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarTiempoTotalTrabajoDelDia();
    }

    private void actualizarTiempoTotalTrabajoDelDia() {
        long tiempoTotalMilisegundos = daoIntervalo.obtenerTiempoTotalTrabajoDelDia();
        String tiempoFormateado = formatearTiempo(tiempoTotalMilisegundos);
        tvTiempo.setText(tiempoFormateado);
    }

    private String formatearTiempo(long milisegundos) {
        int segundos = (int) (milisegundos / 1000) % 60 ;
        int minutos = (int) ((milisegundos / (1000*60)) % 60);
        int horas   = (int) ((milisegundos / (1000*60*60)) % 24);

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos);
    }
}