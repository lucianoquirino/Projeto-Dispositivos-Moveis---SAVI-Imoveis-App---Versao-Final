package com.lucianoquirino.saviimoveisapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lucianoquirino.saviimoveisapp.R;
import com.lucianoquirino.saviimoveisapp.model.Anuncio;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

public class DetalhesAnuncioActivity extends AppCompatActivity {

    private CarouselView carouselView;
    private TextView titulo;
    private TextView preco;
    private TextView estado;
    private TextView tipo;
    private TextView categoria;
    private TextView descricao;
    private Anuncio anuncioSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_anuncio);

        //Inicializar Componentes de interface
        inicializarComponentes();

        //Recuperar anúncio para exibir
        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");

        if( anuncioSelecionado != null){

            titulo.setText(anuncioSelecionado.getTitulo());
            preco.setText(anuncioSelecionado.getValor());
            estado.setText(anuncioSelecionado.getEstado());
            tipo.setText(anuncioSelecionado.getTipo());
            categoria.setText(anuncioSelecionado.getCategoria());
            descricao.setText(anuncioSelecionado.getDescricao());

            ImageListener imageListener = new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {

                    String urlString = anuncioSelecionado.getFotos().get(position);
                    Picasso.get().load(urlString).into(imageView);

                }
            };

            carouselView.setPageCount(anuncioSelecionado.getFotos().size());
            carouselView.setImageListener(imageListener);
        }
    }

    public void visualizarTelefone(View view){
        Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", anuncioSelecionado.getTelefone(), null));
        startActivity(i);
    }

    public void visualizarLocalizacao(View view){

        String localizacao = anuncioSelecionado.getEndereco();

        if (!localizacao.equals("")){

            Intent intent = new Intent(DetalhesAnuncioActivity.this, LocalizacaoImovelActivity.class);
            intent.putExtra("localizacao", localizacao);
            startActivity(intent);

       }else{
            Toast.makeText(this,
                    "Localização não cadastrado!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void inicializarComponentes(){
        carouselView = findViewById(R.id.carouselView);
        titulo = findViewById(R.id.textTituloDetalhe);
        preco = findViewById(R.id.textPrecoDetalhe);
        estado = findViewById(R.id.textEstadoDetalhe);
        tipo = findViewById(R.id.textTipo);
        categoria = findViewById(R.id.textCategoriaDetalhe);
        descricao = findViewById(R.id.textDescricaoDetalhe);
    }
}