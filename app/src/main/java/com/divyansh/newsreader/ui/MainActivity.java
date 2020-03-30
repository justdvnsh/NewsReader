package com.divyansh.newsreader.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.divyansh.newsreader.R;
import com.divyansh.newsreader.network.APIClient;
import com.divyansh.newsreader.network.APIEndpoints;
import com.divyansh.newsreader.pojo.Article;
import com.divyansh.newsreader.pojo.News;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_text)
    TextView textView;

    private APIEndpoints apiEndpoints;
    private String countryCode;
    private String category;
    private String apiKey;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        countryCode = "in";
        category = "business";
        apiKey = "7eb04dd3f8df465e93fcf6cb5e3eef93";

        url = String.format("top-headlines?country=%s&category=%s&apiKey=%s", countryCode, category, apiKey);

        apiEndpoints = APIClient.getInstance().create(APIEndpoints.class);
        Call<News> call = apiEndpoints.fetchNews("http://newsapi.org/v2/top-headlines?country=in&category=business&apiKey=7eb04dd3f8df465e93fcf6cb5e3eef93");
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful()) {
                    News news = response.body();
                    List<Article> articles = news.getArticles();
                    textView.setText(articles.toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Something happened", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                Log.i("Error->", t.getMessage());
                Toast.makeText(getApplicationContext(), "Internal Server Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
