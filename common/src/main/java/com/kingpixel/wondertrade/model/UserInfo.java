package com.kingpixel.wondertrade.model;

import com.kingpixel.wondertrade.CobbleWonderTrade;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.Document;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 25/07/2024 1:10
 */
@Getter
@Setter
@ToString
public class UserInfo {
  private UUID playeruuid;
  private boolean messagesend;
  private long date;

  public UserInfo() {

  }

  public UserInfo(UUID playeruuid) {
    this.playeruuid = playeruuid;
    this.messagesend = false;
    this.date = new Date(1).getTime();
  }


  public UserInfo(UUID uuid, Date futureDate) {
    this.playeruuid = uuid;
    this.messagesend = false;
    this.date = futureDate.getTime();
  }

  public UserInfo(UUID playeruuid, boolean messagesend, Date date) {
    this.playeruuid = playeruuid;
    this.messagesend = messagesend;
    this.date = date.getTime();
  }

  public static Date getDateWithCooldown() {
    long currentTimeMillis = System.currentTimeMillis();
    long cooldownMillis = TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getCooldown());
    return new Date(currentTimeMillis + cooldownMillis);
  }


  public static UserInfo fromDocument(Document document) {
    UserInfo userInfo = new UserInfo();

    if (document.containsKey("playeruuid")) {
      userInfo.setPlayeruuid(UUID.fromString(document.getString("playeruuid")));
    } else {
      throw new IllegalArgumentException("Document does not contain playeruuid field");
    }

    if (document.containsKey("messagesend")) {
      userInfo.setMessagesend(document.getBoolean("messagesend"));
    } else {
      throw new IllegalArgumentException("Document does not contain messagesend field");
    }

    if (document.containsKey("date")) {
      try {
        userInfo.setDate(document.getLong("date"));
      } catch (ClassCastException ignored) {
        userInfo.setDate(document.getDate("date").getTime());
      }
    } else {
      throw new IllegalArgumentException("Document does not contain date field");
    }

    return userInfo;
  }

  public Document toDocument() {
    Document document = new Document();
    document.put("playeruuid", this.getPlayeruuid().toString());
    document.put("messagesend", this.isMessagesend());
    document.put("date", this.getDate());
    return document;
  }
}
