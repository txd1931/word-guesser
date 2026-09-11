package dev.txd.wordguesser;

import java.io.PrintStream;

public class ProgressBar {
    

    public static final String ANSI_LIGHT_BLUE = "\u001B[94m";
    private static final String ANSI_RESET = "\u001B[0m";

    private PrintStream out;
    private String label;
    private int width;
    
    private boolean visible;
    private char fillChar;
    private char emptyChar;
    private boolean hasBorders;

    private boolean displayProgress;
    private int line;
    private int column;
    private int totalValue;
    private int value;
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
        this.displayProgress = builder.displayProgress;
        this.line = builder.line;
        this.column = builder.column;
        this.totalValue = builder.totalValue;
        this.onComplete = builder.onComplete;
    }

    public void advance() {
        advanceBy(1);
    }

    public void advanceBy(int ammount) {
        setValue(value + ammount);
    }

    public void setValue(int value) {
        this.value = value;

        displayBar();
        if (value >= totalValue) {
            value = totalValue;
            completed = true;
            if (onComplete != null) onComplete.run();
        }
    }

    public int getValue() {
        return value;
    }

    public int getTotalWidth() {
        return label.length() + 1 + (hasBorders ? 2 : 0) + width + (displayProgress ? 4 : 0);
    }

    public int getWitdth() {
        return width;
    }

    public float getProgress() {
        return (float)value / (float)totalValue;
    }

    private void displayBar() {
        if (!visible) return;
        StringBuilder bar = new StringBuilder("\033[" + line + ";" + column + "H");
        if (!label.isBlank()) bar.append(label + " ");
        float progress = getProgress();
        if (progress == 1) bar.append(ANSI_LIGHT_BLUE);
        if (hasBorders) bar.append("[");
        bar.append(String.valueOf(fillChar).repeat((int)(progress * width)));
        bar.append(String.valueOf(emptyChar).repeat(width - (int)(progress * width)));
        if (hasBorders) bar.append("]");
        if (progress == 1) bar.append(ANSI_RESET);
        if (displayProgress) {
            StringBuilder percentageText = new StringBuilder(String.valueOf((int)(progress * 100)));
            percentageText.insert(0, " ".repeat(3 - percentageText.length()));
            
            bar.append(" " + percentageText + "%");
        }
        bar.append("\n");
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

        private boolean displayProgress = true;
        private int line = 0;
        private int column = 0;
        private int totalValue = 100;
        private Runnable onComplete = null;
        
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

        public Builder displayProgress(boolean displayProgress) {
            this.displayProgress = displayProgress;
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

        public Builder totalValue(int totalValue) {
            this.totalValue = totalValue;
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