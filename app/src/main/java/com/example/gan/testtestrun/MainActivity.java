package com.example.gan.testtestrun;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

import static com.example.gan.testtestrun.R.id.btnStart;
import static com.example.gan.testtestrun.R.id.default_activity_button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private GraphView graph;
    private LineGraphSeries<DataPoint> lineSeries;
    private static final Random RANDOM  = new Random();
    private int lastX = 0;
    private boolean isStop = true;
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graph = (GraphView)findViewById(R.id.graph);
        lineSeries = new LineGraphSeries<DataPoint>();
        graph.addSeries(lineSeries);
        Viewport viewPort = graph.getViewport();
        viewPort.setYAxisBoundsManual(true);
        viewPort.setXAxisBoundsManual(true);
        viewPort.setMaxY(10);
        viewPort.setMinY(0);
        viewPort.setMaxX(50);
        viewPort.setMinX(0);
        viewPort.setMaxXAxisSize(5);
        //viewPort.setMaxXAxisSize(20);
        viewPort.setScrollable(true);
        Button btnStart = (Button)findViewById(R.id.btnStart);
        Button btnStop = (Button)findViewById(R.id.btnStop);
        Button btnRetrieve = (Button)findViewById(R.id.btnRetrieve);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        // test anonymous click listener
        btnRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Retrieve clicked");
                Intent intent = new Intent(MainActivity.this, RetrieveActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                if(isStop == true) {
                    isStop = false;
                    generateData();
                }
                break;
            case R.id.btnStop:
                isStop = true;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //generateData();
    }

    private void generateData() {
        new GetDataAsync().execute(lastX);
    }

    private class GetDataAsync extends AsyncTask<Integer, Void, DataPoint>
    {

        @Override
        protected DataPoint doInBackground(Integer... params) {
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new DataPoint(lastX++, RANDOM.nextDouble()*10d);
        }

        @Override
        protected void onPostExecute(DataPoint dataPoint) {
            if(lastX >= 50)
                lineSeries.appendData(dataPoint, true, 51);
            else
                lineSeries.appendData(dataPoint, false, 51);
            if(isStop == false)
                new GetDataAsync().execute(lastX);
        }
    }
}
