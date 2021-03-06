package guru.nickthompson.livethread.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import guru.nickthompson.livethread.AsyncCommandAndCallback;
import guru.nickthompson.livethread.DelayRefreshTask;
import guru.nickthompson.livethread.R;
import guru.nickthompson.livethread.SortCommentsByTime;
import guru.nickthompson.livethread.SortedHashedArrayList;
import guru.nickthompson.livethread.adapters.CommentsAdapter;
import guru.nickthompson.redditapi.Comment;
import guru.nickthompson.redditapi.Post;

/**
 * Activity for handling a post in Live Thread mode
 */
public class PostActivity extends AppCompatActivity {
    // 5000 ms (5s) delay between refreshses
    private static final long DELAY = 5000;
    private static final String TAG = "LT.PostActivity";

    private Post post;
    private TextView tvPostNew;

    private SortedHashedArrayList<Comment> comments;
    private CommentsAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayoutManager layoutManager;

    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private AtomicBoolean runRefresh = new AtomicBoolean(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: try checking savedInstanceState
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // TODO: maybe abstract this a bit so we can just pass it into some builder

        tvPostNew = (TextView) findViewById(R.id.tv_post_new);

        post = (Post) getIntent().getSerializableExtra("POST");

        setupComments();

        progressBar = (ProgressBar) findViewById(R.id.pb_post_refresh);//.get();
        // new DelayRefreshTask(5000, progressBar).execute();

        Log.d(TAG, "calling repeating refresh");
        initializeRepeatingRefresh();

        // setup action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.t_post);
        toolbar.setTitle(post.getTitle());
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        cancelRefresh();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        // means onCreate was skipped (maybe phone woke up or something)
        // restart the refresher
        if (!runRefresh.get()) {
            runRefresh.set(true);
            initializeRepeatingRefresh();
        }
    }

    /**
     * Cancel the refresh. Lets the current AsyncTask finish then it stops repeatedly calling.
     */
    private void cancelRefresh() {
        runRefresh.set(false);
    }

    /**
     * Responsible for repeatedly refreshing the comments.
     */
    private void initializeRepeatingRefresh() {
        final CommentRefresher commentRefresherFunctionObject = new CommentRefresher();
        //initial run
        new DelayRefreshTask(progressBar, commentRefresherFunctionObject).execute();

        // helps run code on a given thread after a delay & periodically
        refreshHandler = new Handler();

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                new DelayRefreshTask(DELAY, progressBar, commentRefresherFunctionObject, runRefresh)
                        .execute();
            }
        };

        refreshHandler.post(refreshRunnable);
    }


    /**
     * Setup recycler view and get the data setup.
     */
    private void setupComments() {
        // Lookup the recyclerview in activity layout
        recyclerView = (RecyclerView) findViewById(R.id.rv_post_comments);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // add a horizontal line between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        comments = new SortedHashedArrayList<Comment>(new SortCommentsByTime());
        // Create adapter passing in the sample user data
        adapter = new CommentsAdapter(this, comments);
        // Attach the adapter to the recyclerview to populate items
        recyclerView.setAdapter(adapter);
        // Set layout manager to position the items
        recyclerView.setLayoutManager(layoutManager);

        // add scroll listener for it
        recyclerView.addOnScrollListener(new ScrollListener());
    }

    public class ScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int pos = layoutManager.findFirstVisibleItemPosition();
            pos++;
            //TODO: display in TextView the position
            if (layoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
                tvPostNew.setText(" " + String.valueOf(pos) + " ");
            } else {
                tvPostNew.setText("");
            }
        }
    }

    /**
     * Add a comment to the RecyclerView.
     *
     * @param comment the new comment.
     */
    private void addComment(Comment comment) {

        if (!(this.comments.contains(comment))) {
            int pos = this.comments.insert(comment);
            this.adapter.notifyItemInserted(pos);
        }


        if (this.layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    /**
     * The function object that handles comment refreshing.
     */
    public class CommentRefresher implements AsyncCommandAndCallback<ArrayList<Comment>> {

        @Override
        public ArrayList<Comment> command() {
            Log.d(TAG, "running command");
            return post.getAllComments();

        }

        // TODO: this if statement is pointless, right?
        @Override
        public void callback(ArrayList<Comment> result) {
            Collections.reverse(result);
            for (Comment c : result) {
                addComment(c);
            }
        }
    }
}

