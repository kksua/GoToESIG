package com.example.gotoesig;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrajetAdapter extends RecyclerView.Adapter<TrajetAdapter.TrajetViewHolder> {
    private static final String TAG = "TrajetDetailsDebug";

    private final List<Trajet> trajets;
    private static final LatLng END_POINT = new LatLng(49.395264, 1.088951); // Coordonnées GPS de ESIGELEC

    public TrajetAdapter(List<Trajet> trajets) {
        this.trajets = trajets;
    }

    @NonNull
    @Override
    public TrajetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trajet, parent, false);
        return new TrajetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrajetViewHolder holder, int position) {
        Trajet trajet = trajets.get(position);
        Log.d("TrajetAdapterDebug", "Trajet Lat: " + trajet.getLatitude() + ", Lon: " + trajet.getLongitude());

        // Format date and time if they exist
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String date = trajet.getDate() != null ? dateFormat.format(trajet.getDate()) : "--";
        String time = trajet.getHeure() != null ? trajet.getHeure() : "--";

        holder.dateTime.setText(date);
        holder.time.setText(time);
        holder.pointDepart.setText(trajet.getPointDepart() != null ? trajet.getPointDepart() : "Point de départ non fourni");
        holder.distance.setText(trajet.getDistance() > 0 ? String.format(Locale.getDefault(), "%.2f km", trajet.getDistance()) : "Distance non fournie");
        holder.moyenTransport.setText(trajet.getMoyenTransport() != null ? trajet.getMoyenTransport() : "Transport non fourni");
        holder.duree.setText(trajet.getDuree() > 0 ? String.format(Locale.getDefault(), "%.2f minutes", trajet.getDuree()) : "Durée non fournie");
        // Add OnClickListener to navigate to TrajetDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TrajetDetailsActivity.class);

            // Pass latitude and longitude directly
            intent.putExtra("startLat", trajet.getLatitude());
            intent.putExtra("startLon", trajet.getLongitude());
            intent.putExtra("documentId", trajet.getdocumentId());
            Log.d("TrajetAdapterDebug", "Intent Data Sent: Lat = " + trajet.getLatitude() + ", Lon = " + trajet.getLongitude());

            // Start the activity
            v.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return trajets.size();
    }

    static class TrajetViewHolder extends RecyclerView.ViewHolder {
        TextView dateTime, time, pointDepart, distance, moyenTransport, duree;

        public TrajetViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTime = itemView.findViewById(R.id.dateTime);
            time = itemView.findViewById(R.id.Time);
            pointDepart = itemView.findViewById(R.id.pointDepart);
            distance = itemView.findViewById(R.id.distance);
            moyenTransport = itemView.findViewById(R.id.moyenTransport);
            duree = itemView.findViewById(R.id.duree);
        }
    }
}