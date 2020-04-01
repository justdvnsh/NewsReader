package com.divyansh.newsreader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.divyansh.newsreader.R;
import com.divyansh.newsreader.pojo.Article;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class newsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MENU_ITEM_VIEW_TYPE = 0;
    private static final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;

    private Context context;
    private mContextListener listener;

    // The list of Native ads and menu items.
    private final List<Object> mRecyclerViewItems;

    public interface mContextListener {
        void onNewsClick(Article article);
    }

    public newsAdapter(Context context, mContextListener listener, List<Object> mRecyclerViewItems) {
        this.context = context;
        this.listener = listener;
        this.mRecyclerViewItems = mRecyclerViewItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                View unifiedNativeLayoutView = LayoutInflater.from(
                        viewGroup.getContext()).inflate(R.layout.ad_unified,
                        viewGroup, false);
                return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
            case MENU_ITEM_VIEW_TYPE:
                // Fall through.
            default:
                View menuItemLayoutView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.news_card_layout, viewGroup, false);
                return new newsViewsHolder(menuItemLayoutView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder  holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                UnifiedNativeAd nativeAd = (UnifiedNativeAd) mRecyclerViewItems.get(position);
                populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());
                break;
            case MENU_ITEM_VIEW_TYPE:
                // fall through
            default:
                newsViewsHolder newsViewsHolder = (newsViewsHolder) holder;
                Article article = (Article) mRecyclerViewItems.get(position);

                // set up image
                Object urlToImage = article.getUrlToImage();
                if (urlToImage != null) Picasso.with(context).load(urlToImage.toString()).into(newsViewsHolder.image);

                // set up author and publish date
                Object author = article.getAuthor();
                String pubDate = article.getPublishedAt();
                if (author != null) newsViewsHolder.author.setText("By- " + author.toString());
                else newsViewsHolder.author.setText("By- Some Author");
                newsViewsHolder.date.setText("On - " + pubDate);

                // set up title and content
                String title = article.getTitle();
                Object content = article.getContent();
                if (content == null || content == "") content = "Nothing to show";
                else if ( content.toString().length() < 40 )content = content + " ... Read More";
                else content = content.toString().substring(0,40) + " ...Read More";
                newsViewsHolder.title.setText(title);
                newsViewsHolder.content.setText(content.toString());

                // set up source
                String source = article.getSource().getName();
                newsViewsHolder.source.setText("Source- " + source);
        }
    }

    @Override
    public int getItemCount() {
        if ( mRecyclerViewItems.size() == 0 )  return 0;
        else return mRecyclerViewItems.size();
    }

    @Override
    public int getItemViewType(int position) {

        Object recyclerViewItem = mRecyclerViewItems.get(position);
        if (recyclerViewItem instanceof UnifiedNativeAd) {
            return UNIFIED_NATIVE_AD_VIEW_TYPE;
        }
        return MENU_ITEM_VIEW_TYPE;
    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd,
                                      UnifiedNativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }

    public class newsViewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.newsImage)
        ImageView image;
        @BindView(R.id.author)
        TextView author;
        @BindView(R.id.publish_date)
        TextView date;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.source)
        TextView source;

        public newsViewsHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Article article = (Article) mRecyclerViewItems.get(clickedPosition);
            listener.onNewsClick(article);
        }
    }

}
