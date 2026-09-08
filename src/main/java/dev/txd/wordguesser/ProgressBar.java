package dev.txd.wordguesser;

import java.io.PrintStream;

public class ProgressBar {
    
    private PrintStream out;
    private String label;
    private int width;
    
    private boolean visible;
    private char fillChar;
    private char emptyChar;
    private boolean hasBorders;

    private boolean displayPercentage;
    private int line;
    private int column;
    private int totalProgress;
    private int currentProgress;
    private boolean completed;
    private Runnable onComplete;

    
    private ProgressBar(Builder builder) {
        this.out = builder.out;
        this.label = builder.label;
        this.width = builder.width;
        this.visible = builder.visible;
        this.fillChar = builder.fillChar;
        this.emptyChar = builder.emptyChar;
        this.hasBorders = builder.hasBorders;
        this.displayPercentage = builder.displayPercentage;
        this.line = builder.line;
        this.column = builder.column;
        this.totalProgress = builder.totalProgress;
        this.onComplete = builder.onComplete;
    }

    public void advance() {
        advanceBy(1);
    }

    public void advanceBy(int ammount) {
        setProgress(currentProgress + ammount);
    }

    public void setProgress(int progress) {
        currentProgress = progress;

        displayBar();
        if (currentProgress >= totalProgress) {
            currentProgress = totalProgress;
            completed = true;
            if (onComplete != null) onComplete.run();
        }
    }

    public int getTotalWidth() {
        return label.length() + 1 + (hasBorders ? 2 : 0) + width + (displayPercentage ? 4 : 0);
    }

    public float getProgresFloat() {
        return currentProgress / totalProgress;
    }

    private void displayBar() {
        if (!visible) return;
        StringBuilder bar = new StringBuilder("\033[5" + line + ";" + column);
        if (!label.isBlank()) bar.append(label + " ");
        if (hasBorders) bar.append("[");
        float progress = getProgresFloat();
        bar.append(String.valueOf(fillChar).repeat((int)(progress * width)));
        bar.append(String.valueOf(emptyChar).repeat(width - (int)(progress * width)));
        if (hasBorders) bar.append("]");
        if (displayPercentage) {
            StringBuilder percentageText = new StringBuilder(String.valueOf((int)(progress * 100)));
            percentageText.insert(0, " ".repeat(3 - percentageText.length()));
            
            bar.append(" " + percentageText + "%");
        }
        out.print("\033[u");
        out.print(bar);
    }

    public static class Builder {

        private PrintStream out = System.out;
        private String label = "";
        private int width = 20;
        
        private boolean visible = true;
        private char fillChar = '█';
        private char emptyChar = '░';
        private boolean hasBorders = true;

        private boolean displayPercentage = true;
        private int line = 0;
        private int column = 0;
        private int totalProgress = 100;
        private Runnable onComplete = null;

        public Builder() {

        }

        public Builder out(PrintStream out) {
            this.out = out;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Builder fillChar(char fillChar) {
            this.fillChar = fillChar;
            return this;
        }

        public Builder emptyChar(char emptyChar) {
            this.emptyChar = emptyChar;
            return this;
        }

        public Builder hasBorders(boolean hasBorders) {
            this.hasBorders = hasBorders;
            return this;
        } 

        public Builder displayPercentage(boolean displayPercentage) {
            this.displayPercentage = displayPercentage;
            return this;
        }

        public Builder line(int line) {
            this.line = line;
            return this;
        }

        public Builder column(int column) {
            this.column = column;
            return this;
        }

        public Builder totalProgress(int totalProgress) {
            this.totalProgress = totalProgress;
            return this;
        }

        public Builder onComplete(Runnable onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        public ProgressBar build() {
            return new ProgressBar(this);
        }
    }
}