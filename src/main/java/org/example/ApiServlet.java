package org.example;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {
    private final List<Task> tasks = new ArrayList<>();
    private final Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write(gson.toJson(tasks));
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    , IOException{
        Task newTask = gson.fromJson(request.getReader(), Task.class);
        tasks.add(newTask);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        int taskId = Integer.parseInt(request.getPathInfo().substring(1));
        Task updatedTask = gson.fromJson(request.getReader(), Task.class);

        for (Task task : tasks) {
            if (task.getId() == taskId) {
                task.setTitle(updatedTask.getTitle());
                task.setCompleted(updatedTask.isCompleted());
                break;
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int taskId = Integer.parseInt(request.getPathInfo().substring(1));
        tasks.removeIf(task -> task.getId() == taskId);
    }

}
