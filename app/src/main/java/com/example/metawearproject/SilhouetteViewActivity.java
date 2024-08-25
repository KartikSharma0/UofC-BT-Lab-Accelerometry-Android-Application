package com.example.metawearproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SilhouetteViewActivity extends AppCompatActivity {

    Button leftArm;
    Button rightArm;
    Button leftLeg;
    Button rightLeg;
    Button timeAhead;
    Button timeBack;
    TextView timeStamp;

    int buttonClickCount = 0;
    int dataEntries = 0;

    // This array list will hold data samples as they are read form CSV files:
    private List<PatientData> collectedData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_silhouette_view);

        leftArm = findViewById(R.id.leftArm);
        rightArm = findViewById(R.id.rightArm);
        timeAhead = findViewById(R.id.timeAhead);
        timeBack = findViewById(R.id.timeBack);
        timeStamp = findViewById(R.id.timeStamp);

        try {
            readPatientData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataEntries = collectedData.size();

        // Setting the functionality for "increase time" button:
        timeAhead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(buttonClickCount < dataEntries){
                    setColorAndTime(buttonClickCount);
                    ++buttonClickCount;
                }
            }
        });

        // Setting the functionality for "decrease time" button:
        timeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonClickCount > 0){
                    --buttonClickCount;
                    //timeStamp.setText(get12h(collectedData.get(buttonClickCount).getTime()));
                    setColorAndTime(buttonClickCount);

                }
            }
        });

    }

    public void readPatientData() throws IOException {
        // Declaring CSV file intended to be read from (app_synth_data):
        InputStream readDataCollected = getResources().openRawResource(R.raw.app_synth_data);
        BufferedReader reader = new BufferedReader(new InputStreamReader(readDataCollected, Charset.forName("UTF-8")));
        //9:30
        String input = "";
        reader.readLine();  // skipping first line of CSV file as it only contains text
        while ((input = reader.readLine()) != null){
            String[] dataEntry = input.split(",");  // Splitting values of each line using the comma as delimiter
            PatientData dataLine = new PatientData(Integer.parseInt(dataEntry[0]),Double.parseDouble(dataEntry[1]),Double.parseDouble(dataEntry[2]));
            collectedData.add(dataLine);    // Adding the line of data we just read into the arraylist
        }
    }

    public String get12h(int time24h){
        if(time24h <= 12){
            return time24h + " AM";
        }
        else if(time24h > 12){
            return (time24h-12) + " PM";
        }
        else{
            return"";
        }
    }

    public void setColorAndTime(int buttonClickCount){
        double left = collectedData.get(buttonClickCount).getLeftAccel();
        double right = collectedData.get(buttonClickCount).getRightAccel();

        double ratio = left/right;

        timeStamp.setText(get12h(collectedData.get(buttonClickCount).getTime()));

        if(ratio > 0.9 && ratio < 1.1){
            leftArm.setBackgroundColor(Color.parseColor("green"));
            rightArm.setBackgroundColor(Color.parseColor("green"));
        }

        else if(ratio>1.1 && ratio<1.3){
            leftArm.setBackgroundColor(Color.parseColor("green"));
            leftArm.setBackgroundColor(Color.parseColor("#d98226"));
        }

        else if(ratio>1.3){
            leftArm.setBackgroundColor(Color.parseColor("green"));
            leftArm.setBackgroundColor(Color.parseColor("red"));
        }

    }
}