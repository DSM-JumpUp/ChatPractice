package dasilver.jeong.chatpracticeandroid;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ChatRecyclerDateViewHolder extends RecyclerView.ViewHolder {
    public TextView chatDateText;

    public ChatRecyclerDateViewHolder(View itemView) {
        super(itemView);
        chatDateText = (TextView)itemView.findViewById(R.id.chat_date);
    }
}
