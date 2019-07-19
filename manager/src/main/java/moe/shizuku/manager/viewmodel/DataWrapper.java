package moe.shizuku.manager.viewmodel;

public class DataWrapper<T> {

    public T data;
    public Throwable error;

    public DataWrapper(T data) {
        this.data = data;
    }

    public DataWrapper(Throwable error) {
        this.error = error;
    }
}