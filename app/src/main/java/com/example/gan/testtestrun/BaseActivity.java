package com.example.gan.testtestrun;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_settings:
                return true;
            case R.id.menu_about:
                openAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openAbout(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Gigantech Corp 2017 \u00A9");
        alertDialogBuilder.setPositiveButton("Ok", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
