package com.example.forexeventalarm;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.forexeventalarm.adapter.EventAdapter;
import com.example.forexeventalarm.model.NewsEvents;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<NewsEvents> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();
        eventList.add(new NewsEvents(1, "Non-Farm Employment Change", "Sun, Sep 21", "6:00 PM"));
        eventList.add(new NewsEvents(2, "ECB President Lagarde Speaks", "Sun, Sep 21", "8:00 PM"));
        eventList.add(new NewsEvents(3, "Interest Rate Decision", "Mon, Sep 22", "5:00 PM"));



        adapter = new EventAdapter(eventList);
        recyclerView.setAdapter(adapter);
    }
}


