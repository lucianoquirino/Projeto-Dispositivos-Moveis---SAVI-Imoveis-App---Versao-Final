package com.lucianoquirino.saviimoveisapp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lucianoquirino.saviimoveisapp.R;
import com.lucianoquirino.saviimoveisapp.helper.ConfiguracaoFirebase;
import com.lucianoquirino.saviimoveisapp.helper.Permissoes;
import com.lucianoquirino.saviimoveisapp.model.Anuncio;
import com.santalu.maskara.widget.MaskEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

import static android.R.layout;

public class CadastrarAnuncioActivity extends AppCompatActivity
    implements View.OnClickListener{

    private EditText campoTítulo, campoEndereco, campoDescricao;
    private ImageView imagem1, imagem2, imagem3, imagem4;
    private Spinner campoEstado, campoTipo, campoCategoria;
    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private Anuncio anuncio;
    private StorageReference storage;
    private android.app.AlertDialog dialog;
    private static final int SELECAO_GALERIA = 200;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private List<String> listadeFotosRecuperadas = new ArrayList<>();
    private List<String> listadeURLFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);

        //Configurações iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //Validar permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();
        carregarDadosSpinner();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for( int permissaoResultado : grantResults){
            if( permissaoResultado== PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }



    private void inicializarComponentes(){

        campoTítulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoEndereco = findViewById(R.id.editEndereco);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoTipo = findViewById(R.id.spinnerTipo);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        imagem1 = findViewById(R.id.imagemCadastro1);
        imagem2 = findViewById(R.id.imagemCadastro2);
        imagem3 = findViewById(R.id.imagemCadastro3);
        imagem4 = findViewById(R.id.imagemCadastro4);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);
        imagem4.setOnClickListener(this);

        //Configura localidade para pt -> portugues BR -> Brasil
        Locale locale = new Locale("pt", "BR");
        campoValor.setDefaultLocale(locale);
    }

    public void validarDadosAnuncio(View view) {

        anuncio = configurarAnuncio();
        String valor = String.valueOf(campoValor.getRawValue());

        if (listadeFotosRecuperadas.size() != 0) {

            if(!anuncio.getEstado().isEmpty()){

                if(!anuncio.getTipo().isEmpty()){

                    if(!anuncio.getCategoria().isEmpty()){

                        if(!anuncio.getTitulo().isEmpty()){

                            if(!anuncio.getEndereco().isEmpty()){

                                if(!valor.isEmpty() && !valor.equals("0")){

                                    if(!anuncio.getTelefone().isEmpty()){

                                        if(!anuncio.getDescricao().isEmpty()){

                                            cadastrarAnuncio();

                                        }else{
                                            exibirMenssagemErro("Preencha o campo descrição!");
                                        }

                                    }else{
                                        exibirMenssagemErro("Preencha o campo valor!");
                                    }

                                }else{
                                    exibirMenssagemErro("Preencha o campo valor!");
                                }

                            }else{
                                exibirMenssagemErro("Preencha o campo endereço!");
                            }

                        }else{
                            exibirMenssagemErro("Preencha o campo título!");
                        }

                    }else{
                        exibirMenssagemErro("Selecione a categoria!");
                    }

                }else{
                    exibirMenssagemErro("Selecione o tipo!");
                }

            }else{
                exibirMenssagemErro("Selecione o estado!");
            }

        } else {
            exibirMenssagemErro("Selecione ao menos uma foto!");
        }
    }

    public void cadastrarAnuncio(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando Anúncio")
                .setCancelable(false)
                .build();
        dialog.show();

        //Salvar imagem no Storage
        for (int i=0; i<listadeFotosRecuperadas.size(); i++){
            String urlImagem = listadeFotosRecuperadas.get(i);
            int tamanhoLista = listadeFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i);
        }

    }

    private void salvarFotoStorage(String urlString, int totalFotos, int contador){

        final StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem"+contador);

        //Fazer upload do arquivo
        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlString));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> firebaseUrl = taskSnapshot.getStorage().getDownloadUrl();
                String urlConvertida = firebaseUrl.toString();

                listadeURLFotos.add(urlConvertida);

                if(totalFotos == listadeURLFotos.size()){
                    anuncio.setFotos(listadeURLFotos);
                    anuncio.salvar();

                    dialog.dismiss();
                    finish();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMenssagemErro("Falha ao fazer upload");
                Log.i("INFO", "Falha ao fazer upload:" + e.getMessage());
            }
        });


    }

    private Anuncio configurarAnuncio(){

        String estado = campoEstado.getSelectedItem().toString();
        String tipo = campoTipo.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTítulo.getText().toString();
        String endereco = campoEndereco.getText().toString();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString();
        String descricao = campoDescricao.getText().toString();

        Anuncio anuncio = new Anuncio();
        anuncio.getIdAnuncio();
        anuncio.setEstado(estado);
        anuncio.setTipo(tipo);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setEndereco(endereco);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);

        return anuncio;

    }

    private void exibirMenssagemErro(String mensagem){

        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onClick(View v) {

        switch ( v.getId()){
            case R.id.imagemCadastro1:
                escolherImagem(1);
                break;
            case R.id.imagemCadastro2:
                escolherImagem(2);
                break;
            case R.id.imagemCadastro3:
                escolherImagem(3);
                break;
            case R.id.imagemCadastro4:
                escolherImagem(4);
                break;
        }
    }

    public void escolherImagem(int requestCode){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (i.resolveActivity(getPackageManager()) != null){
            startActivityForResult(i, requestCode);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK){

            //Recuperar Imagem
            Bitmap imagem = null;
            Uri imagemSelecionada = data.getData();
            try {
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemSelecionada);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String caminhoImagem = imagemSelecionada.toString();

            //Configurar imagem no ImageView
            if (requestCode==1){

                imagem1.setImageBitmap(imagem);

            }else if( requestCode == 2){

                imagem2.setImageBitmap(imagem);

            }else if(requestCode==3){

                imagem3.setImageBitmap(imagem);

            }else if(requestCode==4) {

                imagem4.setImageBitmap(imagem);
            }
            listadeFotosRecuperadas.add(caminhoImagem);
        }

    }

    private void carregarDadosSpinner(){

        // Configuração spinner estado
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<String>(
                this, layout.simple_spinner_item,
                estados
        );
        adapterEstado.setDropDownViewResource(layout.simple_spinner_dropdown_item);
        campoEstado.setAdapter(adapterEstado);

        // Configuração spinner tipo
        String[]  tipo= getResources().getStringArray(R.array.tipos);
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<String>(
                this, layout.simple_spinner_item,
                tipo
        );
        adapterTipo.setDropDownViewResource(layout.simple_spinner_dropdown_item);
        campoTipo.setAdapter(adapterTipo);

        // Configuração spinner categoria
        String[]  categorias= getResources().getStringArray(R.array.Categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this, layout.simple_spinner_item,
                categorias
        );
        adapterCategoria.setDropDownViewResource(layout.simple_spinner_dropdown_item);
        campoCategoria.setAdapter(adapterCategoria);

    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}