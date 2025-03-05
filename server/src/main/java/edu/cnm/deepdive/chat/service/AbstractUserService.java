package edu.cnm.deepdive.chat.service;

import edu.cnm.deepdive.chat.model.entity.User;
import java.net.URL;
import java.util.UUID;

public interface AbstractUserService {

  User getCurrent();

  User get(UUID externalKey);

  User getOrCreate(String oauthKey, String displayName);

  User update(User user);

}
