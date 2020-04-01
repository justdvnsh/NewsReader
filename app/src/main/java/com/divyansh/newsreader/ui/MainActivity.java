package com.divyansh.newsreader.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.divyansh.newsreader.MyApplication;
import com.divyansh.newsreader.R;
import com.divyansh.newsreader.adapters.newsAdapter;
import com.divyansh.newsreader.network.APIClient;
import com.divyansh.newsreader.network.APIEndpoints;
import com.divyansh.newsreader.pojo.Article;
import com.divyansh.newsreader.pojo.News;
import com.divyansh.newsreader.pojo.Source;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Arrays;
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

    // The number of native ads to load and display.
    public static final int NUMBER_OF_ADS = 6;

    // The AdLoader used to load ads.
    private AdLoader adLoader;

    // List of native ads that have been successfully loaded.
    private List<UnifiedNativeAd> mNativeAds = new ArrayList<>();

    private List<Object> mRecyclerViewItems = new ArrayList<>();

    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        myApplication = MyApplication.getInstance();

        // fetch teh news & set the progressbar's visibility.
        getNews(getMap(category));
        progressBar.setVisibility(View.VISIBLE);

        // recycler view setup
        setupRecyclerView();

        // initialize mobile ads - admob
        List<String> testDeviceIds = Arrays.asList(Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);

    }


    private void insertAdsInMenuItems() {
        if (mNativeAds.size() <= 0) {
            return;
        }

        int offset = (mRecyclerViewItems.size() / mNativeAds.size()) + 1;
        int index = 0;
        for (UnifiedNativeAd ad: mNativeAds) {
            mRecyclerViewItems.add(index, ad);
            index = index + offset;
        }
    }

    private void loadNativeAds() {

        AdLoader.Builder builder = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110");
        adLoader = builder.forUnifiedNativeAd(
                new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // A native ad loaded successfully, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        mNativeAds.add(unifiedNativeAd);
                        if (!adLoader.isLoading()) {
                            insertAdsInMenuItems();
                        }
                    }
                }).withAdListener(
                new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        Log.e("MainActivity", "The previous native ad failed to load. Attempting to"
                                + " load another.");
                        if (!adLoader.isLoading()) {
                            insertAdsInMenuItems();
                        }
                    }
                }).build();

        // Load the Native Express ad.
        adLoader.loadAds(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators are added by default as test devices
                .build(), NUMBER_OF_ADS);
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
                    for ( Article article: news.getArticles() ) {
                        mRecyclerViewItems.add(article);
                    }

                    // load ads
                    loadNativeAds();
                    adapter = new newsAdapter(MainActivity.this, MainActivity.this, mRecyclerViewItems);
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
                mRecyclerViewItems = new ArrayList<>();
                getNews(getMap(category));
                break;
            case R.id.action_sports:
                category = getString(R.string.category_sports).toLowerCase();
                mRecyclerViewItems = new ArrayList<>();
                getNews(getMap(category));
                break;
            case R.id.action_tech:
                category = getString(R.string.category_tech).toLowerCase();
                mRecyclerViewItems = new ArrayList<>();
                getNews(getMap(category));
                break;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }
}
