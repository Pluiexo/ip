import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

public class TaskManager {
    private ArrayList<Task> items;
    private boolean hasChanged = false;

    private static final String listingResponse = "Here are the tasks in your list:";


    TaskManager() {
        this.items = new ArrayList<>();
    }

    public Task addTask(Tasks options, String instruction) throws DukeException {
        Task item;
        String name;
        String by;
        String from;
        switch (options) {
            case TODO:
                name = instruction.replaceAll("todo", "").trim();
                if (name.isBlank()) {
                    throw new DukeException("description");
                }
                item = new Todo(name);
                break;
            case DEADLINE:
                //add the deadline task
                if (!instruction.contains("/") || !instruction.contains("by")) {
                    throw new DukeException("dateError");
                }
                by = instruction.split("/")[1].replaceAll("by", "").trim();
                name = instruction.split("/")[0].replaceAll("deadline", "").trim();
                if (by.isBlank()) {
                    throw new DukeException("by");
                } else if (name.isBlank()) {
                    throw new DukeException("description");
                }

                item = new Deadline(name, by);
                break;
            case EVENT:
                if (!instruction.contains("/") || !(instruction.contains("by") && instruction.contains("from"))) {
                    throw new DukeException("dateError");
                }
                from = instruction.split("/")[1].replaceAll("from", "").trim();
                by = instruction.split("/")[2].replaceAll("to", "").trim();
                name = instruction.split("/")[0].replaceAll("event", "").trim();
                if (from.isBlank()) {
                    throw new DukeException("from");
                } else if (by.isBlank()) {
                    throw new DukeException("by");
                } else if (name.isBlank()) {
                    throw new DukeException("description");
                }
                item = new Event(name, from, by);
                break;
            default:
                throw new DukeException("Invalid");
        }
        hasChanged = true;
        items.add(item);
        return item;

    }

    public Task mangeTask(Actions act, String instruction) throws DukeException {
        String[] getNumber = instruction.split(" ");
        if (items.isEmpty()) {
            throw new DukeException("empty");
        }
        if (getNumber.length <= 1 || getNumber[1].isBlank()) {
            throw new DukeException("number");
        }
        int id = Integer.parseInt(getNumber[1]) - 1; //Index 0 based
        if (id < 0 || id >= items.size()) {
            throw new DukeException("outOfRange");
        }
        Task item = items.get(id);
        switch (act) {
            case UNMARK:
                item.unmark();
                break;
            case MARK:
                item.markAsDone();
                break;
            case DELETE:
                items.remove(id);
                break;
            default:
                //This does nothing
                break;
        }
        hasChanged = true;
        return item;
    }

    public String numOfTask() {
        return "Now you have " + items.size() + " tasks in the list.";
    }


    public String getTasksSave() {
        StringBuilder returnBuilder = new StringBuilder();
        for (Task item : items) {
            returnBuilder.append(item.saveFile()); //build the save here
            returnBuilder.append("\n");
        }
        return returnBuilder.toString();
    }

    public ArrayList<String> ListItems() {

        int i = 1;
        ArrayList<String> ret = new ArrayList<>();
        if (items.isEmpty()) {
            ret.add("Your list is empty!!!!Add something! ");
            return ret;
        }
        ret.add(listingResponse);
        for (Task item : items) {
            ret.add(" " + i + "." + item);
            i++;
        }
        return ret;
    }

    public void setUpdate(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    public boolean getUpdate() {
        return hasChanged;
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
                item = new Deadline(name, by);
                break;
            case "E":
                name = data[2];
                by = data[3];
                from = data[4];
                item = new Event(name, by, from);
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
    public void testDate(String date) throws DukeException {
        Optional<LocalDate> print = DateHandler.checkDate(date,1);
        if(print.isPresent()) {
            System.out.println(print.get());
        } else {
            System.out.println("Invalid date");
        }
    }

    public void testTime(String time) throws DukeException {
        Optional<LocalTime> print = DateHandler.checkTime(time);
        if(print.isPresent()) {
            System.out.println(print.get());
        } else {
            System.out.println("Invalid time");
        }
    }

    public void loadTasksFromFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String next;
            while ((next = br.readLine()) != null) {
                if (!next.isBlank()) {
                    //Read task file
                    Task item = determineTask(next);
                    items.add(item);
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
