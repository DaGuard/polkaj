package io.emeraldpay.polkaj.scaletypes;

import java.util.Arrays;

public class Timepoint {
    private int BlockNumber;
    private int index;
    public Timepoint(int BlockNumber, int index){
        this.BlockNumber=BlockNumber;
        this.index=index;
    }
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getBlockNumber() {
        return BlockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        BlockNumber = blockNumber;
    }
    @Override
    public String toString() {
        return "Timpoint{" +
                "BlockNumber=" + BlockNumber +
                ", index=" + index +

                '}';
    }
}
