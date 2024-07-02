package com.mk.pomodoro.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;
import com.mk.pomodoro.R;
import com.mk.pomodoro.ui.adapter.GestorFiltrosAdapter;

public class RendimientoFragment extends Fragment {

    private Chip cHoy, cAyer, cSemana, cMes;
    private ViewPager2 vpFiltros;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rendimiento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);

        cHoy = vista.findViewById(R.id.cHoy);
        cAyer = vista.findViewById(R.id.cAyer);
        cSemana = vista.findViewById(R.id.cSemana);
        cMes = vista.findViewById(R.id.cMes);
        vpFiltros = vista.findViewById(R.id.pager_filtro_vista);
        GestorFiltrosAdapter adapter = new GestorFiltrosAdapter(getChildFragmentManager(), getLifecycle());
        vpFiltros.setAdapter(adapter);

        cHoy.setOnClickListener(chipClickListener);
        cAyer.setOnClickListener(chipClickListener);
        cSemana.setOnClickListener(chipClickListener);
        cMes.setOnClickListener(chipClickListener);

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
}