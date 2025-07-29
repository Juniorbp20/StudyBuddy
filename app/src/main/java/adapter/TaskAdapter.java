package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studybuddy.R;
import model.Task;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        Task currentTask = tasks.get(position);
        holder.textViewTitle.setText(currentTask.getTitle());
        holder.textViewDate.setText(currentTask.getDate());
        holder.textViewTime.setText(currentTask.getTime());
        holder.checkBoxCompleted.setChecked(currentTask.isCompleted());
        // Opcional: Cambiar estilo si está completada (tachado, color gris)
        if (currentTask.isCompleted()) {
            holder.textViewTitle.setAlpha(0.5f);
            holder.textViewDate.setAlpha(0.5f);
            holder.textViewTime.setAlpha(0.5f);
        } else {
            holder.textViewTitle.setAlpha(1.0f);
            holder.textViewDate.setAlpha(1.0f);
            holder.textViewTime.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    class TaskHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDate;
        private TextView textViewTime;
        private CheckBox checkBoxCompleted;

        public TaskHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_task_title);
            textViewDate = itemView.findViewById(R.id.text_view_task_date);
            textViewTime = itemView.findViewById(R.id.text_view_task_time);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_task_completed);

            // Manejar clic en el item (opcional)
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(tasks.get(position));
                }
            });

            // Manejar cambio en el checkbox (para marcar/desmarcar completado)
            checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Notificar al listener del cambio de estado
                    if (listener != null) {
                        listener.onTaskStatusChanged(tasks.get(position), isChecked);
                    }
                }
            });
        }
    }

    // Interface para manejar clics en items
    public interface OnItemClickListener {
        void onItemClick(Task task);
        void onTaskStatusChanged(Task task, boolean isCompleted);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}