package com.mk.pomodoro.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.mk.pomodoro.R;
import com.mk.pomodoro.controller.Temporizador;
import com.mk.pomodoro.dao.DaoIntervalo;
import com.mk.pomodoro.dao.DaoSesion;
import com.mk.pomodoro.dao.DaoTipoPomodoro;
import com.mk.pomodoro.dao.impl.DaoIntervaloImpl;
import com.mk.pomodoro.dao.impl.DaoSesionImpl;
import com.mk.pomodoro.dao.impl.DaoTipoPomodoroImpl;
import com.mk.pomodoro.model.Intervalo;
import com.mk.pomodoro.model.SesionDTO;
import com.mk.pomodoro.model.TipoPomodoro;
import com.mk.pomodoro.ui.viewmodel.GestorPomodoroViewModel;
import com.mk.pomodoro.util.ConstantesAppConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    private long tiempoMuertoAcumulado = 0;
    private long ultimoTiempoPausa = 0;

    private Temporizador temporizador;
    private SharedPreferences preferencias;
    private SharedPreferences.Editor actualizarPreferencias;
    private GestorPomodoroViewModel gestorPomodoro;
    private NotificationManagerCompat gestorNotificaciones;
    private MediaPlayer reproductorAlarma;

    private DaoTipoPomodoro daoTipoPomodoro;
    private DaoIntervalo daoIntervalo;
    private DaoSesion daoSesion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencias = requireActivity().getSharedPreferences(ConstantesAppConfig.NOM_ARCHIVO_PREFERENCIAS, MODE_PRIVATE);
        actualizarPreferencias = preferencias.edit();
        gestorPomodoro = new ViewModelProvider(requireActivity()).get(GestorPomodoroViewModel.class);
        gestorNotificaciones = NotificationManagerCompat.from(requireActivity());

        daoTipoPomodoro = new DaoTipoPomodoroImpl(getContext());
        daoIntervalo = new DaoIntervaloImpl(getContext());
        daoSesion = new DaoSesionImpl(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vista, savedInstanceState);

        layoutPestanas = vista.findViewById(R.id.tlOpcionesTiempo);
        tiempo = vista.findViewById(R.id.tvTiempo);
        barraProgresoCircular = vista.findViewById(R.id.pbCirculo);
        botonDetener = vista.findViewById(R.id.btnDetener);
        botonIniciar = vista.findViewById(R.id.btnReproducir);
        botonDetenerAlarma = vista.findViewById(R.id.btnDetenerAlarma);
        botonPausar = vista.findViewById(R.id.btnPausar);
        botonContinuar = vista.findViewById(R.id.btnContinuar);
        reproductorAlarma = MediaPlayer.create(getActivity(), R.raw.kalimba);

        // Configuracion Predeterminada
        tiempoTrabajo = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_TRABAJO, ConstantesAppConfig.V_TIEMPO_TRABAJO_I);
        tiempoDescanso = preferencias.getInt(ConstantesAppConfig.C_TIEMPO_DESCANSO, ConstantesAppConfig.V_TIEMPO_DESCANSO_I);
        gestorPomodoro.setEstadoTemporizador("Trabajo");
        int pestanaSeleccionada = preferencias.getInt(ConstantesAppConfig.C_PESTANA_SELECCIONADA, ConstantesAppConfig.V_PESTANA_SELECCIONADA);
        TabLayout.Tab tab = layoutPestanas.getTabAt(pestanaSeleccionada);
        if (tab != null) {
            tab.select();
        }
        prepararTemporizador((pestanaSeleccionada == 0 ? tiempoTrabajo : tiempoDescanso) * 60);

        gestorPomodoro.getTemaCambiado().observe(getViewLifecycleOwner(), cambiado -> {
            if (cambiado) {
                reiniciarTemporizadorYActualizarBotones();
                gestorPomodoro.setTemaCambiado(false);
                gestorPomodoro.setTemporizadorIniciado(false);
            }
        });

        layoutPestanas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Actualiza los valores en el ViewModel
                int posicion = tab.getPosition();
                iniciarTemporizador = false;
                if (posicion == 0) {
                    prepararTemporizador(tiempoTrabajo * 60);
                    gestorPomodoro.setEstadoTemporizador("Trabajo");
                } else if (posicion == 1) {
                    prepararTemporizador(tiempoDescanso * 60);
                    gestorPomodoro.setEstadoTemporizador("Descanso");
                }
                actualizarPreferencias.putInt(ConstantesAppConfig.C_PESTANA_SELECCIONADA, posicion);
                actualizarPreferencias.apply();

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
                gestorPomodoro.setTemporizadorIniciado(false);
                finalizarIntervalo();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        botonDetener.setOnClickListener(v -> {
            reiniciarTemporizadorYActualizarBotones();
            gestorNotificaciones.cancel(1);
            iniciarTemporizador = false;
            gestorPomodoro.setTemporizadorIniciado(false);
            finalizarIntervalo();
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
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Observa los cambios en ViewModel
        gestorPomodoro.getTiemposActualizados().observe(getViewLifecycleOwner(), tiemposActualizados -> {
            if (tiemposActualizados) {
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
                gestorPomodoro.setTiemposActualizados(false);
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
        finalizarIntervalo();
    }

    private void prepararTemporizador(int segundos) {
        System.out.println(segundos);
        if (temporizador != null) {
            temporizador.destruirTemporizador();
        }
        // Iniciamos el temporizador solo si iniciarTemporizador es verdadero
        if (iniciarTemporizador) {
            temporizador.iniciarTemporizador();
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
            // Leer la preferencia del usuario para el sonido y vibración
            boolean sonidoHabilitado = preferencias.getBoolean(ConstantesAppConfig.C_SONIDO, ConstantesAppConfig.V_SONIDO_B);
            boolean vibracionHabilitada = preferencias.getBoolean(ConstantesAppConfig.C_VIBRACION, ConstantesAppConfig.V_VIBRACION_B);
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
            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);

            // Oculta los otros botones.
            botonIniciar.setVisibility(View.GONE);
            botonPausar.setVisibility(View.GONE);
            botonContinuar.setVisibility(View.GONE);
            botonDetener.setVisibility(View.GONE);
            // Muestra el botón para detener la alarma.
            botonDetenerAlarma.setVisibility(View.VISIBLE);
            finalizarIntervalo();
        });

        // Mostramos el tiempo inicial sin iniciar el temporizador
        int segundosRestantes = segundos;
        tiempo.setText(String.format(Locale.getDefault(), "%02d:%02d", segundosRestantes / 60, segundosRestantes % 60));
        barraProgresoCircular.setProgress(segundosRestantes);

    }

    private void reiniciarTemporizadorYActualizarBotones() {
        temporizador.reiniciarTemporizador(barraProgresoCircular, tiempo);
        botonIniciar.setVisibility(View.VISIBLE);
        botonPausar.setVisibility(View.GONE);
        botonContinuar.setVisibility(View.GONE);
        botonDetener.setVisibility(View.GONE);
        finalizarIntervalo();
    }

    private void finalizarIntervalo() {
        if (intervaloActivado) {
            long fechaFin = System.currentTimeMillis();
            long duracionTotal = fechaFin - fechaInicio;
            long duracionActiva = duracionTotal - tiempoMuertoAcumulado;

            intervaloActivado = false;
            guardarIntervalo(fechaInicio, fechaFin, duracionActiva);
            evaluarIntervalo();
            ultimoTiempoPausa = 0;
            tiempoMuertoAcumulado = 0;
            gestorPomodoro.setDatosTemporizadorActualizados(true);
        }
    }
    private void guardarIntervalo(long inicio, long fin, long duracionActiva) {

        int opcionSeleccionada = preferencias.getInt(ConstantesAppConfig.C_OPCION_SELECCIONADA, ConstantesAppConfig.V_OPCION_SELECCIONADA);
        int pestanaSeleccionada = preferencias.getInt(ConstantesAppConfig.C_PESTANA_SELECCIONADA, ConstantesAppConfig.V_PESTANA_SELECCIONADA);
        boolean esTrabajo = pestanaSeleccionada == 0; //pestanaSeleccionada == 0 ? esTrabajo = true : esTrabajo = false)

        // Crear un objeto Intervalo con las fechas de inicio y fin
        Intervalo intervalo = new Intervalo();
        intervalo.setTipoId(obtenerIdTipoPomodoro(opcionSeleccionada));
        intervalo.setEsTrabajo(esTrabajo);
        intervalo.setFechaInicio(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date(inicio)));
        intervalo.setFechaFin(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date(fin)));
        intervalo.setDuracionTotal((int) duracionActiva);

        daoIntervalo.insertarIntervalo(intervalo);
    }

    private void evaluarIntervalo() {
        // Error cuando el ultimo intervalo es trabajo y el nuevo tambien es trabajo
        SesionDTO ultimaSesion = daoSesion.obtenerUltimaSesion();
        Intervalo intervaloActual = daoIntervalo.obtenerUltimoIntervalo();

        SimpleDateFormat isoFormato = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.getDefault());

        if(ultimaSesion != null) {
            if(ultimaSesion.isCompleta()) {
                registrarSesion(intervaloActual);
            } else {
                if(ultimaSesion.getIdIntervaloTrabajo() != 0 && !intervaloActual.isEsTrabajo()){ // Es trabajo - No se guarda null en la bd
                    try {
                        Date fechaInicio = isoFormato.parse(intervaloActual.getFechaInicio());
                        Intervalo intervaloSesion = daoIntervalo.obtenerIntervaloPorId(ultimaSesion.getIdIntervaloTrabajo());
                        Date fechaFin = isoFormato.parse(intervaloSesion.getFechaFin());
                        if (fechaInicio != null && fechaFin != null) {
                            long limiteMaximo = 5 * 60 * 1000; // 5 min
                            long diferenciaDias = TimeUnit.MILLISECONDS.toDays(fechaFin.getTime() - fechaInicio.getTime());

                            if (diferenciaDias <= 1) {
                                long diferenciaMilisegundos = fechaFin.getTime() - fechaInicio.getTime();
                                if (diferenciaMilisegundos > limiteMaximo) {
                                    registrarSesion(intervaloActual);
                                } else {
                                    actualizarUltimaSesion(intervaloActual, ultimaSesion);
                                }
                            } else {
                                registrarSesion(intervaloActual);
                            }
                        }  else {
                            Log.e("Pomodoro", "Error: Fecha inicio o fecha fin es nula");
                        }
                    } catch (ParseException e) {
                        Log.e("Pomodoro", "Error al analizar las fechas", e);
                    }
                } else { // Es descanso
                    registrarSesion(intervaloActual);
                }
            }
        } else {
            registrarSesion(intervaloActual);
        }
    }

    public void registrarSesion(Intervalo ultimoIntervalo) {
        if (ultimoIntervalo != null) {
            SesionDTO nuevaSesion = new SesionDTO();
            nuevaSesion.setIdIntervaloTrabajo(ultimoIntervalo.isEsTrabajo() ? ultimoIntervalo.getIdIntervalo() : null);
            nuevaSesion.setIdIntervaloDescanso(ultimoIntervalo.isEsTrabajo() ? null : ultimoIntervalo.getIdIntervalo());
            nuevaSesion.setFechaInicioSesion(ultimoIntervalo.getFechaInicio());
            nuevaSesion.setDuracionTotalSesion(ultimoIntervalo.getDuracionTotal());
            nuevaSesion.setCompleta(false); // Por defecto, la sesión no está completa
            daoSesion.insertarSesion(nuevaSesion);
            Log.e("Pomodoro", "Exito: se inserto el ultimoIntervalo en una nueva sesión");
        } else {
            Log.e("Pomodoro", "Error: ultimoIntervalo es nulo al registrar la sesión");
        }
    }

    public void actualizarUltimaSesion(Intervalo ultimoIntervalo, SesionDTO ultimaSesion) {
        if (ultimoIntervalo != null) {
            ultimaSesion.setIdIntervaloDescanso(ultimoIntervalo.getIdIntervalo());
            ultimaSesion.setDuracionTotalSesion(ultimaSesion.getDuracionTotalSesion() + ultimoIntervalo.getDuracionTotal());
            ultimaSesion.setCompleta(true); // Marca la sesión como completa
            daoSesion.actualizarSesion(ultimaSesion);
            Log.e("Pomodoro", "Exito: se inserto el ultimoIntervalo en la sesión existente");
        } else {
            Log.e("Pomodoro", "Error: ultimoIntervalo es nulo al actualizar la sesión");
        }
    }

    private int obtenerIdTipoPomodoro(int opcionSeleccionada) {
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
                // Crear un nuevo tipo de Pomodoro personalizado y obtener su ID
                TipoPomodoro tipoPersonalizado = new TipoPomodoro();
                tipoPersonalizado.setNombre(nombreTipo);
                tipoPersonalizado.setTiempoTrabajoEstablecido(tiempoTrabajo);
                tipoPersonalizado.setTiempoDescansoEstablecido(tiempoDescanso);

                return (int) daoTipoPomodoro.insertarTipoPomodoro(tipoPersonalizado);
        }
        // Consultar en la base de datos para obtener el ID basado en el nombre
        return daoTipoPomodoro.obtenerIdPorNombre(nombreTipo);
    }
}