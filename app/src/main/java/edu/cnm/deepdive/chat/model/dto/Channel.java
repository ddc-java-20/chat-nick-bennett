package edu.cnm.deepdive.chat.model.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.annotations.Expose;
import java.util.UUID;

public class Channel {

  @Expose(serialize = false)
  private UUID key;

  @Expose(serialize = false)
  private String name;

  @Expose(serialize = false)
  private boolean active;

  public UUID getKey() {
    return key;
  }

  public void setKey(UUID key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    boolean result;
    if (this == obj) {
      result = true;
    } else if (obj instanceof Channel other) {
      result = key.equals(other.key);
    } else {
      result = false;
    }
    return result;
  }

  @NonNull
  @Override
  public String toString() {
    return name;
  }

}
