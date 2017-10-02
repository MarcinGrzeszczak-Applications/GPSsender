package com.myapps.gpssender;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.myapps.core.App;
import com.myapps.core.GPSModule;
import com.myapps.core.SMSModule;

import java.util.Map;

public class SendingScreen extends AppCompatActivity {

    private GPSModule gps;
    private EditText textBody;
    boolean link = false;
    private  AutoCompleteTextView contactsView;
    private String finalNumber;
    private String finalBody;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // włączenie opcji menu
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {// Tworzenie menu

        Intent options;

        switch(item.getItemId())
        {
            case R.id.action_about:
                options = new Intent(SendingScreen.this,AboutScreen.class);
                startActivity(options);
                break;
            case R.id.action_licence:
                options = new Intent(SendingScreen.this,LicenceScreen.class);
                startActivity(options);
                break;
            case R.id.action_logout:
                System.exit(1);
                break;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0x4: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    new sendSMS().execute();

                }else {
                    checkSmsPermissons();
                }

            }
            break;

            case 0x2:

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    gps.actualPosition();

                }else {
                   gps.check();
                }
                break;
        }
    }

    // Przechwytywanie decyzji użytkownika o zezwoleniu na dostęp do zasobów telefonu takich jak kontakty czy lokalizacja. Odnosi się to tylko do API 23 (Marshmellow)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
                  //  i 0x1 odnosi się wyłącznie do pozwoleń o udostępnienie lokalizacji oraz jeżeli GPS jest wyłączony to o włączenie lokalizacji
            case 0x1:
                switch (resultCode) {
                    case RESULT_OK:
                        gps.actualPosition();  // wywołanie metody o aktualną pozycję z klasy GPSModule
                        break;
                    case RESULT_CANCELED:
                    default:
                        gps.check();         // powrót do funkcji sprawdzania pozwolneń dostępu , specjalne zapętlenie , bo bez pozwoleń aplikacja dalej nie będzie działać
                        break;
                }
                break;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_screen);
        gps = new GPSModule(GPSlistener,this);              // Deklaracja konstruktora dla klasy GPSModule przekazywany jest listener i activity
    }

    @Override
    protected void onStart() {
        super.onStart();
        finalBody="";    // Zmienna będzie przechowywać ostateczną treść wiadomości która będzie wysyłana. Teraz jest czyszczona.
        finalNumber="";  // Zmienna będzie przechowywać ostateczny numer telefonu na który będzie wysłana wiadomość. Teraz jest czyszczona.



        //Zabezpieczenie przed nie załadowaniem wszystkich kontaktów które wykonywane jest w innym wątku w razie opóźnienia pokazuję się kółko ładowania
        if(!((App) getApplication()).contactsReady) {
            new loadingContacts().execute();  // wywołanie klasy zagnieżdżonej w osobnym wątku gdzie pokazuje na ekranie kółko ładowania , oraz nakazuje temu wątkowi czekać na załadowanie wszystkich kontaktów , uruchamiane tylko w przypadku gdy kontakty nie są załadowane w całości.
        }

        //Tworzenia adaptera (wbudowany) gdzie odwołuję się do layoutu itemku z res/layouts , oraz przypisuję nazwę Name i Phone do danych TextView z layoutu.
        SimpleAdapter adapter = new SimpleAdapter(this,((App) getApplication()).contactList,R.layout.contact_list_item,new String[]{"Name","Phone"},new int[] {R.id.contactItemName,R.id.contactItemNumber});

        //Odszukanie komponentu autocompleteTextView w danym layoucie
        contactsView = (AutoCompleteTextView) findViewById(R.id.autoCompleteContacts);

        //przypisanie wcześniej utworzonego adaptera
        contactsView.setAdapter(adapter);

        //Włączenie wyszukiania po wpisaniu 1 znaku
        contactsView.setThreshold(1);

        //Dodanie obsługi zdarzęń po kliknięciu w item z wyświetlaniej listy
        contactsView.setOnItemClickListener(contactItemClickListener);




        //Odszukanie EditText w danym layoucie
        textBody = (EditText) findViewById(R.id.bodyText);

        //Obsługa zdarzeń przyciśnięcia dla przycisków na layoucie
        findViewById(R.id.positionBtn).setOnClickListener(getPositionListener);
        findViewById(R.id.linkBtn).setOnClickListener(linkBtnListener);
        findViewById(R.id.quickSend).setOnClickListener(sendBtnListener);

    }

    //ciało zdarzenia kliknięcia w itemek z listy dla wyszukiwania kontaktów
    AdapterView.OnItemClickListener contactItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position); // Odszukanie klikniętego itemku w kontenerze (ArrayList) po indeksie (position)

            contactsView.setText(item.get("Name")+" <"+item.get("Phone")+">"); // Zmiana wyświetlanego tekstu w komponencie AutocompleteTextView z {Phone= 123456789 Name=Adrian} na Adrian <123456789>
            finalNumber = item.get("Phone"); // Dodanie numeru telefonu do zmiennej
        }
    };

    //ciało zdarzenia obsługującego kliknięcie w przycisk wyślij
    View.OnClickListener sendBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (finalNumber.matches("")) {//Jeżeli nie wybrano żadnego numeru z listy kontaktów przypisz wpisany ręcznie numer
                finalNumber = contactsView.getText().toString(); // przypisanie wpisanego ręcznie numeru do zmiennej
            }
            finalBody = textBody.getText().toString();// przypisanie treści wiadomości do zmiennej (tak na prawdę jest to nie potrzebna zmienna finalBody , bo można pobierać tekst od razu z textBody.getText().ToString() , ale ze zmienną jest czytelniejsze

            checkSmsPermissons();  // Uruchomienie funkcji wysyłania , oraz sprawdzenie pozwoleń dostępu do zasobów tylko dla API 23 (Marshmellow)
        }
    };

    //ciało zdarzenia kliknięcia w przycisk punkt
    View.OnClickListener linkBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
           gps.check();   // wywołanie metody sprawdzenia lokalizacji w klasie GPSModule
            link = true;
        }
    };

    //ciało zdarzenia kliknięcia w przycisk link do mapy
    View.OnClickListener getPositionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gps.check();    // wywołanie metody sprawdzenia lokalizacji w klasie GPSModule
            link = false;
        }
    };

    // ciało listenera GPSModule , ta funkcja wywoływana jest z klasy GPSModule w momencie uzyskania lokalizacji telefonu
   GPSModule.Listener GPSlistener = new GPSModule.Listener(){

       @Override
       public void getActualPosition(double lat, double lng) {
           if(!link)  // sprawdzenie czy zmienna link ma wartość false
            textBody.append("( "+lat+" , "+lng+" )");    // jeżeli link == false to dopisz do treści wiadomości ( 51.8675645 ; 17.12832122 )
           else
            textBody.append("( "+"https://www.google.pl/maps/@"+lat+","+lng+",16z"+" )");// jeżeli link == true to dopisz do treści wiadomości link do mapy google
       }
   };

    // funkcja która sprawdza pozwolenia dostępu do zasobu telefonu do wysyłania SMS , niezbędne tylko dla API 23 (Marshmellow)
    private void checkSmsPermissons(){
        if(Build.VERSION.SDK_INT ==  Build.VERSION_CODES.M) {     // Sprawdzenie czy wersja androida na telefonie to Marshmellow
            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.SEND_SMS); // Sprawdzenie czy przyznano uprawnienia dla aplikacji na wykorzystanie modułu wysyłania sms
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                        0x4);
            } else
                new sendSMS().execute();    // tworzenie nowego obiektu zagnieżdżonej klas sendSMS uruchamianej w innym wątku
        }
        else
            new sendSMS().execute();  // tworzenie nowego obiektu zagnieżdżonej klas sendSMS uruchamianej w innym wątku
    }

    // Zagnieżdżona klasa której zadaniem jest wywołanie czekania tego wątku jeżeli wszystkie kontakty nie został załadowane (ładowanie kontaktó∑ odywa się w innym wątku)
    class loadingContacts extends AsyncTask<Void,Void,Void>{  //inny wątek
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {  //polecenia wykonywane przed uruchomieniem nowego wątku
            dialog  = ProgressDialog.show(SendingScreen.this, "",  // tworzenie kręcącego się kółka
                    getResources().getString(R.string.contact_loading), true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {  // uruchomienie nowego wątku

            try {
                ((App) getApplication()).runReadContacts.join(); // nakazanie czekania wątkowi głównemu
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { // polecenia wykonywane po zakończeniu wątku
            super.onPostExecute(aVoid);
            dialog.dismiss(); // wyłączenie kółka
        }
    }

    // Zagnieżdżona klasa której zadaniem jest utworzenie nowego wątku w którym wywoływana jest metoda wysyłania sms z klasy SMSModule
    // , nowy wątek został właśnie w taki sposób stworzony , żey można było użyć klasy SMSModule w background service w przypadku korzystania
    // z opcji opóźnienia wysłania wiadomości
    class sendSMS extends AsyncTask<Void,Void,Void> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() { //polecenia wykonywane przed uruchomieniem nowego wątku
            super.onPreExecute();
            dialog  = ProgressDialog.show(SendingScreen.this, "",  // Tworzenie kółka ładowania
                   getResources().getString(R.string.sending), true);
            Toast.makeText(SendingScreen.this,getResources().getString(R.string.sending),Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {   // uruchomienie nowego wątku

                    SMSModule sms = new SMSModule(SendingScreen.this); // tworzenie nowego obiektu klasy SMSModule
                    sms.sendSMS(finalNumber,finalBody); // wywołanie metody wysyłającej sms

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { // polecenia wykonywane po zakończeniu wątku
            super.onPostExecute(aVoid);
            dialog.dismiss(); // wyłączenie kółka ładowania
        }
    }

}
