package com.example.doanhchelsea.english.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.doanhchelsea.english.model.CategoryModel;
import com.example.doanhchelsea.english.model.OptionModel;
import com.example.doanhchelsea.english.model.QuestionModel;
import com.example.doanhchelsea.english.model.LevelModel;
import com.example.doanhchelsea.english.model.ResultModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by doanhchelsea on 17/03/2018.
 */

public class Databases extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/com.example.doanhchelsea.english/databases/";

    private static String DB_NAME = "english_quiz.db";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    public Databases(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public ArrayList<CategoryModel> getCate() {
        ArrayList<CategoryModel> categoryModels = new ArrayList<>();
        Cursor res = myDataBase.rawQuery("select * from categories", null);
        while (res.moveToNext()) {
            String id = res.getInt(res.getColumnIndex("id")) + "";
            String name = res.getString(res.getColumnIndex("name"));
            categoryModels.add(new CategoryModel(name, id));
        }
        res.close();
        return categoryModels;
    }

    public ArrayList<LevelModel> getTopics(int cate_id) {
        ArrayList<LevelModel> topicModels = new ArrayList<>();
//        Cursor res = myDataBase.rawQuery("select categories.id, level.categories_id,  level.name from categories inner join level on categories.id = level.categories_id where categories.categories_id = " + cate_id, null);
        Cursor res = myDataBase.rawQuery("select level.categories_id, level.name, level.id, level_score.score, level_score.id as levelID from level left join level_score on level_score.level_id = level.id inner join categories on level.categories_id = categories.id where categories.id = "+cate_id, null);
        while (res.moveToNext()) {
            int id = res.getInt(res.getColumnIndex("id"));
            int idCate = res.getInt(res.getColumnIndex("categories_id"));
            String name = res.getString(res.getColumnIndex("name"));
            ArrayList<QuestionModel> questionModels = getQuestion(id);
            LevelModel levelModel = new LevelModel(name, id, idCate);
            Integer i = res.getInt(res.getColumnIndex("score"));
            Integer lID = res.getInt(res.getColumnIndex("levelID"));
            levelModel.setScore(i);
            levelModel.setLevelscore_id(lID);
            levelModel.setSl(questionModels.size());
            topicModels.add(levelModel);
        }
        res.close();
        return topicModels;
    }

    public ArrayList<QuestionModel> getQuestion(int id_que) {

        ArrayList<QuestionModel> questionModelArrayList = new ArrayList<>();
        Cursor res = myDataBase.rawQuery("select * from question where id in ( select question_id from level_question where level_id = " + id_que +" );", null);



        while (res.moveToNext()) {
            QuestionModel questionModel = new QuestionModel();
            questionModel.setId(res.getInt(0)) ;
            questionModel.setContent(res.getString(1));
            questionModelArrayList.add(questionModel);
        }
        res.close();
        return questionModelArrayList;
    }

    public ArrayList<OptionModel> getOption(int id) {
        ArrayList<OptionModel> optionModelArrayList = new ArrayList<>();
        Cursor res = myDataBase.rawQuery("select option.id,option.question_id,option.content,option.correct from option inner join question on option.question_id = question.id where option.question_id = "+id, null);
        while (res.moveToNext()) {
            id = res.getInt(res.getColumnIndex("id"));
            int questionId = res.getInt(res.getColumnIndex("question_id"));
            String content = res.getString(res.getColumnIndex("content"));
            boolean correct = Boolean.parseBoolean(res.getString(res.getColumnIndex("correct")));
            optionModelArrayList.add(new OptionModel(id, questionId, content, correct));
        }
        res.close();
        return optionModelArrayList;
    }
    public ArrayList<ResultModel> getResult(int id){
        ArrayList<ResultModel> resultModelArrayList = new ArrayList<>();
        Cursor res = myDataBase.rawQuery("select question.id,question.content,option.content as answer,option.correct from question inner join option on option.question_id = question.id  where question.id in(select  level_question.question_id  from level_question where level_question.level_id = "+ id+ " ) and option.correct=1;",null);
        while (res.moveToNext()){
            id = res.getInt(res.getColumnIndex("id"));
            String content = res.getString(res.getColumnIndex("content"));
            String correctAnswer = res.getString(res.getColumnIndex("answer"));
            boolean correct = Boolean.parseBoolean(res.getString(res.getColumnIndex("correct")));
            resultModelArrayList.add(new ResultModel(id,content,correctAnswer,correct));

        }
        res.close();
        return  resultModelArrayList;
    }
    public void insertToDB(int lId, int score) {


            ContentValues values = new ContentValues();
            values.put("level_id", lId);
            values.put("score", score);
            myDataBase.insert("level_score", null, values);

    }
    public void updateLevelScore(int id ,int levelid,int score){
        ContentValues values = new ContentValues();
        values.put("score",score);
        myDataBase.update("level_score",values,"level_id = "+ levelid,null);
    }

//    public int getBestScorebyLevelID(int levelID) {
//
//        Cursor res = myDataBase.rawQuery("select* from level_score where level_id = "+levelID,null);
//
//        if (res.moveToFirst())
//            return res.getInt(2);
//        res.close();
//        return 0;
//    }
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {

        } else {

            this.getWritableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    public boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

        } catch (SQLiteException e) {


        }

        if (checkDB != null) {

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    private void copyDataBase() throws IOException {


        InputStream myInput = myContext.getAssets().open(DB_NAME);


        String outFileName = DB_PATH + DB_NAME;


        OutputStream myOutput = new FileOutputStream(outFileName);


        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }


        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {


        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

    }

    @Override
    public synchronized void close() {

        if (myDataBase != null)
            myDataBase.close();

        super.close();

    }


}
