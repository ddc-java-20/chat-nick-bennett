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
      // TODO: 2025-03-19 Start a new query for messages in the selected channel.
      selectedChannel.setValue(channel);
    }
  }

  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  public void fetchChannels() {
    messageService
        .getChannels(true)
        .subscribe(
            channels::postValue,
            this::postThrowable,
            pending
        );
  }

  public void fetchMessages() {
    throwable.postValue(null);
    List<Message> messages = this.messages.getValue();
    //noinspection SequencedCollectionMethodCanBeUsed,DataFlowIssue
    Instant since = messages.isEmpty()
        ? Instant.MIN
        : messages
            .get(messages.size() -1)
            .getPosted();
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

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    pending.clear();
    DefaultLifecycleObserver.super.onStop(owner);
  }

  private void postThrowable(Throwable throwable) {
    Log.e(TAG, throwable.getMessage(), throwable);
    this.throwable.postValue(throwable);
  }

}
