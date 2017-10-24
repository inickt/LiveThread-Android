package io.github.livethread.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import io.github.livethread.R;
import io.github.livethread.fragments.PostListFragment;
import io.github.livethread.fragments.SelectSubredditFragment;
import io.github.livethread.redditapi.Post;

/**
 * Handles choosing a subreddit and navigating to a certain post from the sub through fragments.
 */
public class MainActivity extends AppCompatActivity implements SelectSubredditFragment.OnFragmentInteractionListener,
        PostListFragment.OnListFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);

        // set up initial fragment to be the subreddit selector
        if (savedInstanceState == null) {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fl_subreddit, new SelectSubredditFragment());
            ft.commit();

            // setup action bar
            Toolbar toolbar = (Toolbar) findViewById(R.id.t_subreddit);
            toolbar.setTitle("LiveThread");
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.mipmap.ic_launcher_circle);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fm = getSupportFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    /**
     * Called when a subreddit is entered / clicked.
     *
     * @param subredditName name of the subreddit.
     */
    @Override
    public void onSubredditClick(String subredditName) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        ft.replace(R.id.fl_subreddit, PostListFragment.newInstance(subredditName))
                .addToBackStack(null);
        ft.commit();

        // if keyboard is visible hide it
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Called on click of a Post.
     *
     * @param item the Post that was clicked.
     */
    @Override
    public void onListFragmentInteraction(Post item) {
        Intent intent = new Intent(getApplicationContext(), PostActivity.class);
        intent.putExtra("POST", item);
        startActivity(intent);
    }

    /**
     * Handle back button returning to previous fragment (and activity).
     */
    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        // go to the last fragment
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            // handle closing the drawer
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main_profile) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
