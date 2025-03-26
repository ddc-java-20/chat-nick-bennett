package edu.cnm.deepdive.chat.dao;

import edu.cnm.deepdive.chat.model.entity.Channel;
import edu.cnm.deepdive.chat.model.entity.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {

  String LATEST_POSTED_QUERY = """
      SELECT
        m.posted
      FROM
        Message AS m
      WHERE
        m.channel = :channel
        AND m.posted > :posted
      ORDER BY
        m.posted ASC
      """;

  List<Message> getAllByChannelAndPostedAfterOrderByPostedAsc(Channel channel, Instant posted);

  @Query(LATEST_POSTED_QUERY)
  List<Instant> getLastPostedByChannelAndPostedAfter(
      Channel channel, Instant posted, Pageable pageable);

}
