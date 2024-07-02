package com.mk.pomodoro.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.mk.pomodoro.R;
import com.mk.pomodoro.dao.DaoIntervalo;
import com.mk.pomodoro.dao.DaoSesion;
import com.mk.pomodoro.dao.impl.DaoIntervaloImpl;
import com.mk.pomodoro.dao.impl.DaoSesionImpl;
import com.mk.pomodoro.dto.SesionDTO;
import com.mk.pomodoro.ui.adapter.SesionDTOAdapter;
import com.mk.pomodoro.ui.viewmodel.GestorPomodoroViewModel;
import com.mk.pomodoro.util.ConstantesAppConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FiltroHoyFragment extends Fragment {

    private AppCompatTextView tvProgreso, tvProductividadRealizado, tvObjetivoTrabajo, tvObjetivoRealizado, tvMensajeObjetivo, tvDescansoRealizado, tvSesionesRealizados;
    private CircularProgressIndicator pciAnvaceRealizado;
    private MaterialCardView mcdContenedorObjetivoDiario, mcdContenedorProductividadDiaria;
    private RecyclerView rvSesionesHoy;

    private DaoIntervalo daoIntervalo;
    private DaoSesion daoSesion;
    private SharedPreferences preferencias;
    private GestorPomodoroViewModel gestorPomodoro;

    private MaterialDividerItemDecoration divider;
    private String fechaHoy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        daoIntervalo = new DaoIntervaloImpl(getContext());
        daoSesion = new DaoSesionImpl(getContext());
        preferencias = requireActivity().getSharedPreferences(ConstantesAppConfig.NOM_ARCHIVO_PREFERENCIAS, MODE_PRIVATE);
        gestorPomodoro = new ViewModelProvider(requireActivity()).get(GestorPomodoroViewModel.class);

        fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filtro_hoy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);
        mcdContenedorObjetivoDiario = vista.findViewById(R.id.mcdContenedorObjetivoDiario);
        mcdContenedorProductividadDiaria = vista.findViewById(R.id.mcdContenedorProductividadDiaria);
        rvSesionesHoy = vista.findViewById(R.id.rvSesionesHoy);
        rvSesionesHoy.setLayoutManager(new LinearLayoutManager(getContext()));
        divider = new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL);
        tvProgreso = vista.findViewById(R.id.tvProgreso);
        tvProductividadRealizado = vista.findViewById(R.id.tvProductividadRealizado);
        tvObjetivoTrabajo = vista.findViewById(R.id.tvObjetivoTrabajo);
        tvObjetivoRealizado = vista.findViewById(R.id.tvObjetivoRealizado);
        tvMensajeObjetivo = vista.findViewById(R.id.tvMensajeObjetivo);
        tvDescansoRealizado = vista.findViewById(R.id.tvDescansoRealizado);
        tvSesionesRealizados = vista.findViewById(R.id.tvSesionesRealizados);
        pciAnvaceRealizado = vista.findViewById(R.id.pciAnvaceRealizado);

        tarjetaTiempoTrabajoHoy();
        tarjetaDescansoYSesion();
        misSesiones();

        gestorPomodoro.getObjetivoCambiado().observe(getViewLifecycleOwner(), cambiado -> {
            if (cambiado) {
                tarjetaTiempoTrabajoHoy();
                gestorPomodoro.setObjetivoCambiado(false);
            }
        });

        gestorPomodoro.getDatosTemporizadorActualizados().observe(getViewLifecycleOwner(), datosActualizados -> {
            if (datosActualizados) {
                tarjetaTiempoTrabajoHoy();
                tarjetaDescansoYSesion();
                misSesiones();
                gestorPomodoro.setDatosTemporizadorActualizados(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void tarjetaTiempoTrabajoHoy() {
        int tiempoTotalMilisegundos = daoIntervalo.obtenerTiempoTotalPorFecha(true,fechaHoy);

        boolean objetivoActivado = preferencias.getBoolean(ConstantesAppConfig.C_OBJETIVO,ConstantesAppConfig.V_OBJETIVO_B);
        if(objetivoActivado){
            mcdContenedorObjetivoDiario.setVisibility(View.VISIBLE);
            mcdContenedorProductividadDiaria.setVisibility(View.GONE);

            int tiempoObjetivo = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_OBJETIVO, ConstantesAppConfig.V_TIEMPO_OBJETIVO_I);
            tvObjetivoTrabajo.setText(getString(R.string.frag_ren_hoy_card_1_cont_text_1,formatearTiempo(tiempoObjetivo)));
            tvObjetivoRealizado.setText(getString(R.string.frag_ren_hoy_card_1_cont_text_2,formatearTiempo(tiempoTotalMilisegundos)));

            int progreso = 0;
            if(tiempoObjetivo!=0){
                pciAnvaceRealizado.setMax(tiempoObjetivo);
                progreso = ((tiempoTotalMilisegundos * 100) / tiempoObjetivo);
                tvMensajeObjetivo.setText(obtenerMensajeSegunProgreso(progreso));
                pciAnvaceRealizado.setProgress(tiempoTotalMilisegundos);
                progreso = Math.min(progreso, 100);
            }
            tvProgreso.setText(getString(R.string.frag_ren_hoy_card_1_porc_avance,progreso +" %"));
        } else {
            mcdContenedorObjetivoDiario.setVisibility(View.GONE);
            mcdContenedorProductividadDiaria.setVisibility(View.VISIBLE);
            tvProductividadRealizado.setText(getString(R.string.frag_ren_hoy_card_2_cont_text_2,formatearTiempo(tiempoTotalMilisegundos)));
        }
    }

    public void tarjetaDescansoYSesion() {
        int tiempoTotalMilisegundos = daoIntervalo.obtenerTiempoTotalPorFecha(false,fechaHoy);
        int cantidadSesiones = daoSesion.obtenerCantidadSesiones(fechaHoy);
        tvDescansoRealizado.setText(getString(R.string.frag_ren_hoy_card_3_cont_text_1,formatearTiempo(tiempoTotalMilisegundos)));
        tvSesionesRealizados.setText(getString(R.string.frag_ren_hoy_card_4_cont_text_1, cantidadSesiones));
    }

    public void misSesiones(){
        List<SesionDTO> sesionesDeHoy = daoSesion.obtenerSesiones(fechaHoy);
        SesionDTOAdapter sesionDTOAdapter = new SesionDTOAdapter(getContext(), sesionesDeHoy);
        divider.setLastItemDecorated(false);
        rvSesionesHoy.addItemDecoration(divider);
        rvSesionesHoy.setAdapter(sesionDTOAdapter);
    }

    private String formatearTiempo(long milisegundos) {
        int horas   = (int) ((milisegundos / (1000*60*60)) % 24);
        int minutos = (int) ((milisegundos / (1000*60)) % 60);
        int segundos = (int) (milisegundos / 1000) % 60 ;

        //return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos);
        if (horas > 0) {
            if (minutos > 0) {
                return String.format(Locale.getDefault(), "%dh y %dmin", horas, minutos);
            } else {
                return String.format(Locale.getDefault(), (horas == 1) ? "%d hora" : "%d horas", horas);
            }
        } else if (minutos > 0) {
            if (segundos > 0){
                return String.format(Locale.getDefault(), "%dmin y %02ds", minutos, segundos);
            } else {
                return String.format(Locale.getDefault(), (minutos == 1) ? "%d minuto" : "%d minutos", minutos);
            }
        } else {
            return String.format(Locale.getDefault(), "%d segundos", segundos);
        }
    }

    private String obtenerMensajeSegunProgreso(int progreso) {
        Random random = new Random();
        String[] mensajes;

        if (progreso >= 0 && progreso < 10) {
            mensajes = new String[]{"¡Comienza con fuerza!", "Cada paso cuenta."};
        } else if (progreso >= 10 && progreso < 25) {
            mensajes = new String[]{"¡Vas en camino!", "¡Sigue así!"};
        } else if (progreso >= 25 && progreso < 48) {
            mensajes = new String[]{"¡Estás avanzando!", "¡Casi llegas a la mitad!"};
        } else if (progreso >= 48 && progreso <= 52) {
            mensajes = new String[]{"¡Mitad del camino!", "¡Estás en sintonía!"};
        } else if (progreso > 52 && progreso < 75) {
            mensajes = new String[]{"¡Casi allí!", "¡Sigue esforzándote!"};
        } else if (progreso >= 75 && progreso < 90) {
            mensajes = new String[]{"¡Gran progreso!", "¡Estás cerca!"};
        } else if (progreso >= 90 && progreso < 99) {
            mensajes = new String[]{"¡Último esfuerzo!", "¡Casi lo logras!"};
        } else {
            mensajes = new String[]{"¡Objetivo alcanzado!", "¡Excelente trabajo!"};
        }

        // Selecciona un mensaje aleatorio
        int indiceAleatorio = random.nextInt(mensajes.length);
        return mensajes[indiceAleatorio];
    }
}