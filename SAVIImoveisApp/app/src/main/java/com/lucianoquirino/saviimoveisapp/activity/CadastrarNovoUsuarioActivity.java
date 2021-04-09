package com.lucianoquirino.saviimoveisapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.lucianoquirino.saviimoveisapp.R;
import com.lucianoquirino.saviimoveisapp.helper.Base64Custom;
import com.lucianoquirino.saviimoveisapp.helper.ConfiguracaoFirebase;
import com.lucianoquirino.saviimoveisapp.helper.UsuarioFirebase;
import com.lucianoquirino.saviimoveisapp.model.Usuario;

public class CadastrarNovoUsuarioActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_novo_usuario);

        campoNome = findViewById(R.id.editNome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);

    }

    public void cadastrarUsuario(Usuario usuario){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    Toast.makeText(CadastrarNovoUsuarioActivity.this,
                            "Sucesso ao cadastrar usuário",
                            Toast.LENGTH_SHORT).show();
                    UsuarioFirebase.atualizarNomeUsuario( usuario.getNome());
                    finish();

                    try{

                        String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setId( identificadorUsuario );
                        usuario.salvar();

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else{
                    String erroExcecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        erroExcecao = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        erroExcecao = "Por favor, digite um email válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        erroExcecao = "Esta conta de email já foi cadastrada";
                    }catch (Exception e) {
                        erroExcecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastrarNovoUsuarioActivity.this, erroExcecao, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public void validarCadastrodeUsuario(View view){

        //Recuperar textos dos campos
        String textNome = campoNome.getText().toString();
        String textEmail = campoEmail.getText().toString();
        String textSenha = campoSenha.getText().toString();


        //Verificar campos preenchidos
        if(!textNome.isEmpty() ){
            if( !textEmail.isEmpty()){
                if(!textSenha.isEmpty()){

                    Usuario usuario = new Usuario();
                    usuario.setNome(textNome);
                    usuario.setEmail(textEmail);
                    usuario.setSenha(textSenha);

                    cadastrarUsuario(usuario);

                }else{
                    Toast.makeText( CadastrarNovoUsuarioActivity.this,
                            "Preencha a senha!",
                            Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText( CadastrarNovoUsuarioActivity.this,
                        "Preencha o email!",
                        Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText( CadastrarNovoUsuarioActivity.this,
                    "Preencha o nome!",
                    Toast.LENGTH_SHORT).show();

        }
    }


}