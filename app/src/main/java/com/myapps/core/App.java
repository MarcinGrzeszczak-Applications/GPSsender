package com.myapps.core;

import android.app.Application;

import java.util.ArrayList;
import java.util.Map;

// klasa Aplikacji
public class App extends Application {

    //Zmienne globalne

    public ArrayList<Map<String,String>> contactList;
    public boolean contactsReady = false;
    public Thread runReadContacts;

    public App(){
        contactList = new ArrayList<Map<String, String>>();
    }
}
