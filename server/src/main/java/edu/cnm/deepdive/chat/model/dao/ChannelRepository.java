package edu.cnm.deepdive.chat.model.dao;

import edu.cnm.deepdive.chat.model.entity.Channel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

  List<Channel> getAllByOrderByNameAsc();

  List<Channel> getAllByActiveOrderByNameAsc(boolean active);

  Optional<Channel> findByExternalKey(UUID externalKey);

}
