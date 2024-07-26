package com.kingpixel.wondertrade.database.mongodb;

import org.reactivestreams.Publisher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReactiveMongoHelper {
  public static <T> CompletableFuture<T> toCompletableFuture(Publisher<T> publisher) {
    CompletableFuture<T> future = new CompletableFuture<>();
    publisher.subscribe(new SubscriberHelpers.OperationSubscriber<>(future));
    return future;
  }

  public static <T> CompletableFuture<List<T>> toCompletableFutureList(Publisher<T> publisher) {
    CompletableFuture<List<T>> future = new CompletableFuture<>();
    publisher.subscribe(new SubscriberHelpers.CollectSubscriber<>(future));
    return future;
  }
}
