package edu.cnm.deepdive.chat.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import dagger.hilt.android.AndroidEntryPoint;
import edu.cnm.deepdive.chat.R;
import edu.cnm.deepdive.chat.adapter.MessageAdapter;
import edu.cnm.deepdive.chat.databinding.FragmentHomeBinding;
import edu.cnm.deepdive.chat.model.dto.Channel;
import edu.cnm.deepdive.chat.model.dto.Message;
import edu.cnm.deepdive.chat.viewmodel.LoginViewModel;
import edu.cnm.deepdive.chat.viewmodel.MessageViewModel;
import java.util.List;
import javax.inject.Inject;

/** @noinspection SequencedCollectionMethodCanBeUsed*/
@AndroidEntryPoint
public class HomeFragment extends Fragment implements MenuProvider, OnItemSelectedListener {

  private static final String TAG = HomeFragment.class.getSimpleName();

  @Inject
  MessageAdapter adapter;

  private FragmentHomeBinding binding;
  private LoginViewModel loginViewModel;
  private MessageViewModel messageViewModel;
  private Channel selectedChannel;
  private List<Channel> channels;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    // TODO: 2025-03-19 Attach listener to send button, so that when clicked, a new Message instance
    //  is created and passed to messageViewModel.
    binding.messages.setAdapter(adapter);
    binding.channels.setOnItemSelectedListener(this);
    binding.send.setOnClickListener((v) -> {
      Message message = new Message();
      //noinspection DataFlowIssue
      message.setText(binding.message.getText().toString().strip());
      messageViewModel.sendMessage(message);
      binding.message.setText("");
    });
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    LifecycleOwner owner = getViewLifecycleOwner();
    setupLoginViewModel(owner);
    setupMessageViewModel(owner);
    requireActivity().addMenuProvider(this, owner, State.RESUMED);
  }

  @Override
  public void onDestroyView() {
    binding = null;
    super.onDestroyView();
  }

  @Override
  public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.home_options, menu);
  }

  @Override
  public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
    boolean handled = false;
    if (menuItem.getItemId() == R.id.sign_out) {
      loginViewModel.signOut();
      handled = true;
    }
    return handled;
  }

  private void setupLoginViewModel(LifecycleOwner owner) {
    loginViewModel = new ViewModelProvider(requireActivity())
        .get(LoginViewModel.class);
    loginViewModel
        .getAccount()
        .observe(owner, (account) -> {
          if (account == null) {
            Navigation.findNavController(binding.getRoot())
                .navigate(HomeFragmentDirections.navigateToPreLoginFragment());
          }
        });
  }

  private void setupMessageViewModel(LifecycleOwner owner) {
    messageViewModel = new ViewModelProvider(this)
        .get(MessageViewModel.class);
    getLifecycle().addObserver(messageViewModel);
    messageViewModel
        .getChannels()
        .observe(owner, (channels) -> {
          this.channels = channels;
          ArrayAdapter<Channel> adapter = new ArrayAdapter<>(requireContext(),
              android.R.layout.simple_list_item_1, channels);
          binding.channels.setAdapter(adapter);
          setChannelSelection();
        });
    messageViewModel
        .getMessages()
        .observe(owner, messages -> {
          adapter.setMessages(messages);
          binding.messages.scrollToPosition(messages.size() - 1);
        });
    messageViewModel
        .getSelectedChannel()
        .observe(owner, (channel) -> {
          selectedChannel = channel;
          setChannelSelection();
          adapter.setMessages(List.of());
        });
  }

  private void setChannelSelection() {
    if (channels != null && selectedChannel != null) {
      int position = channels.indexOf(selectedChannel);
      if (position >= 0) {
        binding.channels.setSelection(position, true);
      }
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    Channel channel = (Channel) parent.getItemAtPosition(position);
    messageViewModel.setSelectedChannel(channel);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Ignore; this doesn't happen with a Spinner.
  }

}
