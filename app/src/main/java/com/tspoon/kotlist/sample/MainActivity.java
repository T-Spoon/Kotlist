package com.tspoon.kotlist.sample;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.tspoon.kotlist.KotlistView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.list_view) KotlistView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(MainActivity.this);

        List<String> items = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            items.add("Kotlist " + i);
        }

        final KotlistView.RecyclerAdapter adapter = new BasicAdapter(this, items);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Clicked: " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Long Clicked: " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
                return true;
            }
        });


    }

    static class BasicAdapter extends KotlistView.RecyclerAdapter {

        private static final int VIEW_TYPE_ONE = 1;
        private static final int VIEW_TYPE_TWO = 2;

        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private List<String> mItems;

        public BasicAdapter(Context c, List<String> items) {
            mContext = c;
            mLayoutInflater = LayoutInflater.from(c);
            mItems = items;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup container, int position) {
            ViewHolderOne holder;
            if (getItemViewType(position) == VIEW_TYPE_ONE) {
                holder = new ViewHolderOne(mLayoutInflater.inflate(android.R.layout.simple_list_item_1, container, false));
                holder.itemView.setBackgroundColor(Color.parseColor("#B3E5FC"));
            } else {
                holder = new ViewHolderTwo(mLayoutInflater.inflate(android.R.layout.simple_list_item_2, container, false));
                holder.itemView.setBackgroundColor(Color.parseColor("#F3E5F5"));
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            if (getItemViewType(position) == VIEW_TYPE_ONE) {
                ((ViewHolderOne) viewHolder).textView.setText(getItem(position).toString());
            } else {
                ((ViewHolderTwo) viewHolder).textView.setText(getItem(position).toString());
                ((ViewHolderTwo) viewHolder).textViewTwo.setText("Subtitle " + position);
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2 == 0 ? VIEW_TYPE_ONE : VIEW_TYPE_TWO;
        }

        static class ViewHolderOne extends RecyclerView.ViewHolder {

            @InjectView(android.R.id.text1) TextView textView;

            public ViewHolderOne(View itemView) {
                super(itemView);
                ButterKnife.inject(this, itemView);
            }
        }

        static class ViewHolderTwo extends ViewHolderOne {

            @InjectView(android.R.id.text2) TextView textViewTwo;

            public ViewHolderTwo(View itemView) {
                super(itemView);
                ButterKnife.inject(this, itemView);
            }
        }
    }
}
