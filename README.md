# SingleLiveEventDemo
A lifecycle-aware observable that sends only new updates after subscription, used for events like  * navigation and Snackbar messages.

### Android Jetpack 架构组件LiveData的使用

#### 使用 LiveData 的优势
##### 使用 LiveData 具有以下优势：

- 确保界面符合数据状态

LiveData 遵循观察者模式。当生命周期状态发生变化时，LiveData 会通知 Observer 对象。您可以整合代码以在这些 Observer 对象中更新界面。观察者可以在每次发生更改时更新界面，而不是在每次应用数据发生更改时更新界面。
- 不会发生内存泄漏

观察者会绑定到 Lifecycle 对象，并在其关联的生命周期遭到销毁后进行自我清理。
- 不会因 Activity 停止而导致崩溃

如果观察者的生命周期处于非活跃状态（如返回栈中的 Activity），则它不会接收任何 LiveData 事件。
- 不再需要手动处理生命周期

界面组件只是观察相关数据，不会停止或恢复观察。LiveData 将自动管理所有这些操作，因为它在观察时可以感知相关的生命周期状态变化。
- 数据始终保持最新状态

如果生命周期变为非活跃状态，它会在再次变为活跃状态时接收最新的数据。例如，曾经在后台的 Activity 会在返回前台后立即接收最新的数据。
- 适当的配置更改

如果由于配置更改（如设备旋转）而重新创建了 Activity 或 Fragment，它会立即接收最新的可用数据。
- 共享资源

您可以使用单一实例模式扩展 LiveData 对象以封装系统服务，以便在应用中共享它们。LiveData 对象连接到系统服务一次，然后需要相应资源的任何观察者只需观察 LiveData 对象。如需了解详情，请参阅扩展 LiveData。

代码例子如下
```
 public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private MainViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(MainViewModel.class);

        binding.tvAdd.setOnClickListener(this);
        binding.tvContent.setOnClickListener(this);

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
}
    
```


```
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
```


类似效果的我们用过EventBus 事件分发，常用的就有2种形式，普通和粘性事件。而我们的LiveData 默认就是粘性的，在使用时需要注意数据倒灌，而解决方式就是我在demo中用到的SingleLiveEvent或者Event 事件包装下处理，普通的还是可以直接使用MutableLiveData

#### 核心


```
public class SingleLiveEvent<T> extends MutableLiveData<T> {

    private static final String TAG = "SingleLiveEvent";

    private final AtomicBoolean mPending = new AtomicBoolean(false);

    @MainThread
    public void observe(LifecycleOwner owner, final Observer<? super T> observer) {

        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.");
        }

        // Observe the internal MutableLiveData
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                if (t instanceof  Event) {
                    Event event = (Event) t;
                    if (event.getContentIfNotHandled() != null) {
                        observer.onChanged(t);
                    }
                } else {
                    if (mPending.compareAndSet(true, false)) {
                        observer.onChanged(t);
                    }
                }

            }
        });
    }



    @MainThread
    public void setValue(@Nullable T t) {
        mPending.set(true);
        super.setValue(t);
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    public void call() {
        setValue(null);
    }

}
```


