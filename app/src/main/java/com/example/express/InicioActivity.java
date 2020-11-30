package com.example.express;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.express.models.Contactos;
import com.example.express.models.Usuario;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class InicioActivity extends AppCompatActivity {

    BottomNavigationView mBottomNavigationView;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    ListView listV_usuarios;
    Usuario usuarioSelected;

    private RecyclerView mBlogList;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public String uid = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setSelectedItemId((R.id.menu_home));

        inicializarFirebase();

        //RecyclerView
        databaseReference.keepSynced(true);
        mBlogList = (RecyclerView) findViewById(R.id.myRecyclerView);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId() == R.id.menu_contacts){
                    Intent i = new Intent(getApplicationContext(), ContactosActivity.class);
                    startActivity(i);
                    overridePendingTransition(0,0);
                }

                if(item.getItemId() == R.id.menu_account){
                    Intent i = new Intent(getApplicationContext(), MiPerfilActivity.class);
                    startActivity(i);
                    overridePendingTransition(0,0);
                }

                return true;
            }
        });
    }// Fin on Create

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Usuario,UsuarioViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Usuario, UsuarioViewHolder>
                (Usuario.class,R.layout.blog_row,UsuarioViewHolder.class,databaseReference.child("Usuario").orderByChild("profesion").startAt("")) {
            @Override
            protected void populateViewHolder(UsuarioViewHolder usuarioViewHolder, Usuario usuario, int i) {

                usuarioViewHolder.setNombre(usuario.getNombre());
                usuarioViewHolder.setProfesion(usuario.getProfesion());
                usuarioViewHolder.setDescripcion(usuario.getDescripcion());
                usuarioViewHolder.setImagen(getApplicationContext(),usuario.getImagen());

                final String usuarioID = usuario.getUid();
                usuarioViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(InicioActivity.this,MostrarUsuarioActivity.class);
                        intent.putExtra("UsuarioID",usuarioID);
                        intent.putExtra("isContacto","nulo");
                        startActivity(intent);
                    }
                });

                /*
                String idUser = usuario.getUid();
                if (idUser == user.getUid()){
                    usuarioViewHolder.mView.setVisibility(View.GONE);
                    ViewGroup.LayoutParams params = usuarioViewHolder.mView.getLayoutParams();
                    params.height = 0;
                    params.width = 0;
                    params.layoutAnimationParameters = null;
                    usuarioViewHolder.mView.setLayoutParams(params);
                }
                 */
            }
        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }// Fin onStart

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UsuarioViewHolder(View itemView){
            super(itemView);
            mView=itemView;
        }
        public void setNombre(String nombre){
            TextView card_nombre = (TextView)mView.findViewById(R.id.cardNombre);
            card_nombre.setText(nombre);
        }
        public void setProfesion(String profesion){
            TextView card_profesion = (TextView)mView.findViewById(R.id.cardProfesion);
            card_profesion.setText(profesion);
        }
        public void setDescripcion(String descripcion){
            TextView card_descripcion = (TextView)mView.findViewById(R.id.cardDescripcion);
            card_descripcion.setText(descripcion);
        }
        public void setImagen(Context applicationContext, String imagen){
            ImageView card_imagen = (ImageView)mView.findViewById(R.id.cardImage);
            Picasso.get().load(imagen).into(card_imagen);
        }
    }


    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    // MOSTRAR TOP MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_inicio,menu);
        return true;
    }

    // SWITCH DEL TOP MENU
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.iconSearchProfesionist:
                Intent intent = new Intent(InicioActivity.this,ActivityBuscarProfesionista.class);
                startActivity(intent);
                overridePendingTransition(0,0);
                break;

            case R.id.btnSignOut:
                //Detectar al usuario actual
                mAuth = FirebaseAuth.getInstance();

                //SIGN OUT
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                break;

            case R.id.btnDeleteAccount:
                AlertDialog.Builder builder = new AlertDialog.Builder(InicioActivity.this);
                builder.setTitle("Eliminar cuenta permanentemente");
                builder.setMessage("Una vez que elimine la cuenta no podrá recuperarla\n¿Desea eliminar su cuenta?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                user.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(InicioActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                Intent intent = new Intent(InicioActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

/*
Detectar el id del usuario seleccionado


    usuarioSelected = usuario.getUid();
    String usuarioContactosId = usuarioSelected.getUid();
    String usuarioContactosNombre = usuarioSelected.getNombre();

    // Almacenar el id del contacto en la clase Usuario.contactosId
    Contactos c = new Contactos();
    c.setContactoId(usuarioContactosId);
    c.setNombre(usuarioContactosNombre);
    c.setProfesion(usuarioSelected.getProfesion());
    c.setTelefono(usuarioSelected.getTelefono());

    //Crear child contactos
    databaseReference.child("Usuario").child(uid).child("Contactos").child(usuarioContactosId).setValue(c);

    Toast.makeText(InicioActivity.this, "Usuario guardado en contactos", Toast.LENGTH_SHORT).show();

*/