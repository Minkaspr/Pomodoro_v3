package com.mk.pomodoro.ui;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.mk.pomodoro.R;
import com.mk.pomodoro.controller.Temporizador;
import com.mk.pomodoro.dao.DaoIntervalo;
import com.mk.pomodoro.dao.DaoTipoPomodoro;
import com.mk.pomodoro.model.Intervalo;
import com.mk.pomodoro.model.TipoPomodoro;
import com.mk.pomodoro.ui.viewmodel.GestorPomodoroViewModel;
import com.mk.pomodoro.util.PomodoroAppDB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InicioFragment extends Fragment {

    private TabLayout layoutPestanas;
    private AppCompatTextView tiempo;
    private CircularProgressIndicator barraProgresoCircular;
    private MaterialButton botonDetener, botonIniciar, botonPausar, botonContinuar, botonDetenerAlarma;

    private boolean iniciarTemporizador = false;
    private int tiempoTrabajo;
    private int tiempoDescanso;

    private boolean intervaloActivado = false;
    private long fechaInicio;
    private long fechaFin;
    private long tiempoMuertoAcumulado = 0;
    private long ultimoTiempoPausa = 0;

    private Temporizador temporizador;
    private SharedPreferences preferencias;
    private SharedPreferences.Editor actualizarPreferencias;
    private GestorPomodoroViewModel gestorPomodoro;
    private NotificationManagerCompat gestorNotificaciones;
    private MediaPlayer reproductorAlarma;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_inicio, container, false);

        layoutPestanas = vista.findViewById(R.id.tlOpcionesTiempo);
        tiempo = vista.findViewById(R.id.tvTiempo);
        barraProgresoCircular = vista.findViewById(R.id.pbCirculo);
        botonDetener = vista.findViewById(R.id.btnDetener);
        botonIniciar = vista.findViewById(R.id.btnReproducir);
        botonDetenerAlarma = vista.findViewById(R.id.btnDetenerAlarma);
        botonPausar = vista.findViewById(R.id.btnPausar);
        botonContinuar = vista.findViewById(R.id.btnContinuar);
        reproductorAlarma = MediaPlayer.create(getActivity(), R.raw.kalimba);

        preferencias = requireActivity().getSharedPreferences("minka", MODE_PRIVATE);
        actualizarPreferencias = preferencias.edit();
        gestorPomodoro = new ViewModelProvider(requireActivity()).get(GestorPomodoroViewModel.class);
        gestorNotificaciones = NotificationManagerCompat.from(requireActivity());

        gestorPomodoro.getTemaCambiado().observe(getViewLifecycleOwner(), cambiado -> {
            if (cambiado) {
                reiniciarTemporizadorYActualizarBotones();
                gestorPomodoro.setTemaCambiado(false);
            }
        });
        gestorPomodoro.setEstadoTemporizador("Trabajo");

        tiempoTrabajo = preferencias.getInt("tiempoTrabajo", 25 );
        tiempoDescanso = preferencias.getInt("tiempoDescanso", 5 );

        layoutPestanas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Actualiza los valores en el ViewModel
                int position = tab.getPosition();
                iniciarTemporizador = false;
                if (position == 0) {
                    prepararTemporizador(tiempoTrabajo * 60);
                    gestorPomodoro.setEstadoTemporizador("Trabajo");
                    actualizarPreferencias.putInt("tab_position", position);
                    actualizarPreferencias.apply();
                } else if (position == 1) {
                    prepararTemporizador(tiempoDescanso * 60);
                    gestorPomodoro.setEstadoTemporizador("Descanso");
                    actualizarPreferencias.putInt("tab_position", position);
                    actualizarPreferencias.apply();
                }
                gestorPomodoro.setTemporizadorTerminado(false);
                botonIniciar.setVisibility(View.VISIBLE);
                botonPausar.setVisibility(View.GONE);
                botonContinuar.setVisibility(View.GONE);
                botonDetener.setVisibility(View.GONE);

                // Detiene el sonido de la alarma si está sonando.
                if (reproductorAlarma.isPlaying()) {
                    reproductorAlarma.pause();
                    reproductorAlarma.seekTo(0);
                }
                // Detener la vibración si está activa.
                Vibrator vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator.hasVibrator()) { // Verifica si el dispositivo tiene vibrador.
                    vibrator.cancel();
                }
                // Oculta el botón para detener la alarma si está visible.
                if (botonDetenerAlarma.getVisibility() == View.VISIBLE) {
                    botonDetenerAlarma.setVisibility(View.GONE);
                }
                // Cancelar la notificación
                gestorNotificaciones.cancel(1);
                iniciarTemporizador = false;
                if (intervaloActivado) {
                    fechaFin = System.currentTimeMillis(); // Guardar la hora actual como fin
                    long duracionTotal = fechaFin - fechaInicio; // Calcular la duración total del intervalo
                    long duracionActiva = duracionTotal - tiempoMuertoAcumulado; // Restar el tiempo muerto para obtener la duración activa
                    intervaloActivado = false;
                    guardarIntervaloEnBD(fechaInicio, fechaFin, duracionActiva);
                    ultimoTiempoPausa = 0;
                    tiempoMuertoAcumulado = 0;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Recupera la posición de la pestaña desde SharedPreferences
        int position = preferencias.getInt("tab_position", 0);

        // Selecciona la pestaña correspondiente
        TabLayout.Tab tab = layoutPestanas.getTabAt(position);
        if (tab != null) {
            tab.select();
        }

        prepararTemporizador((position == 0 ? tiempoTrabajo : tiempoDescanso) * 60);

        botonDetener.setOnClickListener(v -> {
            reiniciarTemporizadorYActualizarBotones();
            gestorNotificaciones.cancel(1);
            iniciarTemporizador = false;
            gestorPomodoro.setTemporizadorIniciado(false);
            if (intervaloActivado) {
                fechaFin = System.currentTimeMillis(); // Guardar la hora actual como fin
                long duracionTotal = fechaFin - fechaInicio; // Calcular la duración total del intervalo
                long duracionActiva = duracionTotal - tiempoMuertoAcumulado; // Restar el tiempo muerto para obtener la duración activa
                intervaloActivado = false;
                guardarIntervaloEnBD(fechaInicio, fechaFin, duracionActiva);
                ultimoTiempoPausa = 0;
                tiempoMuertoAcumulado = 0;
            }
        });
        botonIniciar.setOnClickListener(v -> {
            iniciarTemporizador = true;
            temporizador.iniciarTemporizador();
            botonIniciar.setVisibility(View.GONE);
            botonPausar.setVisibility(View.VISIBLE);
            botonDetener.setVisibility(View.VISIBLE);
            gestorPomodoro.setTemporizadorIniciado(true);

            if (!intervaloActivado) {
                fechaInicio = System.currentTimeMillis(); // Guardar la hora actual como inicio
                intervaloActivado = true;
                tiempoMuertoAcumulado = 0;
            }
        });
        botonPausar.setOnClickListener(v -> {
            temporizador.pausarTemporizador();
            botonPausar.setVisibility(View.GONE);
            botonContinuar.setVisibility(View.VISIBLE);
            ultimoTiempoPausa = System.currentTimeMillis(); // Guardar la hora actual como tiempo de pausa
        });
        botonContinuar.setOnClickListener(v -> {
            temporizador.reanudarTemporizador();
            botonContinuar.setVisibility(View.GONE);
            botonPausar.setVisibility(View.VISIBLE);
            long tiempoReanudacion = System.currentTimeMillis(); // Guardar la hora actual como tiempo de reanudación
            tiempoMuertoAcumulado += tiempoReanudacion - ultimoTiempoPausa; // Sumar al acumulador de tiempo muerto
        });
        botonDetenerAlarma.setOnClickListener(v -> {
            // Detiene el sonido de la alarma.
            if (reproductorAlarma.isPlaying()) {
                reproductorAlarma.pause();
                reproductorAlarma.seekTo(0);
            }
            // Desactiva la repetición del MediaPlayer.
            reproductorAlarma.setLooping(false);
            // Detener la vibración
            Vibrator vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.cancel();

            // Oculta el botón para detener la alarma.
            botonDetenerAlarma.setVisibility(View.GONE);
            temporizador.reiniciarTemporizador(barraProgresoCircular, tiempo);
            botonIniciar.setVisibility(View.VISIBLE);
            // Cancelar la notificación
            gestorNotificaciones.cancel(1);
            iniciarTemporizador = false;

            if (intervaloActivado) {
                fechaFin = System.currentTimeMillis(); // Guardar la hora actual como fin
                long duracionTotal = fechaFin - fechaInicio; // Calcular la duración total del intervalo
                long duracionActiva = duracionTotal - tiempoMuertoAcumulado; // Restar el tiempo muerto para obtener la duración activa
                intervaloActivado = false;
                guardarIntervaloEnBD(fechaInicio, fechaFin, duracionActiva);
                ultimoTiempoPausa = 0;
                tiempoMuertoAcumulado = 0;
            }
        });

        return vista;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Observa los cambios en ViewModel
        gestorPomodoro.isConfigurationChanged().observe(getViewLifecycleOwner(), configurationChanged -> {
            if (configurationChanged) {
                gestorPomodoro.getTiempoTrabajo().observe(getViewLifecycleOwner(), nuevoTiempoTrabajo -> {
                    // Actualiza tu variable tiempoTrabajo con el nuevo tiempo de trabajo
                    tiempoTrabajo = nuevoTiempoTrabajo;
                    // Comprueba si la opción de trabajo está seleccionada en tu TabLayout
                    if (layoutPestanas.getSelectedTabPosition() == 0) {
                        // Si la opción de trabajo está seleccionada, actualiza el temporizador
                        prepararTemporizador(tiempoTrabajo * 60);
                        reiniciarTemporizadorYActualizarBotones();
                    }
                });

                gestorPomodoro.getTiempoDescanso().observe(getViewLifecycleOwner(), nuevoTiempoDescanso -> {
                    // Actualiza tu variable tiempoDescanso con el nuevo tiempo de descanso
                    tiempoDescanso = nuevoTiempoDescanso;
                    // Comprueba si la opción de descanso está seleccionada en tu TabLayout
                    if (layoutPestanas.getSelectedTabPosition() == 1) {
                        // Si la opción de descanso está seleccionada, actualiza el temporizador
                        prepararTemporizador(tiempoDescanso * 60);
                        reiniciarTemporizadorYActualizarBotones();
                    }
                });
                gestorPomodoro.setConfigurationChanged(false);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reproductorAlarma != null) {
            reproductorAlarma.release();
            reproductorAlarma = null;
        }
        if (intervaloActivado) {
            fechaFin = System.currentTimeMillis(); // Guardar la hora actual como fin
            long duracionTotal = fechaFin - fechaInicio; // Calcular la duración total del intervalo
            long duracionActiva = duracionTotal - tiempoMuertoAcumulado; // Restar el tiempo muerto para obtener la duración activa
            intervaloActivado = false;
            guardarIntervaloEnBD(fechaInicio, fechaFin, duracionActiva);
            ultimoTiempoPausa = 0;
            tiempoMuertoAcumulado = 0;
        }
    }

    private void prepararTemporizador(int segundos) {
        if (temporizador != null) {
            temporizador.destruirTemporizador();
        }
        barraProgresoCircular.setMax(segundos);
        temporizador = new Temporizador(segundos * 1000L, 1000);
        temporizador.setEscuchadorTick(millisUntilFinished -> {
            int segundosRestantes = (int) (millisUntilFinished / 1000f);
            tiempo.setText(String.format(Locale.getDefault(), "%02d:%02d", segundosRestantes / 60, segundosRestantes % 60));
            barraProgresoCircular.setProgress(segundosRestantes);
            System.out.println(segundosRestantes);
            iniciarTemporizador = true;
        });

        temporizador.setEscuchadorFinalizacion(() -> {
            tiempo.setText(getString(R.string.frag_ini_tiempo_predeterminado));
            Activity activity = getActivity();
            if (activity != null) {
                // Leer la preferencia del usuario para el sonido y vibración
                preferencias = getActivity().getSharedPreferences("minka", MODE_PRIVATE);
                boolean sonidoHabilitado = preferencias.getBoolean("sonido", true);
                boolean vibracionHabilitada = preferencias.getBoolean("vibracion", true);
                if (sonidoHabilitado) {
                    // Reproduce el sonido de la alarma.
                    reproductorAlarma.start();
                    // Configura el MediaPlayer para que se repita.
                    reproductorAlarma.setLooping(true);
                }
                if (vibracionHabilitada) {
                    // Activa la vibración en bucle con un patrón de 1 segundo de vibración y 1 segundo de pausa
                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        long[] pattern = {0, 1000, 1000}; // Patrón: 0 ms de espera, 1000 ms de vibración, 1000 ms de pausa
                        vibrator.vibrate(pattern, 0); // El segundo parámetro indica en qué índice del patrón comenzar (0 para repetir desde el principio)
                    }
                }

                // Actualiza los valores en el ViewModel
                gestorPomodoro.setTemporizadorTerminado(true);
                gestorPomodoro.setTemporizadorIniciado(false);
                // Enviar un broadcast para indicar que el temporizador ha terminado
                Intent intent = new Intent("com.mk.pomodoro.TEMPORIZADOR_TERMINADO");
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }

            // Oculta los otros botones.
            botonIniciar.setVisibility(View.GONE);
            botonPausar.setVisibility(View.GONE);
            botonContinuar.setVisibility(View.GONE);
            botonDetener.setVisibility(View.GONE);
            // Muestra el botón para detener la alarma.
            botonDetenerAlarma.setVisibility(View.VISIBLE);
        });

        // Mostramos el tiempo inicial sin iniciar el temporizador
        int segundosRestantes = segundos;
        tiempo.setText(String.format(Locale.getDefault(), "%02d:%02d", segundosRestantes / 60, segundosRestantes % 60));
        barraProgresoCircular.setProgress(segundosRestantes);
        // Iniciamos el temporizador solo si iniciarTemporizador es verdadero
        if (iniciarTemporizador) {
            temporizador.iniciarTemporizador();
        }
    }

    private void reiniciarTemporizadorYActualizarBotones() {
        temporizador.reiniciarTemporizador(barraProgresoCircular, tiempo);
        botonIniciar.setVisibility(View.VISIBLE);
        botonPausar.setVisibility(View.GONE);
        botonContinuar.setVisibility(View.GONE);
        botonDetener.setVisibility(View.GONE);
    }

    private void guardarIntervaloEnBD(long inicio, long fin, long duracionActiva) {
        // Obtener la instancia de SQLiteDatabase
        PomodoroAppDB dbHelper = new PomodoroAppDB(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int opcionSeleccionada = preferencias.getInt("opcionSeleccionada", 2);
        int position = preferencias.getInt("tab_position", 0);
        boolean esTrabajo = position == 0;

        // Crear un objeto Intervalo con las fechas de inicio y fin
        Intervalo intervalo = new Intervalo();
        intervalo.setTipoId(obtenerIdTipoPomodoro(opcionSeleccionada,db));
        intervalo.setEsTrabajo(esTrabajo);
        intervalo.setFechaInicio(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date(inicio)));
        intervalo.setFechaFin(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date(fin)));
        intervalo.setDuracionTotal((int) (duracionActiva));

        DaoIntervalo daoIntervalo = new DaoIntervalo(db);
        daoIntervalo.insertarIntervalo(intervalo);

        db.close();
    }

    private int obtenerIdTipoPomodoro(int opcionSeleccionada, SQLiteDatabase db) {
        String nombreTipo = "";

        switch (opcionSeleccionada) {
            case 1:
                nombreTipo = "Extendido";
                break;
            case 2:
                nombreTipo = "Clásico";
                break;
            case 3:
                nombreTipo = "Corto";
                break;
            case 4:
                nombreTipo = "Personalizado";
                tiempoTrabajo = preferencias.getInt("tiempoTrabajo", 25);
                tiempoDescanso = preferencias.getInt("tiempoDescanso", 5);

                // Crear un nuevo tipo de Pomodoro personalizado y obtener su ID
                TipoPomodoro tipoPersonalizado = new TipoPomodoro();
                tipoPersonalizado.setNombre(nombreTipo);
                tipoPersonalizado.setTiempoTrabajoEstablecido(tiempoTrabajo);
                tipoPersonalizado.setTiempoDescansoEstablecido(tiempoDescanso);

                DaoTipoPomodoro daoTipoPomodoro = new DaoTipoPomodoro(db);
                return (int) daoTipoPomodoro.crearNuevoTipoPomodoro(tipoPersonalizado);
        }

        // Consultar en la base de datos para obtener el ID basado en el nombre
        DaoTipoPomodoro daoTipoPomodoro = new DaoTipoPomodoro(db);
        return daoTipoPomodoro.obtenerIdPorNombre(nombreTipo);
    }
}