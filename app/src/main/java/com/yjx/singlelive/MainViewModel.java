package com.yjx.singlelive;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends AndroidViewModel {

    public SingleLiveEvent<Event<String>> msg = new SingleLiveEvent<>();

    public MutableLiveData<String> content = new MutableLiveData<>();

    public SingleLiveEvent<String> desc = new SingleLiveEvent<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public SingleLiveEvent<Event<String>> getMsg() {
        return msg;
    }

    public MutableLiveData<String> getContent() {
        return content;
    }

    public SingleLiveEvent<String> getDesc() {
        return desc;
    }
}
