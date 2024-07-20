package com.mk.pomodoro.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.mk.pomodoro.R;
import com.mk.pomodoro.dto.SesionDTO;

import java.util.List;
import java.util.Locale;

public class SesionDTOAdapter extends RecyclerView.Adapter<SesionDTOAdapter.SesionDTOViewHolder> {

    private final Context contexto;
    private final List<SesionDTO> listaSesionDTO;

    public SesionDTOAdapter(Context contexto, List<SesionDTO> listaSesionDTO) {
        this.contexto = contexto;
        this.listaSesionDTO = listaSesionDTO;
    }

    public void agregarSesiones(List<SesionDTO> nuevasSesiones) {
        int posicionInicio  = listaSesionDTO.size();
        listaSesionDTO.addAll(nuevasSesiones);
        notifyItemRangeInserted(posicionInicio , nuevasSesiones.size());
    }

    public void borrarSesiones() {
        int cantidadElementos  = listaSesionDTO.size();
        listaSesionDTO.clear();
        notifyItemRangeRemoved(0, cantidadElementos );
    }

    class SesionDTOViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView tvNumeroSesion;
        private final AppCompatTextView tvDetalleSesion;
        private final AppCompatImageView ivSesionIncompleto;
        private final AppCompatImageView ivSesionCompleto;

        private SesionDTOViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumeroSesion = itemView.findViewById(R.id.tvNumeroSesion);
            tvDetalleSesion = itemView.findViewById(R.id.tvDetalleSesion);
            ivSesionIncompleto = itemView.findViewById(R.id.ivSesionIncompleto);
            ivSesionCompleto = itemView.findViewById(R.id.ivSesionCompleto);
        }

        public void bind(SesionDTO sesionDTO, Context context) {
            tvNumeroSesion.setText(context.getString(R.string.frag_ren_item_sesion_num, sesionDTO.getNumeroSesion()));

            String detalle = "";
            if (sesionDTO.getTrabajoDuracionTotal() > 0) {
                detalle += "Trabajo: " + formatearTiempo(sesionDTO.getTrabajoDuracionTotal());
            }
            if (sesionDTO.getDescansoDuracionTotal() > 0) {
                if (!detalle.isEmpty()) {
                    detalle += "\n";
                }
                detalle += "Descanso: " + formatearTiempo(sesionDTO.getDescansoDuracionTotal());
            }
            tvDetalleSesion.setText(detalle);
            if (sesionDTO.isCompleta()) {
                ivSesionIncompleto.setVisibility(View.GONE);
                ivSesionCompleto.setVisibility(View.VISIBLE);
            } else {
                ivSesionIncompleto.setVisibility(View.VISIBLE);
                ivSesionCompleto.setVisibility(View.GONE);
            }
        }
    }

    private String formatearTiempo(long milisegundos) {
        int horas   = (int) ((milisegundos / (1000*60*60)) % 24);
        int minutos = (int) ((milisegundos / (1000*60)) % 60);
        int segundos = (int) (milisegundos / 1000) % 60 ;

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

    @NonNull
    @Override
    public SesionDTOViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexto).inflate(R.layout.item_sesion_hoy_list, parent, false);
        return new SesionDTOViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SesionDTOViewHolder holder, int position) {
        SesionDTO sesionDTO = listaSesionDTO.get(position);
        holder.bind(sesionDTO, contexto);
    }

    @Override
    public int getItemCount() {
        return listaSesionDTO.size();
    }
}
