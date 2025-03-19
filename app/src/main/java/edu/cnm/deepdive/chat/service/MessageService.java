package edu.cnm.deepdive.chat.service;

import edu.cnm.deepdive.chat.model.dto.Channel;
import edu.cnm.deepdive.chat.model.dto.Message;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.http.Path;

@Singleton
public class MessageService {

  private final ChatServiceProxy proxy;
  private final ChatServiceLongPollingProxy longPollingProxy;
  private final Scheduler scheduler;

  @Inject
  public MessageService(ChatServiceProxy proxy, ChatServiceLongPollingProxy longPollingProxy) {
    this.proxy = proxy;
    this.longPollingProxy = longPollingProxy;
    scheduler = Schedulers.io();
  }

  Single<List<Message>> getMessages(UUID channelKey, Instant since) {
    // TODO: 2025-03-19 Refresh bearer token and pass downstream.
    return longPollingProxy
        .getSince(channelKey, since.toEpochMilli())
        .subscribeOn(scheduler);
  }

  Single<List<Message>> sendMessage(UUID channelKey, Message message, Instant since) {
    // TODO: 2025-03-19 Refresh bearer token and pass downstream.
    return proxy
        .postMessage(message, channelKey, since.toEpochMilli())
        .subscribeOn(scheduler);
  }

  Single<List<Channel>> getChannels(boolean active) {
    // TODO: 2025-03-19 Refresh bearer token and pass downstream.
    return proxy
        .getChannels(active)
        .subscribeOn(scheduler);
  }

}
