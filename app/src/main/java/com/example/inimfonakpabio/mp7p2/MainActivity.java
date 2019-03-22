package com.example.inimfonakpabio.mp7p2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase db;
    static final int REQCODE = 1;
    ImageView imgDisplay;
    EditText idQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgDisplay = (ImageView) findViewById(R.id.imgDisplay);
        idQuery = (EditText) findViewById(R.id.idQuery);

        db = openOrCreateDatabase("Images", MODE_PRIVATE, null);

        String query = "CREATE TABLE IF NOT EXISTS Images ( "
                    +  "  id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +  "  image BLOB NOT NULL "
                    +  ");";

        db.execSQL(query);
    }


    public void TakePic(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQCODE);
    }

    public void LoadPic(View view) {
        int curId = 0;
        try {
            curId = Integer.parseInt(idQuery.getText().toString());
            curId = (curId >= 1) ? curId : 0;
        } catch (Exception e) {
            Log.d("NINI", "Could not convert text to int");
        }

        Cursor cr;
        byte[] bGet = null;
        Bitmap bitmap = null;

        try {
            cr = db.rawQuery("SELECT image FROM Images WHERE id = " + curId + ";", null);
            if (cr.moveToFirst()) {
                bGet = cr.getBlob(cr.getColumnIndex("image"));
            }
            cr.close();
            bitmap = BitmapFactory.decodeByteArray(bGet, 0, bGet.length);
        } catch (Exception e) {
            Log.d("NINI", "Couldn't et image from database");
        }

        imgDisplay.setImageBitmap(bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap img = null;
        try {
             img = (Bitmap) data.getExtras().get("data");
        } catch (Exception e) {
            Log.d("NINI", "Couldnt get image");
        }

        imgDisplay.setImageBitmap(img);

        //Store in db
        byte[] bArray;
        ContentValues cv;
        try {
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.PNG, 100, bStream);
            bArray = bStream.toByteArray();

            cv = new ContentValues();
            cv.put("image", bArray);

            if(img != null && !img.isRecycled()) {
                img = null;
            }

            db.insert("Images", null, cv);

        } catch(Exception e) {
            Log.d("NINI", "Couldnt compress image");
            e.printStackTrace();
        }
    }

}
