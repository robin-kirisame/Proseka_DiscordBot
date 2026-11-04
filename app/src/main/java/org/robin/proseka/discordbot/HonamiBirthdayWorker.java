package org.robin.proseka.discordbot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.JDA;

public class HonamiBirthdayWorker {
  private JDA api = null;
  private int myBirthday = 0;
  private int myBirthMonth = 0;
  private final String BIRTHDAY_CHANNEL_ID = "1557068704003661846"; // 誕生日を祝うチャンネルのID
  private final String myName = "望月穂波";

  private record Profile(String name, int birthMonth, int birthDay, String pronoun) {}

  private List<Profile> characters = new ArrayList<>();
  private List<Profile> virtualSingers = new ArrayList<>();
  private List<Profile> users = new ArrayList<>();


  public HonamiBirthdayWorker(JDA api) {
    this.api = api;
    CreateBirthday();
    Optional<Profile> honami = characters.stream().filter(chara -> findMe(chara)).findFirst();
    honami.ifPresent(val -> setMyBirthday(val));
  }

  public void run() {
    int prevDay = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Tokyo")).getDayOfMonth();
    while (true) {
      try {
        var date = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Tokyo"));
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        // 穂波の誕生日祝い
        if (prevDay != day && month == myBirthMonth && day == myBirthday) {
          var channel = api.getTextChannelById(BIRTHDAY_CHANNEL_ID);
          if (channel != null) {
            channel.sendMessage("今日は私の誕生日だね！その...お祝いとかしてくれると、嬉しいな").queue();
          }
        }
        // 穂波以外の誕生日祝い
        StringBuilder nameList = new StringBuilder();
        int matchingCount = 0;
        for (Profile character : characters) {
          if (prevDay != day && month == character.birthMonth() && day == character.birthDay()) {
            if (character.name == myName) continue; // 穂波の場合は上で処理したのでスキップする
            if (matchingCount > 0) nameList.append("と");
            nameList.append(character.pronoun());
            matchingCount++;
          }
        }
        if (matchingCount > 0) {
          var channel = api.getTextChannelById(BIRTHDAY_CHANNEL_ID);
          if (channel != null) {
            channel.sendMessage("今日は" + nameList.toString() + "の誕生日なんだ！お祝いしてくれると嬉しいな。").queue();
          }
        }
        prevDay = day; // 前回の実行日を更新
        Thread.sleep(1000 * 60); // 1分ごとに実行
      } catch (InterruptedException e) {
        e.printStackTrace();
        break; // スレッドが中断された場合はループを抜ける
      }
    }
  }

  private void CreateBirthday() {
    // 誕生日のキャラクターを作成する処理
    characters.add(new Profile("望月穂波", 10, 27, ""));
    // 他のキャラクターも同様に追加することができます
    characters.add(new Profile("宵崎奏", 2, 10, "宵崎さん"));

    characters.add(new Profile("星乃一歌", 8, 11, "一歌ちゃん"));

    virtualSingers.add(new Profile("初音ミク", 8, 31, "ミクちゃん"));
  }

  private boolean findMe(Profile chara) {
    return chara.name == myName;
  }

  private void setMyBirthday(Profile chara) {
    myBirthMonth = chara.birthMonth();
    myBirthday = chara.birthDay();
  }
}
