package org.robin.proseka.discordbot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
  ZoneId zone = ZoneId.of("Asia/Tokyo");
  LocalDate prevDay = LocalDate.now(zone);
    while (true) {
      try {
        LocalDate date = LocalDate.now(zone);
        if(!date.equals(prevDay)){
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        var channel = api.getTextChannelById(BIRTHDAY_CHANNEL_ID);

          if (channel != null) {
        // 穂波の誕生日祝い
        if (month == myBirthMonth && day == myBirthday) {
          if (channel != null) {
            channel.sendMessage("今日は私の誕生日だね！その...お祝いとかしてくれると、嬉しいな").queue();
          }
        }
        // 穂波以外の誕生日祝い
        StringBuilder nameList = new StringBuilder();
        for (Profile character : characters) {
          if (!findMe(character)// 穂波の場合は上で処理したのでスキップする
            && month == character.birthMonth() 
            && day == character.birthDay()) {
            if (nameList.length() > 0) nameList.append("と");
            nameList.append(character.pronoun());
          }
        }
        //バーチャルシンガーの判定
        StringBuilder aniversaryNameList = new StringBuilder();
        for (Profile virtualSingers : virtualSingers) {
          if (month == virtualSingers.birthMonth() && day == virtualSingers.birthDay()) {
            if (aniversaryNameList.length() > 0) aniversaryNameList.append("と");
            aniversaryNameList.append(virtualSingers.pronoun());
             }
          }
        if (aniversaryNameList.length() > 0) {
        channel.sendMessage("今日は" + aniversaryNameList.toString() + "の記念日なんだ！お祝いしてくれると嬉しいな。").queue();
        }
        // キャラクターの名前を追加した後、同じnameListにユーザーも追加する。
        for (Profile user : users) {
        if (month == user.birthMonth() && day == user.birthDay()) {
          if (nameList.length() > 0) nameList.append("と");
          nameList.append(user.pronoun());
          }
        }
        if (nameList.length() > 0) {
        channel.sendMessage("今日は" + nameList.toString() + "の誕生日なんだ！お祝いしてくれると嬉しいな。").queue();
        }
      }
        prevDay = date; 
        }// 前回の実行日を更新
        Thread.sleep(1000 * 60); // 1分ごとに実行
      } catch (InterruptedException e) {
        e.printStackTrace();
        break; // スレッドが中断された場合はループを抜ける
      }
    }
  }

  private void CreateBirthday() {
    
    try(var input = HonamiBirthdayWorker.class.getResourceAsStream("/charactors.json")){

      if(input == null){
        throw new IllegalStateException("名簿が見つからない");
      }

      String text = new String(input.readAllBytes(),StandardCharsets.UTF_8);
      JSONObject root = new JSONObject(text);

      loadprofiles(root.getJSONArray("characters"),characters);
      loadprofiles(root.getJSONArray("virtualSingers"),virtualSingers);

      System.out.println("Loaded characters: " + characters.size()+ ", virtual singers: " + virtualSingers.size());
     } catch (IOException | JSONException e) {
      throw new IllegalStateException("Failed to load birthday JSON", e);
    }
  }

  private void loadprofiles(JSONArray array, List<Profile> destination){
    for(int i = 0; i <array.length();i++){
      JSONObject item = array.getJSONObject(i);
    
    destination.add(new Profile(
      item.getString("name"),
      item.getInt("birthMonth"),
      item.getInt("birthDay"),
      item.getString("pronoun")
    ));
    }
  }

  private boolean findMe(Profile chara) {
    return myName.equals(chara.name());
  }

  private void setMyBirthday(Profile chara) {
    myBirthMonth = chara.birthMonth();
    myBirthday = chara.birthDay();
  }
}