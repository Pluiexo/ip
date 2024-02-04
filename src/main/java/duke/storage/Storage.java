package duke.storage;

import duke.DukeException;
import duke.task.*;

import java.io.*;
import java.time.LocalDateTime;

public class Storage {
    private String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
    }

    //Try to load the task
    //And get the items
    public TaskManager loadFile() throws DukeException {
        TaskManager manager = new TaskManager();
        File directory = new File("data");
        if (!directory.exists()) {
            directory.mkdir();
        } else {
            File storage = new File(filePath);
            try {
                if (!storage.createNewFile()) {

                    loadTasksFromFile(new File(filePath), manager);
                }
            } catch (IOException e) {
                throw new DukeException("Stupid thing won't load");
            }
        }
        return manager;
    }

    public void saveFile(TaskManager manager) {
        if (manager.getUpdate()) {
            try (FileWriter fw = new FileWriter(filePath)) {
                fw.write(manager.getTasksSave());
                manager.setUpdate(false);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void loadTasksFromFile(File file, TaskManager manager) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String next;
            while ((next = br.readLine()) != null) {
                if (!next.isBlank()) {
                    //Read task file
                    Task item = determineTask(next);
                    manager.addItem(item);
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private Task determineTask(String task) {
        String[] data = task.split("\\|");
        String type = data[0];
        Task item;
        String name;
        String by;
        String from;
        switch (type) {
            case "D":
                name = data[2];
                by = data[3];
                String temp = data[4];
                if (!temp.equals("null")) {
                    item = new Deadline(name, LocalDateTime.parse(temp.trim()));
                } else {
                    item = new Deadline(name, by);
                }
                break;
            case "E":
                name = data[2];
                by = data[3];
                from = data[4];
                String tempBy = data[5];
                String tempFrom = data[6];
                if (!(tempBy.equals("null") || tempFrom.equals("null"))) {
                    item = new Event(name, LocalDateTime.parse(tempFrom.trim()), LocalDateTime.parse(tempBy.trim()));
                } else {
                    item = new Event(name, by, from);
                }

                break;
            case "T":
                name = data[2];
                item = new Todo(name);
                break;
            default:
                item = new Task(data[2]);
                break;
        }
        String isDone = data[1];
        if (isDone.equals("x")) {
            item.markAsDone();
        }
        return item;

    }


}