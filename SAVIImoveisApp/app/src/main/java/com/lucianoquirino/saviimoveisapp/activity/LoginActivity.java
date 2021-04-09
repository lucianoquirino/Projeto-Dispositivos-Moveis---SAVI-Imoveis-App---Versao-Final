package com.lucianoquirino.saviimoveisapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lucianoquirino.saviimoveisapp.R;

import com.lucianoquirino.saviimoveisapp.helper.ConfiguracaoFirebase;
import com.lucianoquirino.saviimoveisapp.model.Usuario;


public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
    }

    public void logarUsuario (Usuario usuario){

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this,
                            "Logado com sucesso",
                            Toast.LENGTH_SHORT).show();
                    abrirTelaAnuncios();

                }else{
                    Toast.makeText( LoginActivity.this,
                            "Erro ao autenticar usuario!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void validarAutenticaoUsuario(View view){

        //Recuperar textos dos campos
        String textEmail = campoEmail.getText().toString();
        String textSenha = campoSenha.getText().toString();

        //Verificcar campos preenchidos
        if( !textEmail.isEmpty()){
            if(!textSenha.isEmpty()){

                Usuario usuario = new Usuario();
                usuario.setEmail(textEmail);
                usuario.setSenha(textSenha);

                logarUsuario(usuario);

            }else{
                Toast.makeText( LoginActivity.this,
                        "Preencha a senha!",
                        Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText( LoginActivity.this,
                    "Preencha o email!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void abrirTelaCadastro(View view){

        Intent intent = new Intent(LoginActivity.this, CadastrarNovoUsuarioActivity.class);
        startActivity(intent);

    }

    public void abrirTelaAnuncios(){

        Intent intent = new Intent(LoginActivity.this, AnunciosActivity.class);
        startActivity(intent);

    }
}