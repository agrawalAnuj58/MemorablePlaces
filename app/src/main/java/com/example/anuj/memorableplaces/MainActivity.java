package com.example.anuj.memorableplaces;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> places = new ArrayList<>();
    static ArrayList<String> latitude = new ArrayList<>();
    static ArrayList<String> longitude = new ArrayList<>();
    static ArrayAdapter arrayAdapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);

        sharedPreferences = this.getSharedPreferences("com.example.anuj.memorableplaces",MODE_PRIVATE);

        places.clear();
        latitude.clear();
        longitude.clear();

        try {
            places = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("permanentPlaces",ObjectSerializer.serialize(new ArrayList<String>())));
            latitude = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("permanentLatitude",ObjectSerializer.serialize(new ArrayList<String>())));
            longitude = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("permanentLongitude",ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (places.size() == 0){
            places.add("Add new place....");
            latitude.add("0");
            longitude.add("0");
        }

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("listPosition", position);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                createDialog(view, position);
                return true;
            }
        });
    }

    public void createDialog(View view, final int position){
        AlertDialog.Builder abd = new AlertDialog.Builder(this);
        abd.setIcon(android.R.drawable.ic_delete)
                .setTitle("Delete this Place!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.places.remove(position);
                        MainActivity.latitude.remove(position);
                        MainActivity.longitude.remove(position);
                        try {
                            sharedPreferences.edit().putString("permanentPlaces",ObjectSerializer.serialize(places)).apply();
                            sharedPreferences.edit().putString("permanentLatitude", ObjectSerializer.serialize(latitude)).apply();
                            sharedPreferences.edit().putString("permanentLongitude", ObjectSerializer.serialize(longitude)).apply();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        MainActivity.arrayAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "Note Deleted!!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No",null);
        abd.show();
    }
}
