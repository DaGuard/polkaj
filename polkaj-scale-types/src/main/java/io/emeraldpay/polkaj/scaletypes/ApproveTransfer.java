package io.emeraldpay.polkaj.scaletypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.emeraldpay.polkaj.scale.UnionValue;
import io.emeraldpay.polkaj.types.Address;

import io.emeraldpay.polkaj.scaletypes.Timepoint;
import io.emeraldpay.polkaj.types.DotAmount;

/**
 * Call to transfer [part of] balance to another address
 */
public class ApproveTransfer extends ExtrinsicCall {
    private int threshold;
    private List<UnionValue<MultiAddress>> otherSignatures;
    private Timepoint timepoint;
    private byte[] hash;
    private int weight;


    public ApproveTransfer() {
        super();
    }

    public ApproveTransfer(int moduleIndex, int callIndex) {
        super(moduleIndex, callIndex);
    }

    public ApproveTransfer(Metadata metadata) {
        this();
        init(metadata);
    }

    /**
     * Initialize call index from Runtime Metadata
     *
     * @param metadata current Runtime
     */
    public void init(Metadata metadata) {
        init(metadata, "approve_as_multi");
    }

    /**
     * Initialize call index for given call of the Balance module from Runtime Metadata
     *
     * @param metadata current Runtime
     * @param callName name of the call to execute, e.g. transfer, transfer_keep_alive, or transfer_all
     */
    public void init(Metadata metadata, String callName) {
//        Metadata.Call call = metadata.findCall("Multisig", callName)
//                .orElseThrow(() -> new IllegalStateException("Call 'Balances." + callName + "' doesn't exist"));
//        init(call);
    }

    public List<UnionValue<MultiAddress>> getOtherSignatures() {
        return otherSignatures;
    }



    public void setOtherSignatures(List<Address> otherSignatures) {
        List<UnionValue<MultiAddress>> temp = new ArrayList<>();
        for (int i = 0; i < otherSignatures.size(); i++) {
            temp.add(MultiAddress.AccountID.from(otherSignatures.get(i)));
        }
        this.otherSignatures = temp;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getWeight() {
        return weight;
    }

    public Timepoint getTimepoint() {
        return timepoint;
    }

    public void setTimepoint(Timepoint timepoint) {
        this.timepoint = timepoint;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApproveTransfer)) return false;
        if (!super.equals(o)) return false;
        ApproveTransfer that = (ApproveTransfer) o;
        return threshold == that.threshold && Objects.equals(otherSignatures, that.otherSignatures) && Objects.equals(timepoint, that.timepoint) && weight == that.weight && hash == that.hash;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), threshold, otherSignatures, timepoint, hash, weight);
    }

    @Override
    public boolean canEquals(Object o) {
        return (o instanceof ApproveTransfer);
    }

    @Override
    public String toString() {
        return "ApproveTransfer{" +
                "threshold=" + threshold +
                ", otherSignatures=" + otherSignatures.toString() +
                ", timepoint=" + timepoint.toString() +
                ", hash=" + Arrays.toString(hash) +
                ", weight=" + weight +
                '}';
    }
}
