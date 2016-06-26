package example.testtask.eventqueue;

/**
 *
 */
public class MyEvent {
    /**
     * Event name.
     */
    private String name;

    /**
     *
     * @param name
     */
    public MyEvent(String name) {
        this.name = name;
    }
    
    public String getName() {
    	return name;
    }

    /**
     * Executes event.
     */
    public void execute() throws InterruptedException {
        System.out.println("Executing event. [event=" + toString() + ']');
        Thread.currentThread().sleep(100);
        System.out.println("Execution complete. [event=" + toString() + ']');
    }

    /**
     * @return
     */
    public String toString() {
        return "MyEvent[name=" + name + ']';
    }
}
