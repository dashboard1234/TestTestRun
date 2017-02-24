package com.example.gan.testtestrun;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class RetrieveActivity extends AppCompatActivity {
    private GraphView graph;
    private EditText tvFrom;
    private EditText tvTo;
    private LineGraphSeries<DataPoint> lineSeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve);

        tvFrom = (EditText) findViewById(R.id.edtFrom);
        tvTo = (EditText)findViewById(R.id.edtTo);
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
        Button btnSignal = (Button)findViewById(R.id.btnSignal);
        btnSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long from = Integer.parseInt(tvFrom.getText().toString());
                long to = Integer.parseInt(tvTo.getText().toString());
                new GetDataRangeAsync().execute(from, to);
            }
        });
    }

    private class GetDataRangeAsync extends AsyncTask<Long, Void, Integer>
    {

        @Override
        protected Integer doInBackground(Long... params) {
            long from = params[0];
            long to = params[1];
            int count = 0;
            Uri uri = ContentUris.withAppendedId(SignalProvider.CONTENT_URI,from);
            uri = ContentUris.withAppendedId(uri, to);
            Cursor cursor = getContentResolver().query(uri, new String[]{SignalProvider.COL_TICK, SignalProvider.COL_SIGNAL},
                    null, null, null);
            if(cursor == null)
                return 0;
            try{
                while (cursor.moveToNext())
                {
                    lineSeries.appendData(new DataPoint(cursor.getDouble(0), cursor.getDouble(1)), false, 50);
                    count++;
                }
            }
            finally {
                cursor.close();
            }
            return count;
        }

        @Override
        protected void onPostExecute(Integer count) {
            Toast.makeText(getBaseContext(), count + " points retrieved", Toast.LENGTH_SHORT).show();

        }
    }
}
