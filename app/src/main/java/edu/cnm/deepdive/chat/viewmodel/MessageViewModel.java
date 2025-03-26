package edu.cnm.deepdive.chat.viewmodel;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import edu.cnm.deepdive.chat.model.dto.Channel;
import edu.cnm.deepdive.chat.model.dto.Message;
import edu.cnm.deepdive.chat.service.MessageService;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

/** @noinspection SequencedCollectionMethodCanBeUsed, unused */
@HiltViewModel
public class MessageViewModel extends ViewModel implements DefaultLifecycleObserver {

  private static final String TAG = MessageViewModel.class.getSimpleName();

  private final MessageService messageService;
  private final MutableLiveData<List<Message>> messages;
  private final MutableLiveData<List<Channel>> channels;
  private final MutableLiveData<Channel> selectedChannel;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;

  private Disposable poll;

  @Inject
  public MessageViewModel(MessageService messageService) {
    this.messageService = messageService;
    messages = new MutableLiveData<>(new LinkedList<>());
    channels = new MutableLiveData<>();
    selectedChannel = new MutableLiveData<>();
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
    fetchChannels();
  }

  public LiveData<List<Message>> getMessages() {
    return messages;
  }

  public LiveData<List<Channel>> getChannels() {
    return channels;
  }

  public LiveData<Channel> getSelectedChannel() {
    return selectedChannel;
  }

  public void setSelectedChannel(@Nullable Channel channel) {
    if (!Objects.equals(channel, selectedChannel.getValue())) {
      messages.postValue(new LinkedList<>());
      selectedChannel.postValue(channel);
      List<Message> msgs = messages.getValue();
      //noinspection DataFlowIssue
      msgs.clear();
      fetchMessages(channel, getSince(msgs));
    }
  }

  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  public void fetchChannels() {
    throwable.setValue(null);
    messageService
        .getChannels(true)
        .subscribe(
            this::handleChannels,
            this::postThrowable,
            pending
        );
  }

  /**
   * @noinspection DataFlowIssue
   */
  public void fetchMessages(Channel selectedChannel, Instant since) {
    if (selectedChannel != null) {
      throwable.postValue(null);
      if (poll != null) {
        poll.dispose();
      }
      poll = messageService
          .getMessages(selectedChannel.getKey(), since)
          .subscribe(
              (msgs) -> {
                List<Message> messages = this.messages.getValue();
                if (!msgs.isEmpty()) {
                  messages.addAll(msgs);
                  fetchMessages(selectedChannel, getSince(msgs));
                } else {
                  fetchMessages(selectedChannel, since);
                }
                this.messages.postValue(messages);
              },
              this::postThrowable,
              pending
          );
    }
  }

  /**
   * @noinspection DataFlowIssue
   */
  public void sendMessage(Message message) {
    throwable.setValue(null);
    Instant since = getSince(messages.getValue());
    messageService
        .sendMessage(selectedChannel.getValue().getKey(), message, since)
        .ignoreElement()
        .subscribe(
            () -> {
            },
            this::postThrowable,
            pending
        );
  }

  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    DefaultLifecycleObserver.super.onResume(owner);
    Channel channel = selectedChannel.getValue();
    if (channel != null) {
      List<Message> messages = this.messages.getValue();
      this.messages.postValue(messages);
      //noinspection DataFlowIssue
      fetchMessages(channel, getSince(messages));
    }
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    pending.clear();
    DefaultLifecycleObserver.super.onStop(owner);
  }

  private void handleChannels(List<Channel> channels) {
    this.channels.postValue(channels);
    Channel previous = this.selectedChannel.getValue();
    if (!channels.isEmpty()) {
      if (previous == null || !channels.contains(previous)) {
        setSelectedChannel(channels.get(0));
      }
    } else {
      setSelectedChannel(null);
    }
  }

  private static Instant getSince(List<Message> messages) {
    return messages.isEmpty()
        ? Instant.MIN
        : messages
            .get(messages.size() - 1)
            .getPosted();
  }

  private void postThrowable(Throwable throwable) {
    Log.e(TAG, throwable.getMessage(), throwable);
    this.throwable.postValue(throwable);
  }

}
