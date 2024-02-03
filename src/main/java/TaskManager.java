import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskManager {
    private ArrayList<Task> items;
    private boolean hasChanged = false;
    private static final String INDENT = "   ";

    private static final String listingResponse = "Here are the tasks in your list:";

    //Strings for marking and unmarking

    private static final String RESPONSE_MARK = "Nice! I've marked this task as done:";
    private static final String RESPONSE_UMARK = "OK, I've marked this task as not done yet:";
    private static final String RESPONSE_REMOVE = "Noted. I've removed this task";


    //String and variables for task
    private static final String RESPONSE_ADD = "Got it. I've added this task:";


    TaskManager() {
        this.items = new ArrayList<>();
    }

    public String addTask(Actions options, String instruction) throws DukeException {
        Task item;
        String description;
        String by;
        String from;
        Pattern deadlineFormat = Pattern.compile("(?<description>.+)\\s?((?i)/by)(?<by>.+)");
        Pattern eventFormat = Pattern.compile("(?<description>.+)\\s?((?i)/from)(?<from>.+)((?i)/to)(?<by>.+)");
        switch (options) {
            case TODO:
                if (instruction.isBlank()) {
                    throw new DukeException("description");
                }
                item = new Todo(instruction);
                break;
            case DEADLINE:
                //add the deadline task
                Matcher deadlineMatch = deadlineFormat.matcher(instruction);
                if (!deadlineMatch.find()) {
                    throw new DukeException("GIGABOOOM");
                }
                description = deadlineMatch.group("description");
                by = deadlineMatch.group("by").trim();
                if (description.isBlank()) {
                    throw new DukeException("description");
                } else if (by.isBlank()) {
                    throw new DukeException("by");
                }
                Optional<LocalDate> testDate = DateHandler.checkDate(by);
                LocalTime testTime = DateHandler.checkTime(by).orElse(LocalTime.of(0, 0));
                item = testDate.map(localDate -> new Deadline(description, LocalDateTime.of(localDate, testTime)))
                        .orElseGet(() -> new Deadline(description, by));
                break;
            case EVENT:
                Matcher eventMatch = eventFormat.matcher(instruction);
                if (!eventMatch.find()) {
                    System.out.println("reached here");
                    throw new DukeException("GIGABOOOM");
                }
                description = eventMatch.group("description");
                by = eventMatch.group("by").trim();
                from = eventMatch.group("from").trim();
                if (from.isBlank()) {
                    throw new DukeException("from");
                } else if (by.isBlank()) {
                    throw new DukeException("by");
                } else if (description.isBlank()) {
                    throw new DukeException("description");
                }
                Optional<LocalDate> testByDate = DateHandler.checkDate(by);
                Optional<LocalDate> testFromDate = DateHandler.checkDate(from);
                LocalTime testByTime = DateHandler.checkTime(by).orElse(LocalTime.of(0, 0));
                LocalTime testFromTime = DateHandler.checkTime(from).orElse(LocalTime.of(0, 0));

                if (testByDate.isPresent() && testFromDate.isPresent()) {
                    item = new Event(description, LocalDateTime.of(testFromDate.get(), testFromTime),
                            LocalDateTime.of(testByDate.get(), testByTime));
                } else {
                    item = new Event(description, from, by);
                }
                break;
            default:
                throw new DukeException("Invalid");
        }
        hasChanged = true;
        items.add(item);
        return RESPONSE_ADD + "\n" + INDENT + item + "\n" + INDENT + numOfTask();

    }

    public String manageTask(Manage act, String instruction) throws DukeException {
        if (items.isEmpty()) {
            throw new DukeException("empty");
        }
        if (instruction.isBlank()) {
            throw new DukeException("number");
        }
        int id = Integer.parseInt(instruction) - 1; //Index 0 based
        if (id < 0 || id >= items.size()) {
            throw new DukeException("outOfRange");
        }
        Task item = items.get(id);
        String response = "";
        switch (act) {
            case UNMARK:
                item.unmark();
                response = RESPONSE_UMARK;
                break;
            case MARK:
                response = RESPONSE_MARK;
                item.markAsDone();
                break;
            case DELETE:
                response = RESPONSE_REMOVE;
                items.remove(id);
                break;
            default:
                //This does nothing
                break;
        }
        hasChanged = true;
        if (act.equals(Manage.MARK) || act.equals(Manage.UNMARK)) {
            return response + "\n" + INDENT + item;
        } else {
            //Should be delete bu default
            return response + "\n" + INDENT + item + "\n" + INDENT + numOfTask();
        }
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
        Optional<LocalDate> print = DateHandler.checkDate(date);
        if (print.isPresent()) {
            System.out.println(print.get());
        } else {
            System.out.println("Invalid date");
        }
    }

    public void testTime(String time) throws DukeException {
        Optional<LocalTime> print = DateHandler.checkTime(time);
        if (print.isPresent()) {
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
