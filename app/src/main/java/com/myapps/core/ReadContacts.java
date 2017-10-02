package com.myapps.core;

import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.HashMap;
import java.util.Map;

public class ReadContacts implements Runnable { // klasa uruchamiana w nowym wątku

   private Activity activity;
    public ReadContacts(Activity activity){
        this.activity = activity;
        ((App)activity.getApplication()).contactList.clear();
    }

    @Override
    public void run() {

        // ustawianie kursora
        Cursor cursor = activity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        Cursor phones;
        String contactId, name , number,hasPhone;
        Map<String,String> tmpData;

        while(cursor.moveToNext()){ // pętla wykonuje się tak długo dopóki nie skończą się pozycje w kursorze

            contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); // pobranie id kontaktu
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)); // pobranie nazwy kontaktu
            hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)); // sprawdzenie czy kontakt ma przypisany numer
            if(Integer.parseInt(hasPhone) > 0){

                phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, //ustawienie kursora na pobieranie numeru od konkretnego kontaktu przy urzyciu id kontaktu
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = "+contactId,
                        null,null);

                while(phones.moveToNext()) { //pętla torchę bez użyteczna , bo i tak przy pierwszym obrocie pętli się wyłączy za pomocą break , ale zostawiłem ją

                    number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)); // pobranie numeru kontaktu

                    tmpData = new HashMap<String, String>();

                    tmpData.put("Name", name);  // dodanie nazwy do Map
                    tmpData.put("Phone", number); // dodanie numeru do Map
                    ((App) activity.getApplication()).contactList.add(tmpData); // Zapisanie Map do ArrayList
                    break;
                }
                phones.close(); //zamknięcie kursora dla numeru
            }

        }
        ((App) activity.getApplication()).contactsReady = true; // zmiana zmiennej globalnej potwierdzająca pobranie wszystkich kontaktów
        cursor.close(); // zamknięcie kursora dla kontaktów
    }

}
