package com.example.gan.testtestrun;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    private void addData(){
        lineSeries.appendData(new DataPoint(lastX++, RANDOM.nextDouble()*10d), false, 20);
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
        generateData();
    }

    private void generateData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isStop == false)
                //for(int i=0; i<10; i++)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addData();
                        }
                    });
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
