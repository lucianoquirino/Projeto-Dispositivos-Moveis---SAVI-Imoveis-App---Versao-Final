package com.lucianoquirino.saviimoveisapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lucianoquirino.saviimoveisapp.R;
import com.lucianoquirino.saviimoveisapp.adapter.AdapterAnuncios;
import com.lucianoquirino.saviimoveisapp.helper.ConfiguracaoFirebase;
import com.lucianoquirino.saviimoveisapp.helper.RecyclerItemClickListener;
import com.lucianoquirino.saviimoveisapp.model.Anuncio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recycleAnunciosPublicos;
    private AdapterAnuncios adapterAnuncios;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private DatabaseReference anunciosPublicosRef;
    private String filtroEstado = "";
    private String filtroTipo = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;
    private boolean filtrandoPorTipo = false;
    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        inicializarComponentes();

        //Configurações iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios");

        //Configurar RecyclerView
        recycleAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recycleAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(listaAnuncios, this);
        recycleAnunciosPublicos.setAdapter( adapterAnuncios );

        recuperarAnunciosPublicos();

        //Aplicar evento de clique
        recycleAnunciosPublicos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recycleAnunciosPublicos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                //Detalhe Anuncio
                                Anuncio anuncioSelecionado = listaAnuncios.get(position);
                                Intent i = new Intent(AnunciosActivity.this, DetalhesAnuncioActivity.class);
                                i.putExtra("anuncioSelecionado", anuncioSelecionado);
                                startActivity(i);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
    }

    //Passa os arquivos de menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Fazer verificações e alterar itens já carregados
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        if(autenticacao.getCurrentUser() == null){ //usuario deslogado
            menu.setGroupVisible(R.id.group_deslogado, true);
        }else{ //usuario logado
            menu.setGroupVisible(R.id.group_Logado, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //Testar qual item de menu foi selecionado
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.menu_login:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
            case R.id.menu_sair:
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;
            case R.id.menu_anuncios:
                startActivity(new Intent(getApplicationContext(),MeusAnunciosActivity.class));
                break;
            case R.id.menu_configuracoes:
                startActivity(new Intent(getApplicationContext(), ConfiguracoesUsuarioActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void inicializarComponentes(){
        recycleAnunciosPublicos = findViewById(R.id.recycleAnunciosPublicos);
    }


    public void filtrarPorEstado(View view){

        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione a região desejada");

        //Configurar Spinner
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                estados
        );
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstado);

        dialogEstado.setView(viewSpinner);

        dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                filtroEstado = spinnerEstado.getSelectedItem().toString();
                recuperarAnunciosPorEstado();
                filtrandoPorEstado = true;

            }
        });

        dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();
    }

    public void recuperarAnunciosPorEstado(){

        //Configura nó por estado
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado);

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaAnuncios.clear();

                for(DataSnapshot tipo: dataSnapshot.getChildren()){

                    for(DataSnapshot categorias: tipo.getChildren()){

                        for (DataSnapshot anuncios: categorias.getChildren()){

                            Anuncio anuncio = anuncios.getValue(Anuncio.class);
                            listaAnuncios.add(anuncio);
                        }
                    }
                }

                Collections.reverse( listaAnuncios );
                adapterAnuncios.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void filtrarPorTipo(View view){

        if(filtrandoPorEstado == true){

            AlertDialog.Builder dialogTipo = new AlertDialog.Builder(this);
            dialogTipo.setTitle("Selecione o tipo desejado");

            //Configurar Spinner
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
            Spinner spinnerTipo = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] tipo = getResources().getStringArray(R.array.tipos);
            ArrayAdapter<String> adapterTipo = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item,
                    tipo
            );

            adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTipo.setAdapter(adapterTipo);

            dialogTipo.setView(viewSpinner);

            dialogTipo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    filtroTipo = spinnerTipo.getSelectedItem().toString();
                    recuperarAnunciosPorTipo();
                    filtrandoPorTipo = true;

                }
            });

            dialogTipo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = dialogTipo.create();
            dialog.show();

        }else{
            Toast.makeText(this,"Escolha primeiro uma região!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void recuperarAnunciosPorTipo(){

        //Configura nó por tipo
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroTipo);

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaAnuncios.clear();

                for(DataSnapshot categorias: dataSnapshot.getChildren()){

                    for (DataSnapshot anuncios: categorias.getChildren()){

                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        listaAnuncios.add(anuncio);
                    }
                }

                Collections.reverse( listaAnuncios );
                adapterAnuncios.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void filtrarPorCategoria(View view){

        if(filtrandoPorTipo == true){

            AlertDialog.Builder dialogCategoria = new AlertDialog.Builder(this);
            dialogCategoria.setTitle("Selecione a categoria desejada");

            //Configurar Spinner
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
            Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] categoria = getResources().getStringArray(R.array.Categorias);
            ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item,
                    categoria
            );

            adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapterCategoria);

            dialogCategoria.setView(viewSpinner);

            dialogCategoria.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                    recuperarAnunciosPorCategoria();

                }
            });

            dialogCategoria.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = dialogCategoria.create();
            dialog.show();

        }else{
            Toast.makeText(this,"Escolha primeiro um tipo!",
                    Toast.LENGTH_SHORT).show();
        }

    }


    public void recuperarAnunciosPorCategoria(){

        //Configura nó por Categoria
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroTipo)
                .child(filtroCategoria);

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaAnuncios.clear();

                for (DataSnapshot anuncios: dataSnapshot.getChildren()){

                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        listaAnuncios.add(anuncio);
                }

                Collections.reverse( listaAnuncios );
                adapterAnuncios.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void recuperarAnunciosPublicos(){

        listaAnuncios.clear();
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot estados: dataSnapshot.getChildren()){

                    for(DataSnapshot tipo: estados.getChildren()){

                        for(DataSnapshot categorias: tipo.getChildren()){

                            for (DataSnapshot anuncios: categorias.getChildren()){

                                Anuncio anuncio = anuncios.getValue(Anuncio.class);
                                listaAnuncios.add(anuncio);

                                Collections.reverse(listaAnuncios);
                                adapterAnuncios.notifyDataSetChanged();
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}