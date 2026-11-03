package au.edu.adelaide.Assessment1.model;

public class StatsResponse {

    private long inputTokens;
    private long outputTokens;

    public StatsResponse(long inputTokens, long outputTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    public long getInputTokens() {
        return inputTokens;
    }

    public long getOutputTokens() {
        return outputTokens;
    }
}