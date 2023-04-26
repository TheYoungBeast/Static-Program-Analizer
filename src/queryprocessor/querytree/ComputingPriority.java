package queryprocessor.querytree;

public enum ComputingPriority
{
    HIGHEST(4), // The very light computational demand
    HIGH(3), // light computational demand
    NORMAL(2), // Normal computational demand
    LOW(1); // Heavy computational demand

    private final int priority;

    ComputingPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
