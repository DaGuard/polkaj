package io.emeraldpay.polkaj.scale.writer;

import io.emeraldpay.polkaj.scale.ScaleWriter;
import io.emeraldpay.polkaj.scale.ScaleCodecWriter;

import java.io.IOException;

public class UInt8Writer implements ScaleWriter<Byte> {
    @Override
    public void write(ScaleCodecWriter wrt, Byte value) throws IOException {
        wrt.directWrite(value & 0xff);
    }
}
