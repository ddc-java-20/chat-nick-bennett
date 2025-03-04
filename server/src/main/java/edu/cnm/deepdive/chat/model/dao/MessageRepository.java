package edu.cnm.deepdive.chat.model.dao;

import edu.cnm.deepdive.chat.model.entity.Channel;
import edu.cnm.deepdive.chat.model.entity.Message;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

  List<Message> getAllByChannelAndPostedAfterOrderByPostedAsc(Channel channel, Instant posted);

}
