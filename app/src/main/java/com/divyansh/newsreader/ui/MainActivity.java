package com.divyansh.newsreader.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.divyansh.newsreader.R;
import com.divyansh.newsreader.adapters.newsAdapter;
import com.divyansh.newsreader.network.APIClient;
import com.divyansh.newsreader.network.APIEndpoints;
import com.divyansh.newsreader.pojo.Article;
import com.divyansh.newsreader.pojo.News;
import com.divyansh.newsreader.pojo.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements newsAdapter.mContextListener{

    @BindView(R.id.recycle_news)
    RecyclerView recyclerView;
    @BindView(R.id.progress_main)
    ProgressBar progressBar;

    private newsAdapter adapter;
    private APIEndpoints apiEndpoints;
    private final String CATEGORY = "category";
    private final String API_KEY = "apiKey";
    private final String COUNTRY = "country";
    private String countryCode = "in";
    private String category = "technology";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // fetch teh news & set the progressbar's visibility.
        getNews(getMap(category));
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
                    adapter = new newsAdapter(MainActivity.this, MainActivity.this, articles);
                    recyclerView.setAdapter(adapter);
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

    @Override
    public void onNewsClick(Article article) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("url", article.getUrl());
        startActivity(intent);
    }

    private Map<String, String> getMap(String cat){
        Map<String, String> options = new HashMap<>();
        options.put(COUNTRY, countryCode);
        options.put(CATEGORY, cat);
        options.put(API_KEY, APIEndpoints.APIKEY);
        return options;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_business:
                category = getString(R.string.category_business).toLowerCase();
                getNews(getMap(category));
                break;
            case R.id.action_sports:
                category = getString(R.string.category_sports).toLowerCase();
                getNews(getMap(category));
                break;
            case R.id.action_tech:
                category = getString(R.string.category_tech).toLowerCase();
                getNews(getMap(category));
                break;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }
}
