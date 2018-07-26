package dasilver.jeong.chatpracticeandroid;

public class ChatRecyclerItem {
    String messageText, timeText;
    private int itemViewType;

    public String getMessageText() {
        return messageText;
    }

    public String getTimeText() {
        return timeText;
    }


    public int getItemViewType() {
        return itemViewType;
    }

    ChatRecyclerItem(int itemViewType, String messageText, String timeText) {
        this.itemViewType = itemViewType;
        this.messageText = messageText;
        this.timeText = timeText;
    }

}
