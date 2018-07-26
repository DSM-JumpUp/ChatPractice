package dasilver.jeong.chatpracticeandroid;

public class ChatRecyclerItem {
    String messageText, timeText, dateText;
    private int itemViewType;

    public String getMessageText() {
        return messageText;
    }

    public String getTimeText() {
        return timeText;
    }

    public String getDateText() {
        return dateText;
    }

    public int getItemViewType() {
        return itemViewType;
    }

    ChatRecyclerItem(int itemViewType, String messageText, String timeText) {
        this.itemViewType = itemViewType;
        this.messageText = messageText;
        this.timeText = timeText;
    }

    ChatRecyclerItem(int itemViewType, String dateText) {
        this.itemViewType = itemViewType;
        this.dateText = dateText;
    }
}
