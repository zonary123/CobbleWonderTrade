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
  private Date date;

  public UserInfo() {

  }

  public UserInfo(UUID playeruuid) {
    this.playeruuid = playeruuid;
    this.messagesend = false;
    this.date = new Date(1);
  }


  public UserInfo(UUID uuid, Date futureDate) {
    this.playeruuid = uuid;
    this.messagesend = false;
    this.date = futureDate;
  }

  public UserInfo(UUID playeruuid, boolean messagesend, Date date) {
    this.playeruuid = playeruuid;
    this.messagesend = messagesend;
    this.date = date;
  }

  public static Date getDateWithCooldown() {
    long currentTimeMillis = System.currentTimeMillis();
    long cooldownMillis = TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getCooldown());
    return new Date(currentTimeMillis + cooldownMillis);
  }


  public static UserInfo fromDocument(Document document) {
    UserInfo userInfo = new UserInfo();

    // Asegúrate de que los campos existan en el documento
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
      Object dateObj = document.get("date");
      if (dateObj instanceof Date) {
        userInfo.setDate((Date) dateObj);
      } else {
        throw new IllegalArgumentException("Date field in document is not of type Date");
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
