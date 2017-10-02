package com.myapps.core;

import android.content.Context;
import android.content.SharedPreferences;

//Klasa której zadanie jest zapisywać do pamięci długotrwałej aplikacji
public class Storage {

   private SharedPreferences pref;

    public final static String HELLO_SCREEN = "gpssenderHelloScreen"; // stała która ułątwia posługiwanie się klasą , przechowuje nazwę klucza do wartości checkBox

    public Storage(Context context){
        pref = context.getSharedPreferences("com.myapps.gpssender", Context.MODE_PRIVATE);
    }

    //DOdawanie do paięci i edycja
    public void addToStorage(String name , String value){
        pref.edit().putString(name,value).commit();
    }

    //Odczytywanie z pamięci
    public String getFromStorage(String name){
        return pref.getString(name,null);
    }

}
