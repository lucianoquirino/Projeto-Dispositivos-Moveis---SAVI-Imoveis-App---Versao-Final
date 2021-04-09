package com.lucianoquirino.saviimoveisapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lucianoquirino.saviimoveisapp.R;
import com.lucianoquirino.saviimoveisapp.model.Anuncio;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterAnuncios extends RecyclerView.Adapter<AdapterAnuncios.MyViewHolder> {

    private List<Anuncio> anuncios;
    private Context context;

    public AdapterAnuncios(List<Anuncio> anuncios, Context context) {
        this.anuncios = anuncios;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_anuncio, parent, false);
        return new MyViewHolder( item );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Anuncio anuncio = anuncios.get(position);
        holder.titulo.setText( anuncio.getTitulo() );
        holder.valor.setText( anuncio.getValor() );

        List<String> urlFotos = anuncio.getFotos();

        if (urlFotos != null){
            String urlCapa = urlFotos.get(0).toString();
            Picasso.get()
                    .load(urlCapa)
                    .into(holder.foto);
        } else{
            holder.foto.setImageResource(R.drawable.padrao);
        }

        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        //Glide.with(context)
        //        .load(anuncio.getFotos())
        //        .into(holder.foto);

    }

    @Override
    public int getItemCount() {

        return anuncios.size();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titulo;
        TextView valor;
        ImageView foto;

        public MyViewHolder(View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.textTitulo);
            valor = itemView.findViewById(R.id.textValor);
            foto = itemView.findViewById(R.id.imageAnuncio);
        }

    }

}

