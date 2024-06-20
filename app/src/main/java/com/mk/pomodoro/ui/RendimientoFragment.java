package com.mk.pomodoro.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.mk.pomodoro.R;
import com.mk.pomodoro.dao.DaoIntervalo;
import com.mk.pomodoro.ui.adapter.GestorFiltrosAdapter;
import com.mk.pomodoro.util.PomodoroAppDB;

import java.util.Locale;

public class RendimientoFragment extends Fragment {

    private DaoIntervalo daoIntervalo;
    private PomodoroAppDB pomodoroDB;
    private Chip cHoy, cAyer, cSemana, cMes;
    private ViewPager2 vpFiltros;
    private TextView tvTiempo;

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
        //tvTiempo = view.findViewById(R.id.tvTiempo);
        // Inicializa los chips
        cHoy = view.findViewById(R.id.cHoy);
        cAyer = view.findViewById(R.id.cAyer);
        cSemana = view.findViewById(R.id.cSemana);
        cMes = view.findViewById(R.id.cMes);
        vpFiltros = view.findViewById(R.id.pager_filtro_vista);
        GestorFiltrosAdapter adapter = new GestorFiltrosAdapter(getChildFragmentManager(), getLifecycle());
        vpFiltros.setAdapter(adapter);

        // Asigna OnClickListener a los chips
        cHoy.setOnClickListener(chipClickListener);
        cAyer.setOnClickListener(chipClickListener);
        cSemana.setOnClickListener(chipClickListener);
        cMes.setOnClickListener(chipClickListener);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //actualizarTiempoTotalTrabajoDelDia();
    }

    private final View.OnClickListener chipClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Deselecciona todos los chips
            cHoy.setChecked(false);
            cAyer.setChecked(false);
            cSemana.setChecked(false);
            cMes.setChecked(false);

            // Establece el chip actual como seleccionado
            ((Chip) v).setChecked(true);

            // Cambia al fragmento correspondiente en el ViewPager
            if (v == cHoy) {
                vpFiltros.setCurrentItem(0, false); // FiltroHoyFragment
            } else if (v == cAyer) {
                vpFiltros.setCurrentItem(1, false); // FiltroAyerFragment
            } else if (v == cSemana) {
                vpFiltros.setCurrentItem(2, false); // FiltroSemanaFragment
            } else if (v == cMes) {
                vpFiltros.setCurrentItem(3, false); // FiltroMesFragment
            }
        }
    };

    private void actualizarTiempoTotalTrabajoDelDia() {
        long tiempoTotalMilisegundos = daoIntervalo.obtenerTiempoTotalTrabajoHoy();
        String tiempoFormateado = formatearTiempo(tiempoTotalMilisegundos);
        tvTiempo.setText(tiempoFormateado);
    }

    private String formatearTiempo(long milisegundos) {
        int horas   = (int) ((milisegundos / (1000*60*60)) % 24);
        int minutos = (int) ((milisegundos / (1000*60)) % 60);
        int segundos = (int) (milisegundos / 1000) % 60 ;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos);
    }
}