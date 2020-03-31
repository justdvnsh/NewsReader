package com.divyansh.newsreader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.divyansh.newsreader.R;
import com.divyansh.newsreader.pojo.Article;
import com.divyansh.newsreader.pojo.News;
import com.divyansh.newsreader.pojo.Source;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class newsAdapter extends RecyclerView.Adapter<newsAdapter.newsViewsHolder> {

    private Context context;
    private mContextListener listener;
    private List<Article> Articles;

    public interface mContextListener {
        void onNewsClick(News news);
    }

    public newsAdapter(Context context, mContextListener listener, List<Article> articles) {
        this.context = context;
        this.listener = listener;
        this.Articles = articles;
    }

    @NonNull
    @Override
    public newsViewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.news_card_layout;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new newsViewsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull newsViewsHolder holder, int position) {
        String urlToImage = Articles.get(position).getUrlToImage().toString();
        Picasso.with(context).load(urlToImage).into(holder.image);
    }

    @Override
    public int getItemCount() {
        if ( Articles.size() == 0 )  return 0;
        else return Articles.size();
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

        }
    }
}
