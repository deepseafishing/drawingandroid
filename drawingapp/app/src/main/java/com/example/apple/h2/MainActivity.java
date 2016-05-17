package com.example.apple.h2;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends Activity {
    EditText ne;
    String loadname;
    ArrayList<String> items;
    ArrayAdapter<String> adapter;
    View bs;
    View bl;
    View inv;
    View wv;
    private View v;
    ArrayList<Pathpoints> paths;
    ArrayList<Vertex> arVertex = new ArrayList<Vertex>();
    Pathpoints pathpoints;
    ArrayList<Pathpoints> deletepaths;

    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable("Points", paths);
    }

    public class Pathpoints implements Serializable{

        private static final long serialVersionUID = 100L;
        ArrayList<Vertex> pv;
        String p;
        String s;
        Pathpoints(ArrayList<Vertex> v, String ap, String as){
            pv = new ArrayList<Vertex>(v);
            p = ap;
            s = as;
        }
        private void writeObject(ObjectOutputStream stream) throws IOException
        {
            stream.writeObject(pv);
            stream.writeObject(p);
            stream.writeObject(s);
        }
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
        {
            pv = new ArrayList<Vertex>((ArrayList<Vertex>)stream.readObject());
            p = (String)stream.readObject();
            s = (String)stream.readObject();
        }

    }

    public class Vertex implements Serializable{
        private static final long serialVersionUID = 100L;
        float x;
        float y;
        boolean bdraw;

        Vertex(float ax, float ay, boolean ad) {
            x = ax;
            y = ay;
            bdraw = ad;
        }
        private void writeObject(ObjectOutputStream stream) throws IOException
        {
            stream.writeFloat(x);
            stream.writeFloat(y);
            stream.writeBoolean(bdraw);
        }

        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
        {
            x = stream.readFloat();
            y = stream.readFloat();
            bdraw = stream.readBoolean();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v = new MyView(this);
        v.setOnTouchListener((View.OnTouchListener) v);
        RelativeLayout r = (RelativeLayout) View.inflate(this, R.layout.activity_main, null);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
        p.addRule(RelativeLayout.BELOW, R.id.widgets);
        v.setId(R.id.canvas);
        r.addView(v, p);
        setContentView(r);
        inv = findViewById(R.id.search);
        wv = findViewById(R.id.widgets);
        bl = findViewById(R.id.button_load);
        bs = findViewById(R.id.button_save);
        if(savedInstanceState == null)
        {
            paths = new ArrayList<Pathpoints>();
        }else{
            paths = (ArrayList<Pathpoints>)savedInstanceState.getSerializable("Points");
        }
        findViewById(R.id.draw).setOnClickListener((View.OnClickListener) v);
        findViewById(R.id.delete).setOnClickListener((View.OnClickListener) v);
        findViewById(R.id.save).setOnClickListener((View.OnClickListener) v);
        findViewById(R.id.load).setOnClickListener((View.OnClickListener) v);
        findViewById(R.id.button_load).setOnClickListener((View.OnClickListener) v);
        findViewById(R.id.button_save).setOnClickListener((View.OnClickListener) v);
        ne = (EditText)findViewById(R.id.name);
        items = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                items);
        ListView list = (ListView)findViewById(R.id.list); list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = items.get(position);
                if (item.endsWith("/")) {
                    EditText e = ((EditText) findViewById(R.id.edit));
                    String current = e.getText().toString();
                    item = item.substring(0, item.length() - 1);
                    if (item.equals("..")) {
                        current = current.substring(0, current.lastIndexOf("/"));
                        if (current.equals("")) e.setText("/");
                        else e.setText(current);
                    } else {
                        if (current.equals("/")) e.setText(current + item);
                        else e.setText(current + "/" + item);
                    }
                    refresh();
                } else {
                    loadname = item;
                    Toast.makeText(MainActivity.this, "Press 'Load' button to load " + item, Toast.LENGTH_SHORT).show();
                }
            }
        });
        ((EditText)findViewById(R.id.edit)).setText(Environment.getExternalStorageDirectory().getAbsolutePath());
        refresh();
    }

    void refresh() {
        String current = ((EditText)findViewById(R.id.edit)).getText().toString(); File path = new File(current);
        String[] temp = path.list();
        items.clear();
        items.add("../"); if (temp!=null) {
            for (int i=0; i<temp.length; i++) {
                if ((new File(current+"/"+temp[i])).isDirectory()) items.add(temp[i]+"/"); else items.add(temp[i]);
            } }
        adapter.notifyDataSetChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class MyView extends View implements View.OnTouchListener, View.OnClickListener{

        int dw;
        String sc = "Pen";
        String wc = "Normal";
        String cc = "Blue";
        Paint circlePaint;
        Paint nbPaint;
        Paint ngPaint;
        Paint nrPaint;
        Paint tbPaint;
        Paint tgPaint;
        Paint trPaint;
        Paint ibPaint;
        Paint igPaint;
        Paint irPaint;

        public MyView(Context c){
            super(c);
            ngPaint = new Paint();
            ngPaint.setColor(Color.GREEN);
            ngPaint.setStrokeWidth(12);
            ngPaint.setAntiAlias(true);
            ngPaint.setDither(true);
            ngPaint.setStyle(Paint.Style.STROKE);
            ngPaint.setStrokeJoin(Paint.Join.ROUND);
            ngPaint.setStrokeCap(Paint.Cap.ROUND);

            nbPaint = new Paint();
            nbPaint.setColor(Color.BLUE);
            nbPaint.setStrokeWidth(12);
            nbPaint.setAntiAlias(true);
            nbPaint.setDither(true);
            nbPaint.setStyle(Paint.Style.STROKE);
            nbPaint.setStrokeJoin(Paint.Join.ROUND);
            nbPaint.setStrokeCap(Paint.Cap.ROUND);

            nrPaint = new Paint();
            nrPaint.setColor(Color.RED);
            nrPaint.setStrokeWidth(12);
            nrPaint.setAntiAlias(true);
            nrPaint.setDither(true);
            nrPaint.setStyle(Paint.Style.STROKE);
            nrPaint.setStrokeJoin(Paint.Join.ROUND);
            nrPaint.setStrokeCap(Paint.Cap.ROUND);

            tgPaint = new Paint();
            tgPaint.setColor(Color.GREEN);
            tgPaint.setStrokeWidth(24);
            tgPaint.setAntiAlias(true);
            tgPaint.setDither(true);
            tgPaint.setStyle(Paint.Style.STROKE);
            tgPaint.setStrokeJoin(Paint.Join.ROUND);
            tgPaint.setStrokeCap(Paint.Cap.ROUND);

            tbPaint = new Paint();
            tbPaint.setColor(Color.BLUE);
            tbPaint.setStrokeWidth(24);
            tbPaint.setAntiAlias(true);
            tbPaint.setDither(true);
            tbPaint.setStyle(Paint.Style.STROKE);
            tbPaint.setStrokeJoin(Paint.Join.ROUND);
            tbPaint.setStrokeCap(Paint.Cap.ROUND);

            trPaint = new Paint();
            trPaint.setColor(Color.RED);
            trPaint.setStrokeWidth(24);
            trPaint.setAntiAlias(true);
            trPaint.setDither(true);
            trPaint.setStyle(Paint.Style.STROKE);
            trPaint.setStrokeJoin(Paint.Join.ROUND);
            trPaint.setStrokeCap(Paint.Cap.ROUND);

            igPaint = new Paint();
            igPaint.setColor(Color.GREEN);
            igPaint.setStrokeWidth(6);
            igPaint.setAntiAlias(true);
            igPaint.setDither(true);
            igPaint.setStyle(Paint.Style.STROKE);
            igPaint.setStrokeJoin(Paint.Join.ROUND);
            igPaint.setStrokeCap(Paint.Cap.ROUND);

            ibPaint = new Paint();
            ibPaint.setColor(Color.BLUE);
            ibPaint.setStrokeWidth(6);
            ibPaint.setAntiAlias(true);
            ibPaint.setDither(true);
            ibPaint.setStyle(Paint.Style.STROKE);
            ibPaint.setStrokeJoin(Paint.Join.ROUND);
            ibPaint.setStrokeCap(Paint.Cap.ROUND);

            irPaint = new Paint();
            irPaint.setColor(Color.RED);
            irPaint.setStrokeWidth(6);
            irPaint.setAntiAlias(true);
            irPaint.setDither(true);
            irPaint.setStyle(Paint.Style.STROKE);
            irPaint.setStrokeJoin(Paint.Join.ROUND);
            irPaint.setStrokeCap(Paint.Cap.ROUND);

            circlePaint = new Paint();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLACK);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(2f);

        }


        public Paint getPaint(String ap)
        {
            switch (ap)
            {
                case "ThinBlue":
                    return ibPaint;
                case "ThinGreen":
                    return igPaint;
                case "ThinRed":
                    return irPaint;

                case "NormalBlue":
                    return nbPaint;
                case "NormalGreen":
                    return ngPaint;
                case "NormalRed":
                    return nrPaint;

                case "ThickBlue":
                    return tbPaint;
                case "ThickGreen":
                    return tgPaint;
                case "ThickRed":
                    return trPaint;
                case "delete":
                    return circlePaint;
            }
            return nbPaint;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.LTGRAY);
            Paint rp;
            rp = getPaint(wc + cc);
            rp.setStyle(Paint.Style.FILL);

            for ( int i = 0; i < paths.size(); i++) {
                switch (paths.get(i).s){
                    case "Pen":
                    for (int j = 0; j < paths.get(i).pv.size(); j++) {
                        if (paths.get(i).pv.get(j).bdraw) {
                            canvas.drawLine(paths.get(i).pv.get(j - 1).x, paths.get(i).pv.get(j - 1).y,
                                    paths.get(i).pv.get(j).x, paths.get(i).pv.get(j).y, getPaint(paths.get(i).p));
                        }
                    }
                    break;
                    case "Rectangle":
                        rp = getPaint(paths.get(i).p);
                        rp.setStyle(Paint.Style.FILL);
                        canvas.drawRect(paths.get(i).pv.get(0).x, paths.get(i).pv.get(0).y,
                                paths.get(i).pv.get(paths.get(i).pv.size()-1).x, paths.get(i).pv.get(paths.get(i).pv.size()-1).y, rp);
                        break;
                    case "Ellipse":
                        rp = getPaint(paths.get(i).p);
                        rp.setStyle(Paint.Style.FILL);
                        canvas.drawOval(paths.get(i).pv.get(0).x, paths.get(i).pv.get(0).y,
                                paths.get(i).pv.get(paths.get(i).pv.size() - 1).x, paths.get(i).pv.get(paths.get(i).pv.size() - 1).y, rp);
                        break;
                    case "Line":
                        canvas.drawLine(paths.get(i).pv.get(0).x, paths.get(i).pv.get(0).y,
                                paths.get(i).pv.get(paths.get(i).pv.size() - 1).x, paths.get(i).pv.get(paths.get(i).pv.size() - 1).y, getPaint(paths.get(i).p));
                        break;

                }

            }
            if(sc == "delete"){
                deletepaths = new ArrayList<Pathpoints>();
                for (int k = 0; k < arVertex.size(); k++) {
                    canvas.drawCircle(arVertex.get(arVertex.size() - 1).x, arVertex.get(arVertex.size() - 1).y, dw, circlePaint);
                    for ( int i = 0; i < paths.size(); i++) {
                        for (int j = 0; j < paths.get(i).pv.size(); j++) {
                            if((paths.get(i).pv.get(j).x > (arVertex.get(arVertex.size()-1).x - dw)) &&
                                    (paths.get(i).pv.get(j).x < (arVertex.get(arVertex.size()-1).x + dw))
                                    && (paths.get(i).pv.get(j).y > (arVertex.get(arVertex.size()-1).y - dw))
                                    && (paths.get(i).pv.get(j).y < (arVertex.get(arVertex.size()-1).y + dw)))
                            {
                                deletepaths.add(paths.get(i));
                            }
                        }
                }
                }
                paths.removeAll(deletepaths);
                deletepaths = new ArrayList<Pathpoints>();
            }

            if (sc == "Pen"){
                for (int i = 0; i < arVertex.size(); i++) {

                    if (arVertex.get(i).bdraw) {
                        canvas.drawLine(arVertex.get(i - 1).x, arVertex.get(i - 1).y,
                                arVertex.get(i).x, arVertex.get(i).y, getPaint(wc+cc));
                    }
                }

            }

            if(sc == "Rectangle") {
                for (int i = 0; i < arVertex.size(); i++) {
                   canvas.drawRect(arVertex.get(0).x, arVertex.get(0).y, arVertex.get(arVertex.size() - 1).x, arVertex.get(arVertex.size() - 1).y, rp);
                }
            }

            if(sc == "Line") {
                for (int i = 0; i < arVertex.size(); i++) {
                    canvas.drawLine(arVertex.get(0).x, arVertex.get(0).y, arVertex.get(arVertex.size() - 1).x, arVertex.get(arVertex.size() - 1).y, getPaint(wc+cc));
                }
            }

            if(sc == "Ellipse") {
                for (int i = 0; i < arVertex.size(); i++) {
                    canvas.drawOval(arVertex.get(0).x, arVertex.get(0).y, arVertex.get(arVertex.size() - 1).x, arVertex.get(arVertex.size() - 1).y, rp);
                }
            }
        }

        public boolean onTouch(View v, MotionEvent event){
//            switch (sc) {
//                case "Pen":
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        arVertex.add(new Vertex(event.getX(), event.getY(), false));
//                        return true;
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        arVertex.add(new Vertex(event.getX(), event.getY(), true));
//                        invalidate();
//                        return true;
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        pathpoints = new Pathpoints(arVertex, wc+cc, sc);
//                        paths.add(pathpoints);
//                        arVertex = new ArrayList<Vertex>();
//                        invalidate();
//                        return true;
//                    }
//                    break;
//                case "Rectangle":
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        arVertex.add(new Vertex(event.getX(), event.getY(), false));
//                        return true;
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        arVertex.add(new Vertex(event.getX(), event.getY(), true));
//                        invalidate();
//                        return true;
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        pathpoints = new Pathpoints(arVertex, wc+cc, sc);
//                        paths.add(pathpoints);
//                        arVertex = new ArrayList<Vertex>();
//                        invalidate();
//                        return true;
//                    }
//                    break;
//                case "Ellipse":
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        arVertex.add(new Vertex(event.getX(), event.getY(), false));
//                        return true;
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        arVertex.add(new Vertex(event.getX(), event.getY(), true));
//                        invalidate();
//                        return true;
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        pathpoints = new Pathpoints(arVertex, wc+cc, sc);
//                        paths.add(pathpoints);
//                        arVertex = new ArrayList<Vertex>();
//                        invalidate();
//                        return true;
//                    }
//                    break;
//                case "Line":
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        arVertex.add(new Vertex(event.getX(), event.getY(), false));
//                        return true;
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        arVertex.add(new Vertex(event.getX(), event.getY(), true));
//                        invalidate();
//                        return true;
//                    }
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        Vertex bv = new Vertex(arVertex.get(0).x, arVertex.get(0).y, true);
//                        Vertex ev = new Vertex(arVertex.get(arVertex.size()-1).x, arVertex.get(arVertex.size()-1).y, true);
//                        arVertex = new ArrayList<Vertex>();
//                        for (int i = 0; i < )
//                        pathpoints = new Pathpoints(arVertex, wc+cc, sc);
//                        paths.add(pathpoints);
//                        arVertex = new ArrayList<Vertex>();
//                        invalidate();
//                        return true;
//                    }
//                    break;
//            }
            if (sc == "delete"){
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    arVertex.add(new Vertex(event.getX(), event.getY(), false));
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    arVertex.add(new Vertex(event.getX(), event.getY(), true));
                    invalidate();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    arVertex = new ArrayList<Vertex>();
                    invalidate();
                    return true;
                }

            }else{
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    arVertex.add(new Vertex(event.getX(), event.getY(), false));
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    arVertex.add(new Vertex(event.getX(), event.getY(), true));
                    invalidate();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    pathpoints = new Pathpoints(arVertex, wc+cc, sc);
                    paths.add(pathpoints);
                    arVertex = new ArrayList<Vertex>();
                    invalidate();
                    return true;
                }
            }
            return false;
        }

        public void onClick(View v){
            switch (v.getId())
            {
                case R.id.button_save:
                    String savename = "";
                    savename= ne.getText().toString();
                    if(savename==""){
                        Toast.makeText(MainActivity.this, "Please write the filename that ends with .txt to save", Toast.LENGTH_SHORT).show();

                    }else{
                        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
                        String filename = dir + "/"+savename;
                        try{
                            FileOutputStream fileOutputStream = new FileOutputStream(filename);
                            ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
                            oos.writeObject(paths);
                            fileOutputStream.close();
                            wv.setVisibility(View.VISIBLE);
                            findViewById(R.id.canvas).setVisibility(View.VISIBLE);
                            inv.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Saved as " + ne.getText().toString(), Toast.LENGTH_SHORT).show();

                        }
                        catch (IOException x){
                            Toast.makeText(MainActivity.this, "Please write the filename that ends with .txt to save", Toast.LENGTH_SHORT).show();
                            x.printStackTrace();
                        }

                    }
                    break;
                case R.id.button_load:
                    String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String filename = dir + "/" + loadname;
                    if(loadname == ""){
                        Toast.makeText(MainActivity.this, "Please click the filename that ends with .txt to load", Toast.LENGTH_SHORT).show();
                    }else {
                        try{
                            FileInputStream fileInputStream = new FileInputStream(filename);
                            ObjectInputStream ois = new ObjectInputStream(fileInputStream);
                            paths = new ArrayList<Pathpoints>((ArrayList<Pathpoints>) ois.readObject());
                            fileInputStream.close();
                            wv.setVisibility(View.VISIBLE);
                            findViewById(R.id.canvas).setVisibility(View.VISIBLE);
                            inv.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, loadname + " is now loaded", Toast.LENGTH_SHORT).show();
                            loadname = "";
                            invalidate();
                        }
                        catch (IOException x){
                            Toast.makeText(MainActivity.this, "ERROR: File writing", Toast.LENGTH_SHORT).show();
                            x.printStackTrace();
                        }
                        catch(ClassNotFoundException x){
                            Toast.makeText(MainActivity.this, "ERROR: Class not found"+ x.toString(), Toast.LENGTH_SHORT).show();
                            x.printStackTrace();
                        }

                    }
                    break;
                case R.id.save:
                    wv.setVisibility(View.INVISIBLE);
                    findViewById(R.id.canvas).setVisibility(View.INVISIBLE);
                    inv.setVisibility(View.VISIBLE);
                    bs.setVisibility(View.VISIBLE);
                    ne.setVisibility(View.VISIBLE);
                    bl.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Press 'Save as' button after name entered", Toast.LENGTH_SHORT).show();
                    refresh();
                    break;
                case R.id.load:
                    wv.setVisibility(View.INVISIBLE);
                    findViewById(R.id.canvas).setVisibility(View.INVISIBLE);
                    inv.setVisibility(View.VISIBLE);
                    bl.setVisibility(View.VISIBLE);
                    bs.setVisibility(View.INVISIBLE);
                    ne.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Press the .txt filename you want to load", Toast.LENGTH_LONG).show();
                    refresh();
                    break;
                case R.id.delete:
                    PopupMenu popup = new PopupMenu(MainActivity.this,v);
                    Menu menu = popup.getMenu();
                    popup.getMenuInflater().inflate(R.menu.deleting, menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId())
                            {
                                case R.id.small:
                                    sc = "delete";
                                    dw = 50;
                                    return true;
                                case R.id.medium:
                                    sc = "delete";
                                    dw = 100;
                                    return true;
                                case R.id.large:
                                    sc = "delete";
                                    dw = 150;
                                    return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    break;
                case R.id.draw:

                    PopupMenu dpopup = new PopupMenu(MainActivity.this,v);
                    Menu dmenu = dpopup.getMenu();
                    dpopup.getMenuInflater().inflate(R.menu.drawing, dmenu);
                    dpopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.blue:
                                    cc = "Blue";
                                    return true;
                                case R.id.green:
                                    cc = "Green";
                                    return true;
                                case R.id.red:
                                    cc = "Red";
                                    return true;
                                case R.id.thick:
                                    wc = "Thick";
                                    return true;
                                case R.id.normal:
                                    wc = "Normal";
                                    return true;
                                case R.id.thin:
                                    wc = "Thin";
                                    return true;
                                case R.id.line:
                                    sc = "Line";
                                    return true;
                                case R.id.rectangle:
                                    sc = "Rectangle";
                                    return true;
                                case R.id.ellipse:
                                    sc = "Ellipse";
                                    return true;
                                case R.id.pen:
                                    sc = "Pen";
                                    return true;

                            }
                            return false;
                        }
                    });
                    dmenu.findItem(R.id.shape).setTitle("Shape : " + sc);
                    dmenu.findItem(R.id.width).setTitle("Width : "+ wc);
                    dmenu.findItem(R.id.color).setTitle("Color : "+ cc);

                    dpopup.show();
                    break;
            }
        }
    }
}
