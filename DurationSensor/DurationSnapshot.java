package com.mycompany.app;

public record DurationSnapshot(long avg, long max, long min, long total) implements Duration {

    @Override
    public long getAvg() { 
        return avg; 
    }

    @Override
    public long getMax() { 
        return max; 
    }

    @Override
    public long getMin() { 
        return min; 
    }

    @Override
    public long getTotal() { 
        return total; 
    }
}
