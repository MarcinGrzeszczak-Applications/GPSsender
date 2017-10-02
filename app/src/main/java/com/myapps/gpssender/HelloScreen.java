package com.myapps.gpssender;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.myapps.core.App;
import com.myapps.core.ReadContacts;
import com.myapps.core.Storage;

public class HelloScreen extends AppCompatActivity {

    private  Storage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_screen);
    }


    @Override
    protected void onStart() {
        super.onStart();
        storage = new Storage(this); // Nowy obiekt klasy Storage

        checkContactPermmison(); // Sprawdzenie pozwoleń na dostęp do listy kontaktów , tylko dla API 23 (Marshmellow) , oraz wywołanie nowego wątku z ładowaniem kontaktów do ArrayList

        if(storage.getFromStorage(Storage.HELLO_SCREEN) != null) // sprwadzenie czy już kiedyś kliknięto checkBox (nie pokazuj tego okna ponownie)
            goToSendingScreen(); // wywołanie funkcji przechodzenia do okna wysyłania wiadomości

        findViewById(R.id.helloNextBtn).setOnClickListener(nextBtnListener); // przypisanie osługi zdarzń kliknięcia dla przycisku Dalej

    }

    // Ciało zdarzenia kliknięcia przycisku dalej
    View.OnClickListener nextBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

           final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox); // znaleźienie komponentu checkBox w layoucie

            if(checkBox.isChecked()){  //Sprawdzenie czy zaznaczono checkBox
                storage.addToStorage(Storage.HELLO_SCREEN,"checked"); // jeśli tak to przypisz do SharedPreferences (zapisanie długotrwałe , nawet po wyłączeniu aplikacji) że kliknięto
            }

            goToSendingScreen(); // załaduj nowe okno

        }
    };

    //Przechodzenie do nowego okna
    private void goToSendingScreen(){
        final Intent nextWindow = new Intent(HelloScreen.this, SendingScreen.class);
        startActivity(nextWindow);
        finish();
    }

    //Obsługa zdarzeń dla sprawdzenia co kliknięto dla pozwolenia dostępu do kontaktów , tylko dla API 23
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0x3: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    readContacts();

                }else {
                   checkContactPermmison();
                }

            }

        }
    }

    //Funkcja sprawdzająca czy aplikacji przyznano pozwolenie dla kontaktów tylko dla API 23 (Marshmellow)
    @TargetApi(Build.VERSION_CODES.M)
    private void checkContactPermmison(){
        if(Build.VERSION.SDK_INT ==  Build.VERSION_CODES.M) {
            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        0x3);
            } else
                readContacts();
        }
        else
            readContacts();
    }

    //funkcja której celem jest wywołanie nowego wątku odczytywania kontaktów
    private void readContacts(){
        //Tworzenie nowego wątku używajac klasy ReadContacts. runReadContacts to globalna zmienna widoczna w każdej klasie.
        ((App) getApplication()).runReadContacts = new Thread(new ReadContacts(this));
        ((App) getApplication()).runReadContacts.start();
    }
}
