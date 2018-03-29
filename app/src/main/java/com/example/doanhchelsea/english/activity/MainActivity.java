package com.example.doanhchelsea.english.activity;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.doanhchelsea.english.R;
import com.example.doanhchelsea.english.adapter.CategoryAdapter;
import com.example.doanhchelsea.english.adapter.LevelAdapter;
import com.example.doanhchelsea.english.model.CategoryModel;
import com.example.doanhchelsea.english.model.LevelModel;
import com.example.doanhchelsea.english.sqlite.Databases;

import java.io.IOException;
import java.util.ArrayList;
import java.lang.String;

public class MainActivity extends AppCompatActivity {
    ListView lvCategory;
    ListView lvTopic;
    ArrayList<CategoryModel> catelogyModels;
    ArrayList<LevelModel> levelModels = new ArrayList<>();
    Databases databases;
    int selectedLevel = 1;
    LevelAdapter topicAdapter;
    int selectedCate = 1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databases = new Databases(this);
        setUpDb();
        addCatelogy();
        addLevel();

    }

    private void setUpDb() {
        try {

            databases.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            databases.openDataBase();

        }catch(SQLException sqle){

            throw sqle;

        }
    }

    private void addCatelogy() {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //
        catelogyModels = databases.getCate();
        getSupportActionBar().setTitle(catelogyModels.get(0).getTitleCatelogy());
        lvCategory = findViewById(R.id.list_item1);
        CategoryAdapter catelogyAdapter = new CategoryAdapter(this, R.layout.item_category, catelogyModels);
        lvCategory.setAdapter(catelogyAdapter);
        lvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                toolbar.setTitle(catelogyModels.get(i).getTitleCatelogy());
                if (i+1 != selectedLevel) {
                    selectedCate = i+1;
                    addLevel();
                }
                drawer.closeDrawers();
            }
        });
    }
    private void addLevel() {

        levelModels = databases.getTopics(selectedCate);
        lvTopic = findViewById(R.id.list_viewTopic);
        topicAdapter = new LevelAdapter(this, R.layout.item_topic,levelModels);
        lvTopic.setAdapter(topicAdapter);
        lvTopic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                intent.putExtra(QuestionActivity.KEY_TITLE, String.valueOf(levelModels.get(i)));
                intent.putExtra("ID", levelModels.get(i).getId());
                intent.putExtra("level_score_id", levelModels.get(i).getLevelscore_id());
                intent.putExtra("score",levelModels.get(i).getScore());
                selectedLevel = i;
                startActivityForResult(intent,0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                int d = data.getIntExtra("score", 0);
                if (d > levelModels.get(selectedLevel).getScore()) {
                    levelModels.get(selectedLevel).setScore(d);
                    topicAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
