package com.kingpixel.wondertrade.database;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SubscriberHelpers {
  public static class OperationSubscriber<T> implements Subscriber<T> {
    private final CompletableFuture<T> future;

    public OperationSubscriber(CompletableFuture<T> future) {
      this.future = future;
    }

    @Override
    public void onSubscribe(Subscription s) {
      s.request(1);
    }

    @Override
    public void onNext(T t) {
      future.complete(t);
    }

    @Override
    public void onError(Throwable t) {
      future.completeExceptionally(t);
    }

    @Override
    public void onComplete() {
      // No-op
    }
  }

  public static class CollectSubscriber<T> implements Subscriber<T> {
    private final CompletableFuture<List<T>> future;
    private final List<T> items = new ArrayList<>();

    public CollectSubscriber(CompletableFuture<List<T>> future) {
      this.future = future;
    }

    @Override
    public void onSubscribe(Subscription s) {
      s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
      items.add(t);
    }

    @Override
    public void onError(Throwable t) {
      future.completeExceptionally(t);
    }

    @Override
    public void onComplete() {
      future.complete(items);
    }
  }
}
