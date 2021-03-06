package com.example.fatani.medizone_v2;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.fatani.medizone_v2.Db.dataPatients;
import static com.example.fatani.medizone_v2.LoginScreen.getTextBetweenTwoWords;

public class MainActivity extends AppCompatActivity {
    Integer doctorId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //declaring element variables
        //TextView variables
        TextView name = findViewById(R.id.txtFname);
        TextView age = findViewById(R.id.txtAge);
        TextView ethloc = findViewById(R.id.txtEthLoc);
        TextView weight = findViewById(R.id.txtWeight);
        TextView height = findViewById(R.id.txtHeight);
        TextView blood = findViewById(R.id.txtBloodType);
        TextView familyContact = findViewById(R.id.txtFamilyContact);
        TextView familyLocation = findViewById(R.id.txtFamilyLocation);
        //ImageView variables
        ImageView patientImage = findViewById(R.id.imgPatientFirst);
        //Collecting values from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Integer value = extras.getInt("EXTRA_PATIENT_ID");
            //Retrieving patient file from the hashtable
            Patient patient_id = dataPatients.get(value);
            //setting the element based on the values from the database
            name.setText(patient_id.fName + " " + patient_id.lName);
            ethloc.setText(patient_id.getEthnicity() + " - " + patient_id.getLocation());
            age.setText(patient_id.getDob());
            weight.setText(patient_id.getWeight());
            height.setText(patient_id.getHeight());
            blood.setText(patient_id.getBloodType());
            familyContact.setText(patient_id.getFamilyContact());
            familyLocation.setText(patient_id.getFamilyLocation());
            String imgName = patient_id.getfName();
            doctorId = extras.getInt("EXTRA_DOCTOR_ID");
            //choosing the right profile image, these images are implemented into the application.
            if (value == 1) {
                patientImage.setImageDrawable(getDrawable(R.drawable.samantha));
            } else if (value == 2) {
                patientImage.setImageDrawable(getDrawable(R.drawable.akber));
            } else if (value == 3) {
                patientImage.setImageDrawable(getDrawable(R.drawable.daniel));
            } else if (value == 4) {
                patientImage.setImageDrawable(getDrawable(R.drawable.mason));
            }
            //The response from emotion analysis
            String s = extras.getString("EXTRA_EMOTION_RESPONSE");
            // Final Variables that are used to go decipher the response.
            final String status_complete, status_uncomplete, status_progress, getId, anger, disgust, fear, joy, sadness, surprise;
            Log.i("TESTING", s);
            status_complete = "\"status_message\":\"Complete\",";
            status_uncomplete = "emotions\":{\"anger\":";
            status_progress = "\"status_message\":\"In Progress\"";
            getId = getTextBetweenTwoWords("{\"id\":\"", "\",\"media_info\":", s);
            //Button declared, used to refresh the emotion graph.
            Button c = findViewById(R.id.btnRefresh);
            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    refreshEmotion(getId);
                }
            });
            //This function checks the status of the response if successful then show graph
            if (s.toLowerCase().indexOf(status_complete.toLowerCase()) != -1 && s.toLowerCase().indexOf(status_uncomplete.toLowerCase()) != -1 && s.toLowerCase().indexOf(status_progress.toLowerCase()) == -1 ) {
                anger = getTextBetweenTwoWords("{\"anger\":", ",\"disgust\":", s);
                disgust = getTextBetweenTwoWords(",\"disgust\":", ",\"fear\":", s);
                fear = getTextBetweenTwoWords(",\"fear\":", ",\"joy\":", s);
                joy = getTextBetweenTwoWords(",\"joy\":", ",\"sadness\":", s);
                sadness = getTextBetweenTwoWords(",\"sadness\":", ",\"surprise\":", s);
                surprise = getTextBetweenTwoWords(",\"surprise\":", "},\"tracking\"", s);
                BarChart chart = (BarChart) findViewById(R.id.chart);
                //bar entries
                List<BarEntry> entries = new ArrayList<>();
                entries.add(new BarEntry(0f, Float.parseFloat(anger)));
                entries.add(new BarEntry(1f, Float.parseFloat(disgust)));
                entries.add(new BarEntry(2f, Float.parseFloat(fear)));
                entries.add(new BarEntry(3f, Float.parseFloat(joy)));
                entries.add(new BarEntry(4f, Float.parseFloat(sadness)));
                entries.add(new BarEntry(5f, Float.parseFloat(surprise)));

                BarDataSet ange = new BarDataSet(entries, "");
                ange.setColors(ColorTemplate.VORDIPLOM_COLORS);

                BarData data = new BarData(ange);
                data.setBarWidth(0.9f); // set custom bar width

                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setTextSize(10f);
                xAxis.setTextColor(Color.RED);
                xAxis.setDrawAxisLine(true);
                xAxis.setDrawGridLines(false);

                String[] values = new String[]{"Anger", "Disgust", "Fear", "Joy", "Sadness", "Surprise"};

                xAxis.setValueFormatter(new MainActivity.MyXAxisValueFormatter(values));
                chart.setData(data);
                chart.setFitBars(true);
                chart.getDescription().setEnabled(false);
                chart.invalidate(); // refresh
            }
        }

        logoActionBar();
        //setting up the tabs
        TabHost tabs = (TabHost) findViewById(R.id.tabHost);
        tabs.setup();
        //Facial Analysis Tab
        TabHost.TabSpec calculatorTab = tabs.newTabSpec("Facial Analysis");
        calculatorTab.setContent(R.id.FacialAnalysis);
        calculatorTab.setIndicator("Facial Analysis");
        tabs.addTab(calculatorTab);

        //Overall Emotion Tab
        TabHost.TabSpec homeTab = tabs.newTabSpec("Overall Emotion");
        homeTab.setContent(R.id.OverallEmotion);
        homeTab.setIndicator("Overall Emotion");
        tabs.addTab(homeTab);
        //post method used to post to web-server
        HashMap postData = new HashMap();
        //(Post-name, Value)
        postData.put("emotionImage", "patient1.jpg");
        PostResponseAsyncTask taskInsert = new PostResponseAsyncTask(MainActivity.this, postData, new AsyncResponse() {
            @Override
            public void processFinish(String s) {
                LineChart chart2 = findViewById(R.id.chart2);
                String status_complete, status_uncomplete;
                Log.i("TESTING", s);
                String response = s;
                status_complete = "\"status_message\":\"Complete\",";
                status_uncomplete = "emotions\":{\"anger\":";

                List<Entry> angerEntries = new ArrayList<>();
                List<Entry> fearEntries = new ArrayList<>();
                List<Entry> entries3 = new ArrayList<>();
                List<Entry> entries4 = new ArrayList<>();
                List<Entry> entries5 = new ArrayList<>();
                List<Entry> entries6 = new ArrayList<>();

                Integer a = s.indexOf("\"emotions\":{");
                Float xPoints = 0f;
                while (a >= 0){
                    String text = s.substring(a);
                    Log.i("TESTING", text);
                    Integer anger = Integer.parseInt(getTextBetweenTwoWords("{\"anger\":", ",\"disgust\":", text));
                    Integer disgust = Integer.parseInt(getTextBetweenTwoWords(",\"disgust\":", ",\"fear\":", text));
                    Integer fear = Integer.parseInt(getTextBetweenTwoWords(",\"fear\":", ",\"joy\":", text));
                    Integer joy = Integer.parseInt(getTextBetweenTwoWords(",\"joy\":", ",\"sadness\":", text));
                    Integer sadness = Integer.parseInt(getTextBetweenTwoWords(",\"sadness\":", ",\"surprise\":", text));
                    Integer  surprise = Integer.parseInt(getTextBetweenTwoWords(",\"surprise\":", "},\"tracking\"", text));
                    System.out.println(fear);

                    angerEntries.add(new Entry(xPoints, anger));
                    fearEntries.add(new Entry(xPoints, disgust));
                    entries3.add(new Entry(xPoints, fear));
                    entries4.add(new Entry(xPoints, joy));
                    entries5.add(new Entry(xPoints, sadness));
                    entries6.add(new Entry(xPoints, surprise));
                    xPoints = xPoints +1 ;
                    a = s.indexOf("\"emotions\":{", a + 1);
                }

                LineDataSet ange = new LineDataSet(angerEntries, "Anger");
                ange.setColor(Color.parseColor("#f44242"));

                LineDataSet disgus = new LineDataSet(fearEntries, "Disgust");
                disgus.setColor(Color.parseColor("#f49b41"));

                LineDataSet fea = new LineDataSet(entries3, "Fear");
                fea.setColor(Color.parseColor("#e541f4"));

                LineDataSet jo = new LineDataSet(entries4, "Joy");
                jo.setColor(Color.parseColor("#43f441"));

                LineDataSet sadnes = new LineDataSet(entries5, "Sadness");
                sadnes.setColor(Color.parseColor("#41f4d9"));

                LineDataSet surpris = new LineDataSet(entries6, "Surprise");
                surpris.setColor(Color.parseColor("#41e2f4"));

                LineData data2 = new LineData(ange, disgus, fea, jo, sadnes, surpris);

                XAxis xAxis = chart2.getXAxis();
                xAxis.setGridLineWidth(5f);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setTextSize(10f);
                xAxis.setTextColor(Color.RED);
                xAxis.setDrawAxisLine(true);
                xAxis.setDrawGridLines(false);

                String[] values = new String[] {"Dec","Jan","Feb","March","April","May"};
                xAxis.setValueFormatter(new MainActivity.MyXAxisValueFormatter(values));

                chart2.setData(data2);
                chart2.setTouchEnabled(false);
                chart2.getDescription().setEnabled(false);
                chart2.invalidate(); // refresh

                if (s.toLowerCase().indexOf(status_complete.toLowerCase()) != -1 && s.toLowerCase().indexOf(status_uncomplete.toLowerCase()) != -1 ) {

                } else {
                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                }
            }
        });
        taskInsert.execute("http://fatanidev.com/demo/insert.php");

        //2nd graph
        HashMap postDatas = new HashMap();
        postDatas.put("emotionImage2", "patient1.jpg");
        PostResponseAsyncTask taskInsert2 = new PostResponseAsyncTask(MainActivity.this, postDatas, new AsyncResponse() {
            @Override
            public void processFinish(String s) {
                LineChart chart2 = findViewById(R.id.chart3);
                String status_complete, status_uncomplete;
                Log.i("TESTING", s);
                status_complete = "\"status_message\":\"Complete\",";
                status_uncomplete = "emotions\":{\"anger\":";
                List<Entry> angerEntries = new ArrayList<>();
                List<Entry> fearEntries = new ArrayList<>();
                List<Entry> entries3 = new ArrayList<>();
                List<Entry> entries4 = new ArrayList<>();
                List<Entry> entries5 = new ArrayList<>();
                List<Entry> entries6 = new ArrayList<>();
                Integer a = s.indexOf("\"emotions\":{");
                Float xPoints = 0f;
                while (a >= 0){
                    String text = s.substring(a);
                    Log.i("TESTING", text);
                    Integer anger = Integer.parseInt(getTextBetweenTwoWords("{\"anger\":", ",\"disgust\":", text));
                    Integer disgust = Integer.parseInt(getTextBetweenTwoWords(",\"disgust\":", ",\"fear\":", text));
                    Integer fear = Integer.parseInt(getTextBetweenTwoWords(",\"fear\":", ",\"joy\":", text));
                    Integer joy = Integer.parseInt(getTextBetweenTwoWords(",\"joy\":", ",\"sadness\":", text));
                    Integer sadness = Integer.parseInt(getTextBetweenTwoWords(",\"sadness\":", ",\"surprise\":", text));
                    Integer  surprise = Integer.parseInt(getTextBetweenTwoWords(",\"surprise\":", "},\"tracking\"", text));
                    System.out.println(fear);

                    angerEntries.add(new Entry(xPoints, anger));
                    fearEntries.add(new Entry(xPoints, disgust));
                    entries3.add(new Entry(xPoints, fear));
                    entries4.add(new Entry(xPoints, joy));
                    entries5.add(new Entry(xPoints, sadness));
                    entries6.add(new Entry(xPoints, surprise));
                    xPoints = xPoints +1 ;

                    a = s.indexOf("\"emotions\":{", a + 1);
                }
                LineDataSet ange = new LineDataSet(angerEntries, "Anger");
                ange.setColor(Color.parseColor("#f44242"));

                LineDataSet disgus = new LineDataSet(fearEntries, "Disgust");
                disgus.setColor(Color.parseColor("#f49b41"));

                LineDataSet fea = new LineDataSet(entries3, "Fear");
                fea.setColor(Color.parseColor("#e541f4"));

                LineDataSet jo = new LineDataSet(entries4, "Joy");
                jo.setColor(Color.parseColor("#43f441"));

                LineDataSet sadnes = new LineDataSet(entries5, "Sadness");
                sadnes.setColor(Color.parseColor("#41f4d9"));

                LineDataSet surpris = new LineDataSet(entries6, "Surprise");
                surpris.setColor(Color.parseColor("#41e2f4"));

                LineData data2 = new LineData(ange, disgus, fea, jo, sadnes, surpris);

                XAxis xAxis = chart2.getXAxis();
                xAxis.setGridLineWidth(5f);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setTextSize(10f);
                xAxis.setTextColor(Color.RED);
                xAxis.setDrawAxisLine(true);
                xAxis.setDrawGridLines(false);

                String[] values = new String[] {"Jun","Jul","Aug","Sep","Oct","Nov"};
                xAxis.setValueFormatter(new MainActivity.MyXAxisValueFormatter(values));

                chart2.setData(data2);
                chart2.setTouchEnabled(false);
                chart2.getDescription().setEnabled(false);
                chart2.invalidate(); // refresh

                if (s.toLowerCase().indexOf(status_complete.toLowerCase()) != -1 && s.toLowerCase().indexOf(status_uncomplete.toLowerCase()) != -1 ) {

                } else {
                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                }
            }
        });
        taskInsert2.execute("http://fatanidev.com/demo/insert.php");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            DialogFragment dialog = new BackPatientDialogFragment("Close Session", "Are you sure you want to cancel session with this patient?", doctorId);
            dialog.show(getFragmentManager(), "MyDialogFragmentTag");
        }
        return true;
    }

    public void logoActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);

        actionBar.setIcon(R.mipmap.med_launcher);
    }

    private void refreshEmotion(String getId) {
        HashMap postData = new HashMap();
        postData.put("mediaId", getId);
        PostResponseAsyncTask taskInsert = new PostResponseAsyncTask(MainActivity.this, postData, new AsyncResponse() {
            @Override
            public void processFinish(String s) {
                String status_complete, status_uncomplete, getId, anger, disgust, fear, joy, sadness, surprise;
                Log.i("TESTING", s);
                status_complete = "\"status_message\":\"Complete\",";
                status_uncomplete = "emotions\":{\"anger\":";
                if (s.toLowerCase().indexOf(status_complete.toLowerCase()) != -1 && s.toLowerCase().indexOf(status_uncomplete.toLowerCase()) != -1 ) {
                    anger = getTextBetweenTwoWords("{\"anger\":", ",\"disgust\":", s);
                    disgust = getTextBetweenTwoWords(",\"disgust\":", ",\"fear\":", s);
                    fear = getTextBetweenTwoWords(",\"fear\":", ",\"joy\":", s);
                    joy = getTextBetweenTwoWords(",\"joy\":", ",\"sadness\":", s);
                    sadness = getTextBetweenTwoWords(",\"sadness\":", ",\"surprise\":", s);
                    surprise = getTextBetweenTwoWords(",\"surprise\":", "},\"tracking\"", s );
                    BarChart chart = (BarChart) findViewById(R.id.chart);

                    List<BarEntry> entries = new ArrayList<>();
                    entries.add(new BarEntry(0f, Float.parseFloat(anger)));
                    entries.add(new BarEntry(1f, Float.parseFloat(disgust)));
                    entries.add(new BarEntry(2f, Float.parseFloat(fear)));
                    entries.add(new BarEntry(3f, Float.parseFloat(joy)));
                    entries.add(new BarEntry(4f, Float.parseFloat(sadness)));
                    entries.add(new BarEntry(5f, Float.parseFloat(surprise)));

                    BarDataSet ange = new BarDataSet(entries, "");
                    ange.setColors(ColorTemplate.VORDIPLOM_COLORS);

                    BarData data = new BarData(ange);

                    data.setBarWidth(0.9f); // set custom bar width
                    XAxis xAxis = chart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setTextSize(10f);
                    xAxis.setTextColor(Color.RED);
                    xAxis.setDrawAxisLine(true);
                    xAxis.setDrawGridLines(false);

                    String[] values = new String[] { "Anger", "Disgust", "Fear", "Joy", "Sadness", "Surprise"};
                    xAxis.setValueFormatter(new MainActivity.MyXAxisValueFormatter(values));

                    chart.setData(data);

                    chart.setFitBars(true); // make the x-axis fit exactly all bars
                    chart.invalidate(); // refresh
                } else {
                }
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });
        taskInsert.execute("http://fatanidev.com/demo/insert.php");
    }

    public class MyXAxisValueFormatter implements IAxisValueFormatter {
        private String[] mValues;
        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return mValues[(int) value];
        }
    }
}