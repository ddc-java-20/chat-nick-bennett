package edu.cnm.deepdive.chat.service;

import edu.cnm.deepdive.chat.model.entity.Channel;
import java.util.List;
import java.util.UUID;

public interface AbstractChannelService {

  Channel get(UUID externalKey);

  Channel add(Channel channel);
  
  String setName(UUID externalKey, String name);

  String getName(UUID externalKey);

  boolean setActive(UUID externalKey, boolean active);
  
  boolean getActive(UUID externalKey);
  
  void remove(UUID externalKey);

  List<Channel> getAll();
  
  List<Channel> getAllByActive(boolean active);
  
}
