package io.emeraldpay.polkaj.scaletypes;

import io.emeraldpay.polkaj.scale.ScaleCodecWriter;
import io.emeraldpay.polkaj.scale.ScaleWriter;
import io.emeraldpay.polkaj.scale.UnionValue;
import io.emeraldpay.polkaj.scale.writer.UnionWriter;

import java.io.IOException;
import java.util.Arrays;

public class TimepointWriter implements ScaleWriter<Timepoint> {


    @Override
    public void write(ScaleCodecWriter wrt, Timepoint value) throws IOException {
        //wrt.writeByte(0); null
    }


}
