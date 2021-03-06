package edu.ucla.cens.wetap;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.widget.Button;

public class about extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate (b);
        setContentView (R.layout.about);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu m) {
        super.onCreateOptionsMenu (m);

        m.add (Menu.NONE, 0, Menu.NONE, "Home").setIcon (android.R.drawable.ic_menu_revert);
        m.add (Menu.NONE, 1, Menu.NONE, "Survey").setIcon (android.R.drawable.ic_menu_agenda);
        m.add (Menu.NONE, 2, Menu.NONE, "Map").setIcon (android.R.drawable.ic_menu_mapmode);
        m.add (Menu.NONE, 3, Menu.NONE, "Instructions").setIcon (android.R.drawable.ic_menu_help);
        m.add (Menu.NONE, 4, Menu.NONE, "Statistics").setIcon(android.R.drawable.ic_menu_sort_by_size);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem index) {
        Context ctx = about.this;
        Intent i;
        switch (index.getItemId()) {
            case 0:
                i = new Intent (ctx, home.class);
                break;
            case 1:
                i = new Intent (ctx, survey.class);
                break;
            case 2:
                i = new Intent (ctx, map.class);
                break;
            case 3:
                i = new Intent (ctx, instructions.class);
                break;
            default:
                return false;
        }
        ctx.startActivity (i);
        this.finish();
        return true;
    }
}
