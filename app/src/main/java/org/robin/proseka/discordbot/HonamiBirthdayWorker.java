package org.robin.proseka.discordbot;

import net.dv8tion.jda.api.JDA;

public class HonamiBirthdayWorker {
  private JDA api = null;
  private final String BIRTHDAY_CHANNEL_ID = "1557068704003661846"; // 誕生日を祝うチャンネルのID

  public HonamiBirthdayWorker(JDA api) {
    this.api = api;
  }

  public void run() {
    int prevDay = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Tokyo")).getDayOfMonth();
    while (true) {
      try {
        var date = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Tokyo"));
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        // 10月27日が誕生日なので、前回の実行日と異なる場合にメッセージを送信
        if (prevDay != day && month == 10 && day == 27) {
          var channel = api.getTextChannelById(BIRTHDAY_CHANNEL_ID);
          if (channel != null) {
            channel.sendMessage("今日は私の誕生日だね！その...お祝いとかしてくれると、嬉しいな").queue();
          }
        }
        prevDay = day; // 前回の実行日を更新
        Thread.sleep(1000 * 60); // 1分ごとに実行
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
