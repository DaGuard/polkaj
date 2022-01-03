package io.emeraldpay.polkaj.scaletypes;

import io.emeraldpay.polkaj.scale.ScaleCodecWriter;
import io.emeraldpay.polkaj.scale.ScaleWriter;
import io.emeraldpay.polkaj.scale.UnionValue;
import io.emeraldpay.polkaj.scale.writer.ListWriter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApproveTransferWriter implements ScaleWriter<ApproveTransfer> {
    private static final ListWriter<UnionValue<MultiAddress>> SIGNATURES_WRITER = new ListWriter<>(new MultiAddressWriter());
    private static final TimepointWriter TIMEPOINT_WRITER = new TimepointWriter();

    @Override
    public void write(ScaleCodecWriter wrt, ApproveTransfer value) throws IOException {
        wrt.writeByte(31);//value.getModuleIndex());
        wrt.writeByte(2);//value.getCallIndex());
        wrt.write(ScaleCodecWriter.UINT16,value.getThreshold());
        wrt.write(SIGNATURES_WRITER, value.getOtherSignatures());
        wrt.write(TIMEPOINT_WRITER, null); //timepoint
        wrt.write(new ListWriter<Byte>(ScaleCodecWriter.UINT8), convertBytesToList(value.getHash()));//32 elements of u8
        wrt.write(ScaleCodecWriter.UINT64, BigInteger.valueOf(value.getWeight()));//u64
    }
    private static List<Byte> convertBytesToList(byte[] bytes) {
        final List<Byte> list = new ArrayList<>();
        for (byte b : bytes) {
            list.add(b);
        }
        return list;
    }

}
