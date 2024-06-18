package com.mk.pomodoro.ui;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mk.pomodoro.R;
import com.mk.pomodoro.ui.adapter.AdaptadorDeVista;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 pagerVista;
    private BottomNavigationView navegacionInferior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        configurarInsets(findViewById(R.id.main));

        pagerVista = findViewById(R.id.pager_vista);
        navegacionInferior = findViewById(R.id.navegacion_inferior);

        pagerVista.setAdapter(new AdaptadorDeVista(this));
        pagerVista.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                navegacionInferior.getMenu().getItem(position).setChecked(true);
            }
        });

        navegacionInferior.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navegacion_inicio) {
                pagerVista.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.navegacion_rendimiento) {
                pagerVista.setCurrentItem(1);
                return true;
            } else if (item.getItemId() == R.id.navegacion_ajustes) {
                pagerVista.setCurrentItem(2);
                return true;
            }
            return false;
        });
    }

    /**
     * Configura los insets de la vista dada para ajustar el padding según las barras del sistema.
     *
     * @param view La vista a la que se aplicarán los insets
     */
    private void configurarInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, navigationBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

}