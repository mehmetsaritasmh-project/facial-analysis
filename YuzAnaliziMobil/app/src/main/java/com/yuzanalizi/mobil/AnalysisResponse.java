public class AnalysisResponse<T> {
    private T measurements; // Flutter'daki _measurements
    private T labels;       // Flutter'daki _labels
    private int statusCode;

    public AnalysisResponse(T measurements, T labels, int statusCode) {
        this.measurements = measurements;
        this.labels = labels;
        this.statusCode = statusCode;
    }

    public T getMeasurements() { return measurements; }
    public T getLabels() { return labels; }
}