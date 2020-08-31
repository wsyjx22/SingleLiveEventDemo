package com.yjx.singlelive;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.yjx.singlelive.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private MainViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Main", "onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // 创建ViewModel的方法
        ViewModelProvider provider = new ViewModelProvider(getViewModelStore(), ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        viewModel =  provider.get("main", MainViewModel.class);

        //viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(MainViewModel.class);

        binding.tvAdd.setOnClickListener(this);
        binding.tvContent.setOnClickListener(this);

        Log.i("Main", viewModel.getContent().getValue() + "======");

        viewModel.getContent().setValue("默认数据");
        viewModel.getDesc().setValue("默认描述内容，非粘性消息");
        viewModel.getMsg().setValue(new Event<>("MSG Event 默认描述内容，非粘性消息"));

        // 注册订阅者观察LiveData数据变化(默认LiveData 粘性)
        viewModel.getContent().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.i(MainActivity.class.getSimpleName(), "content = " + s);
            }
        });
        // 注册订阅者观察SingleLiveEvent数据变化 (SingleLiveEvent 非粘性)
        viewModel.getDesc().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.i(MainActivity.class.getSimpleName(), "desc = " + s);
            }
        });

        // 注册订阅者观察SingleLiveEvent数据变化 (SingleLiveEvent 非粘性)
        viewModel.getMsg().observe(this, new Observer<Event<String>>() {
            @Override
            public void onChanged(Event<String> event) {
                if (event.getContent() != null) {
                    Log.i(MainActivity.class.getSimpleName(), "msg = " + event.getContent());
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_add) {
            // LiveData 粘性消息，当我先处理viewModel.getContent().setValue("新内容");，后处理下面代码时出现它同时收到了新内容的文案
            // 这个时候其实这个数据 其实不是我们想要的，如果规避这样的粘性消息就引出了 SingleLiveEvent
            // Event 包装事件也是可以，2种处理方式

            // 注册订阅者观察LiveData数据变化(默认LiveData 粘性)
            viewModel.getContent().observe(this, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    Log.i(MainActivity.class.getSimpleName(), "content = " + s);
                }
            });
            // 注册订阅者观察SingleLiveEvent数据变化 (SingleLiveEvent 非粘性)
            viewModel.getDesc().observe(this, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    Log.i(MainActivity.class.getSimpleName(), "desc = " + s);
                }
            });

            // 注册订阅者观察SingleLiveEvent数据变化 (SingleLiveEvent 非粘性)
            viewModel.getMsg().observe(this, new Observer<Event<String>>() {
                @Override
                public void onChanged(Event<String> event) {
                    if (event.getContentIfNotHandled() != null) {
                        Log.i(MainActivity.class.getSimpleName(), "msg = " + event.getContentIfNotHandled());
                    }
                }
            });
        } else {
            viewModel.getContent().setValue("新内容");
            viewModel.getDesc().setValue("新的描述内容，发送非粘性消息");
            viewModel.getMsg().setValue(new Event<>("MSG Event 新的描述内容，非粘性消息"));
        }
    }

    @Override
    protected void onResume() {
        Log.i("Main", "onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.i("Main", "onRestart");
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Log.i("Main", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("Main", "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Main", "onDestroy");
        super.onDestroy();
    }

}
