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

@Singleton
public class MessageService {

  private static final String BEARER_TOKEN_FORMAT = "Bearer %s";

  private final ChatServiceProxy proxy;
  private final ChatServiceLongPollingProxy longPollingProxy;
  private final GoogleSignInService signInService;
  private final Scheduler scheduler;

  @Inject
  public MessageService(ChatServiceProxy proxy, ChatServiceLongPollingProxy longPollingProxy,
      GoogleSignInService signInService) {
    this.proxy = proxy;
    this.longPollingProxy = longPollingProxy;
    this.signInService = signInService;
    scheduler = Schedulers.io();
  }

  Single<List<Message>> getMessages(UUID channelKey, Instant since) {
    return signInService
        .refreshBearerToken()
        .observeOn(scheduler)
        .map((token) -> String.format(BEARER_TOKEN_FORMAT, token))
        .flatMap((bearerToken) -> longPollingProxy
            .getSince(channelKey, since.toEpochMilli(), bearerToken));
  }

  Single<List<Message>> sendMessage(UUID channelKey, Message message, Instant since) {
    return signInService
        .refreshBearerToken()
        .observeOn(scheduler)
        .map((token) -> String.format(BEARER_TOKEN_FORMAT, token))
        .flatMap((bearerToken) -> proxy
            .postMessage(message, channelKey, since.toEpochMilli(), bearerToken));
  }

  Single<List<Channel>> getChannels(boolean active) {
    return signInService
        .refreshBearerToken()
        .observeOn(scheduler)
        .map((token) -> String.format(BEARER_TOKEN_FORMAT, token))
        .flatMap((bearerToken) -> proxy.getChannels(active, bearerToken));
  }

}
