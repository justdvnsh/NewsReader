package com.divyansh.newsreader.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.divyansh.newsreader.MyApplication;
import com.divyansh.newsreader.R;
import com.divyansh.newsreader.adapters.newsAdapter;
import com.divyansh.newsreader.handlers.MyNotificationOpenHandler;
import com.divyansh.newsreader.network.APIClient;
import com.divyansh.newsreader.network.APIEndpoints;
import com.divyansh.newsreader.pojo.Article;
import com.divyansh.newsreader.pojo.News;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements newsAdapter.mContextListener, LocationListener {

    @BindView(R.id.recycle_news)
    RecyclerView recyclerView;
    @BindView(R.id.progress_main)
    ProgressBar progressBar;
    @BindView(R.id.error_layout)
    LinearLayout errorLayout;

    private newsAdapter adapter;
    private APIEndpoints apiEndpoints;
    private final String CATEGORY = "category";
    private final String API_KEY = "apiKey";
    private final String COUNTRY = "country";
    private String countryCode = "";
    private String category = "technology";

    // The number of native ads to load and display.
    public static final int NUMBER_OF_ADS = 3;

    // final int for location permissions
    public static final int LOCATION_PERMISSION = 10;

    // The AdLoader used to load ads.
    private AdLoader adLoader;

    // List of native ads that have been successfully loaded.
    private List<UnifiedNativeAd> mNativeAds = new ArrayList<>();

    private List<Object> mRecyclerViewItems = new ArrayList<>();

    private Context myApplication;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private LocationManager locationManager;

    // check permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Granted. Start getting the location information
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 10, this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }  else {
                errorLayout.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Cannot fetch location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        myApplication = MyApplication.getInstance();

        // setup location manager & check permissions
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        } else {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 10, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // recycler view setup
        setupRecyclerView();

        // initialize mobile ads - admob
        List<String> testDeviceIds = Arrays.asList(Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
        MobileAds.initialize(this, "ca-app-pub-7888506497709570~4864315574");

        // firebase remote config initialization
        intializeFirebaseRemoteConfig();
    }

    private void intializeFirebaseRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1000)
                .setDeveloperModeEnabled(true)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default);
        fetchAdsShowcase();
    }

    private void fetchAdsShowcase() {
        Log.i("FIREBASE_REMOTE_ADS", mFirebaseRemoteConfig.getString("ads_enabled"));
//        Toast.makeText(this, mFirebaseRemoteConfig.getString("ads_enabled"), Toast.LENGTH_SHORT).show();
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.i("FIREBASE_REMOTE_CONFIG", "Config params updated: " + updated);
//                            Toast.makeText(MainActivity.this, "Fetch and activate succeeded",
//                                    Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(MainActivity.this, "Fetch failed",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                    if (mFirebaseRemoteConfig.getString("ads_enabled").equals("true")) {
                        loadNativeAds();
                    }
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

    private Map<String, String> getMap(String countryCode, String cat){
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
                if (countryCode != "") {
                    getNews(getMap(countryCode, category));
                }
                break;
            case R.id.action_sports:
                category = getString(R.string.category_sports).toLowerCase();
                mRecyclerViewItems = new ArrayList<>();
                if (countryCode != "") {
                    getNews(getMap(countryCode, category));
                }
                break;
            case R.id.action_tech:
                category = getString(R.string.category_tech).toLowerCase();
                mRecyclerViewItems = new ArrayList<>();
                if (countryCode != "") {
                    getNews(getMap(countryCode, category));
                }
                break;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getCountryCodeAndFetchNews(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {

            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if ( addresses != null && addresses.size() > 0 ) {

                countryCode = addresses.get(0).getCountryCode();
                Log.i("Address", countryCode);
                // fetch teh news & set the progressbar's visibility.
                getNews(getMap(countryCode, category));
                progressBar.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        getCountryCodeAndFetchNews(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
