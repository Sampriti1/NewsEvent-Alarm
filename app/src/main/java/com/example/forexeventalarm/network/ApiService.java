package com.example.forexeventalarm.network;

import com.example.forexeventalarm.model.Event;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET(".") // 👈 update endpoint if needed
    Call<List<Event>> getEvents();
}
