package com.alba.yu.android.component;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ContainerView mainContainer = findViewById(R.id.container_view);
        CustomerView customerView = findViewById(R.id.customer_view);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        // 增加滚动依赖
        mainContainer.setRelevantViews(customerView, recyclerView);

        // 填充一些内容
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView textView = new TextView(MainActivity.this);
                textView.setPadding(20, 20, 20, 20);
                return new ViewHolder(textView);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

                ((ViewHolder) holder).view.setText("这是第" + position + "个");
            }

            @Override
            public int getItemCount() {
                return 55;
            }
        });

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView view;

        public ViewHolder(TextView view) {
            super(view);
            this.view = view;
        }
    }
}
