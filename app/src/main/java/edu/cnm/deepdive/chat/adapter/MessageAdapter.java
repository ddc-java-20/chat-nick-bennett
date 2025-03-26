package edu.cnm.deepdive.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.FragmentScoped;
import edu.cnm.deepdive.chat.databinding.ItemMessageBinding;
import edu.cnm.deepdive.chat.model.dto.Message;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

@FragmentScoped
public class MessageAdapter extends Adapter<ViewHolder> {

  private final LayoutInflater inflater;
  private final List<Message> messages;
  private final DateTimeFormatter formatter;

  @Inject
  MessageAdapter(@ActivityContext Context context) {
    inflater = LayoutInflater.from(context);
    messages = new ArrayList<>();
    formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int itemType) {
    return new Holder(ItemMessageBinding.inflate(inflater, parent, false), formatter);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
    ((Holder) viewHolder).bind(messages.get(position));
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }

  public void setMessages(@NonNull List<Message> messages) {
    if (!messages.isEmpty()) {
      int size = messages.size();
      this.messages.clear();
      this.messages.addAll(messages);
      notifyItemRangeInserted(size, messages.size());
    } else {
      int size = this.messages.size();
      this.messages.clear();
      notifyItemRangeRemoved(0, size);
    }
  }

  private static class Holder extends ViewHolder {

    private final ItemMessageBinding binding;
    private final DateTimeFormatter formatter;

    Holder(ItemMessageBinding binding, DateTimeFormatter formatter) {
      super(binding.getRoot());
      this.binding = binding;
      this.formatter = formatter;
    }

    void bind(Message message) {
      binding.sender.setText(message.getSender().getDisplayName());
      binding.posted.setText(message.getPosted().atZone(ZoneId.systemDefault()).format(formatter));
      binding.text.setText(message.getText());
    }

  }

}
