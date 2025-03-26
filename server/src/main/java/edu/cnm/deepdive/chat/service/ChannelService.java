package edu.cnm.deepdive.chat.service;

import edu.cnm.deepdive.chat.dao.ChannelRepository;
import edu.cnm.deepdive.chat.model.entity.Channel;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class ChannelService implements AbstractChannelService {

  private final ChannelRepository channelRepository;

  @Autowired
  ChannelService(ChannelRepository channelRepository) {
    this.channelRepository = channelRepository;
  }

  @Override
  public Channel get(UUID externalKey) {
    return channelRepository
        .findByExternalKey(externalKey)
        .orElseThrow();
  }

  @Override
  public Channel add(Channel channel) {
    return channelRepository.save(channel);
  }

  @Override
  public String setName(UUID externalKey, String name) {
    return channelRepository
        .findByExternalKey(externalKey)
        .map((channel) -> {
          channel.setName(name);
          return channelRepository
              .save(channel)
              .getName();
        })
        .orElseThrow();
  }

  @Override
  public String getName(UUID externalKey) {
    return channelRepository
        .findByExternalKey(externalKey)
        .map(Channel::getName)
        .orElseThrow();
  }

  @Override
  public boolean setActive(UUID externalKey, boolean active) {
    return channelRepository
        .findByExternalKey(externalKey)
        .map((channel) -> {
          channel.setActive(active);
          return channelRepository
              .save(channel)
              .isActive();
        })
        .orElseThrow();
  }

  @Override
  public boolean getActive(UUID externalKey) {
    return channelRepository
        .findByExternalKey(externalKey)
        .map(Channel::isActive)
        .orElseThrow();
  }

  @Override
  public void remove(UUID externalKey) {
    channelRepository
        .findByExternalKey(externalKey)
        .ifPresent(channelRepository::delete);
  }

  @Override
  public List<Channel> getAll() {
    return channelRepository.getAllByOrderByNameAsc();
  }

  @Override
  public List<Channel> getAllByActive(boolean active) {
    return channelRepository.getAllByActiveOrderByNameAsc(active);
  }
}
