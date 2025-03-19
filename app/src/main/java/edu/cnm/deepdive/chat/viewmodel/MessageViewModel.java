package edu.cnm.deepdive.chat.viewmodel;

import android.util.Log;
import androidx.annotation.NonNull;
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
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class MessageViewModel extends ViewModel implements DefaultLifecycleObserver {

  private static final String TAG = MessageViewModel.class.getSimpleName();

  private final MessageService messageService;
  private final MutableLiveData<List<Message>> messages;
  private final MutableLiveData<List<Channel>> channels;
  private final MutableLiveData<Channel> selectedChannel;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;

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

  public void setSelectedChannel(@NonNull Channel channel) {
    if (!channel.equals(selectedChannel.getValue())) {
      messages.setValue(new LinkedList<>());
      selectedChannel.setValue(channel);
      fetchMessages();
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
            channels::postValue,
            this::postThrowable,
            pending
        );
  }

  /**
   * @noinspection DataFlowIssue
   */
  public void fetchMessages() {
    throwable.postValue(null);
    List<Message> messages = this.messages.getValue();
    Instant since = getSince(messages);
    messageService
        .getMessages(selectedChannel.getValue().getKey(), since)
        .subscribe(
            (msgs) -> {
              messages.addAll(msgs);
              this.messages.postValue(messages);
              fetchMessages();
            },
            this::postThrowable,
            pending
        );
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
    if (selectedChannel.getValue() != null) {
      fetchMessages();
    }
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    pending.clear();
    DefaultLifecycleObserver.super.onStop(owner);
  }

  private static Instant getSince(List<Message> messages) {
    //noinspection SequencedCollectionMethodCanBeUsed
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
