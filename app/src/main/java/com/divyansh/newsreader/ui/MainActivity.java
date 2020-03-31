package com.divyansh.newsreader.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.divyansh.newsreader.R;
import com.divyansh.newsreader.network.APIClient;
import com.divyansh.newsreader.network.APIEndpoints;
import com.divyansh.newsreader.pojo.Article;
import com.divyansh.newsreader.pojo.News;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycle_news)
    RecyclerView recyclerView;
    @BindView(R.id.progress_main)
    ProgressBar progressBar;

    private APIEndpoints apiEndpoints;
    private String countryCode = "in";
    private String category = "business";
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // make the url query
        Map<String, String> options = new HashMap<>();
        options.put("country", countryCode);
        options.put("category", category);
        options.put("apiKey", APIEndpoints.APIKEY);

        // fetch teh news & set the progressbar's visibility.
        getNews(options);
        progressBar.setVisibility(View.VISIBLE);

        // recycler view setup
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void getNews(Map<String, String> options) {

        apiEndpoints = APIClient.getInstance().create(APIEndpoints.class);
        Call<News> call = apiEndpoints.fetchNews(options);
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    News news = response.body();
                    List<Article> articles = news.getArticles();
//                    textView.setText(articles.get(0).getDescription().toString());
                } else {
                    Log.i("Error-body->", response.raw().message());
                    Toast.makeText(getApplicationContext(), "Something happened", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.i("Error->", t.getMessage());
                Toast.makeText(getApplicationContext(), "Internal Server Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
