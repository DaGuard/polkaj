import io.emeraldpay.polkaj.apiws.JavaHttpSubscriptionAdapter;
import io.emeraldpay.polkaj.scale.ScaleExtract;
import io.emeraldpay.polkaj.scaletypes.AccountInfo;
import io.emeraldpay.polkaj.scaletypes.Metadata;
import io.emeraldpay.polkaj.scaletypes.MetadataReader;
import io.emeraldpay.polkaj.schnorrkel.Schnorrkel;
import io.emeraldpay.polkaj.schnorrkel.SchnorrkelException;
import io.emeraldpay.polkaj.ss58.SS58Type;
import io.emeraldpay.polkaj.tx.AccountRequests;
import io.emeraldpay.polkaj.tx.ExtrinsicContext;
import io.emeraldpay.polkaj.tx.SignException;
import io.emeraldpay.polkaj.types.Address;
import io.emeraldpay.polkaj.api.PolkadotApi;

import java.io.*;
import java.net.*;
import java.lang.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import com.google.common.collect.Lists;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

import org.bitcoinj.core.Base58;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.primitives.UnsignedBytes;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;
import java.math.BigInteger;

import io.emeraldpay.polkaj.api.*;

import io.emeraldpay.polkaj.types.*;
import org.apache.commons.codec.binary.Hex;
import org.w3c.dom.css.CSSCharsetRule;

public class Multisig {
    private static final DotAmountFormatter AMOUNT_FORMAT = DotAmountFormatter.autoFormatter();

    public static void main(String[] args) throws SchnorrkelException, Exception {
        //change from kusama?
        //extrinsincs and rpc
        //search encodeHex and metadata text

        String api = "wss://westend-rpc.polkadot.io";

        System.out.println("Connect to: " + api);

        Schnorrkel.KeyPair aliceKey = Schnorrkel.getInstance().generateKeyPairFromSeed(
                Hex.decodeHex("e5be9a5092b81bca64be81d212e7f2f9eba183bb7a90954f7b76361f6edb5c0a")
        );
        Schnorrkel.KeyPair bobKey = Schnorrkel.getInstance().generateKeyPairFromSeed(Hex.decodeHex("38225f05ae70d1cf6979b9e44e3eada7084dd3ae5e51d08a5146e24bd4f5c68f"));
        Schnorrkel.KeyPair michaelKey = Schnorrkel.getInstance().generateKeyPairFromSeed(Hex.decodeHex("38d2fe416cd50160b32ddbf0fc8224fc8b94c58e537825f416c82cbbf1e64b22"));
//        byte[] result = new byte[96];
//        System.arraycopy(bobKey.getSecretKey(), 0, result, 0, bobKey.getSecretKey().length);
//        System.arraycopy(bobKey.getPublicKey(), 0, result, 64, bobKey.getPublicKey().length);
//        System.out.println(Hex.encodeHex(result));
//        System.arraycopy(michaelKey.getSecretKey(), 0, result, 0, michaelKey.getSecretKey().length);
//        System.arraycopy(michaelKey.getPublicKey(), 0, result, 64, michaelKey.getPublicKey().length);
//
//        System.out.println(Hex.encodeHex(result));
//        byte[] seed = new byte[32];
//        SecureRandom.getInstanceStrong().nextBytes(seed);
//        System.out.println(Hex.encodeHex(seed));
//        seed = new byte[32];
//        SecureRandom.getInstanceStrong().nextBytes(seed);
//        System.out.println(Hex.encodeHex(seed));

        Address aliceAddress;
        Address bobAddress;
        Address michaelAddress;
        aliceAddress = new Address(SS58Type.Network.SUBSTRATE, aliceKey.getPublicKey());
        bobAddress = new Address(SS58Type.Network.SUBSTRATE, bobKey.getPublicKey());
        michaelAddress = new Address(SS58Type.Network.SUBSTRATE, michaelKey.getPublicKey());
        System.out.println(aliceAddress);
        System.out.println(bobAddress);
        System.out.println(michaelAddress);

        Random random = new Random();
        DotAmount amount = DotAmount.fromPlancks(
                Math.abs(random.nextLong()) % DotAmount.fromDots(20).getValue().longValue()
        );


        String[] addresses = new String[]{aliceAddress.toString(), bobAddress.toString(), michaelAddress.toString()};
//        String[] addresses = new String[]{"16JKTEBr47wm2ryL6gVGs8aQTsggRK8DWYqanayR29TaxmWm", "1A41j9wx215FqtaZF39hpoZqaeSH7UHSXNvxDokS8Wb1Lmh", "16MC42X9HLEuwaFYdttEn8wLsHjBXFKLPksnBRr1MNLjigD"};
        int threshold = 2;

        String multisig = encodeMultiAddress(addresses, threshold);
        System.out.println(multisig);
        Address multisigAddress = Address.from(multisig);
        final JavaHttpSubscriptionAdapter adapter = JavaHttpSubscriptionAdapter.newBuilder().connectTo(api).build();
        try (PolkadotApi client = PolkadotApi.newBuilder().subscriptionAdapter(adapter).build()) {
            System.out.println("Connected: " + adapter.connect().get());

            // Subscribe to block heights
            AtomicLong height = new AtomicLong(0);
            CompletableFuture<Long> waitForBlocks = new CompletableFuture<>();
            client.subscribe(
                    StandardSubscriptions.getInstance().newHeads()
            ).get().handler((event) -> {
                long current = event.getResult().getNumber();
                System.out.println("Current height: " + current);
                if (height.get() == 0) {
                    height.set(current);
                } else {
                    long blocks = current - height.get();
                    if (blocks > 3) {
                        waitForBlocks.complete(current);
                    }
                }
            });

            // Subscribe to balance updates
            AccountRequests.AddressBalance from = AccountRequests.balanceOf(multisigAddress);
            AccountRequests.AddressBalance to = AccountRequests.balanceOf(aliceAddress);
            client.subscribe(
                    StandardSubscriptions.getInstance()
                            .storage(Arrays.asList(
                                    // need to provide actual encoded requests
                                    from.encodeRequest(),
                                    to.encodeRequest())
                            )
            ).get().handler((event) -> {
                event.getResult().getChanges().forEach((change) -> {
                    AccountInfo value = null;
                    Address target = null;
                    if (from.isKeyEqualTo(change.getKey())) {
                        value = from.apply(change.getData());
                        target = multisigAddress;
                    } else if (to.isKeyEqualTo(change.getKey())) {
                        value = to.apply(change.getData());
                        target = aliceAddress;
                    } else {
                        System.err.println("Invalid key: " + change.getKey());
                    }
                    if (value != null) {
                        System.out.println("Balance update. User: " + target + ", new balance: " + AMOUNT_FORMAT.format(value.getData().getFree()));
                    }
                });
            });


            // prepare context for execution
            ExtrinsicContext context = ExtrinsicContext.newAutoBuilder(multisigAddress, client)
                    .get()
                    .build();

            // get current balance to show, optional
            AccountInfo fromAccount = from.execute(client).get();

            System.out.println("Using genesis : " + context.getGenesis());
            System.out.println("Using runtime : " + context.getTxVersion() + ", " + context.getRuntimeVersion());
            System.out.println("Using nonce   : " + context.getNonce());
            System.out.println("------");
            System.out.println("Currently available: " + AMOUNT_FORMAT.format(fromAccount.getData().getFree()));
            System.out.println("Transfer           : " + AMOUNT_FORMAT.format(amount) + " from " + michaelAddress + " to " + multisigAddress);
            //aprove as multi callIndex:2, moduleindex: 31
            //as multi callIndex: 1,moduleIndex:31
            // prepare call, and sign with sender Secret Key within the context
            AccountRequests.TransferBuilder transfer = AccountRequests.transfer()
                    .from(multisigAddress)
                    .to(aliceAddress)
                    .amount(amount);
            byte[] hash = bnToU8a(BigInteger.valueOf(transfer.hashCode()),true,false,32);
            //timepoint null
            int MAX_WEIGHT = 640000000;
            Address[] otherSignaturesSorted = new Address[]{aliceAddress,michaelAddress};
            Arrays.sort(otherSignaturesSorted);
            AccountRequests.Approve approve = AccountRequests.approve()
                    .module(31,2)
                    .from(bobAddress)
                    .threshold(threshold)
                    .otherSignatures(Arrays.asList(otherSignaturesSorted))
                    .timepoint(null)
                    .hash(hash)
                    .weight(MAX_WEIGHT)
                    .sign(bobKey,context)
                    .build();
            ByteData req = approve.encodeRequest();
            System.out.println("RPC Request Payload: " + req);
            //tx?
            //use sample type 137 multiaddress with index like timepoint
            Hash256 txid = client.execute(
                    StandardCommands.getInstance().authorSubmitExtrinsic(req)
            ).get();
            System.out.println("Tx Hash: " + txid);

            // wait for a few blocks, to show how subscription to storage changes works, which will
            // notify about relevant updates during those blocks
            waitForBlocks.get();
//            ByteData req = transfer.encodeRequest();
//            System.out.println("RPC Request Payload: " + req);
//            Hash256 txid = client.execute(
//                    StandardCommands.getInstance().authorSubmitExtrinsic(req)
//            ).get();
//            System.out.println("Tx Hash: " + txid);
//
//            // wait for a few blocks, to show how subscription to storage changes works, which will
//            // notify about relevant updates during those blocks
//            waitForBlocks.get();
        }


    }

    public static String encodeMultiAddress(String[] addresses, int threshold) {
        return encodeAddress(createKeyMulti(addresses, BigInteger.valueOf(threshold)));
    }

    public static List<byte[]> u8aSorted(String[] who) {
        List<byte[]> unsorted = new ArrayList<>();
        for (int i = 0; i < who.length; i++) {
            unsorted.add(decodeAddress(who[i]));
        }
        Collections.sort(unsorted, UnsignedBytes.lexicographicalComparator());

        for (byte[] bytes : unsorted) {
            System.out.println(Arrays.toString(bytes));
        }
        return unsorted;
    }

    public static byte[] createKeyMulti(String[] addresses, BigInteger threshold) {
        List<byte[]> sorted = u8aSorted(addresses);
        //change args to spread operator somehow instead of hardcoded
        return blake2AsU8a(u8aConcat(Lists.newArrayList(stringToU8a("modlpy/utilisuba"), compactToU8a(BigInteger.valueOf(addresses.length)), sorted.get(0), sorted.get(1), sorted.get(2), bnToU8a(threshold, true, false, 16))), 256, null);
    }

    public static byte[] toByteArrayUnsigned(BigInteger bi) {
        byte[] extractedBytes = bi.toByteArray();
        int skipped = 0;
        boolean skip = true;
        for (byte b : extractedBytes) {
            boolean signByte = b == (byte) 0x00;
            if (skip && signByte) {
                skipped++;
                continue;
            } else if (skip) {
                skip = false;
            }
        }
        extractedBytes = Arrays.copyOfRange(extractedBytes, skipped,
                extractedBytes.length);
        return extractedBytes;
    }

    public static byte[] toByteArrayLittleEndianUnsigned(BigInteger bi) {
        byte[] extractedBytes = toByteArrayUnsigned(bi);
        ArrayUtils.reverse(extractedBytes);
        //byte[] reversed = ByteUtils.reverseArray(extractedBytes);
        return extractedBytes;
    }

    public static byte[] bnToU8a(BigInteger value, boolean isLe, boolean isNegative, int bitLength) {
        int byteLength;
        if (bitLength == -1) {
            byteLength = (int) Math.ceil(value.bitLength() / 8f);
        } else {
            byteLength = (int) Math.ceil(bitLength / 8f);
        }

        if (value == null) {
            if (bitLength == -1) {
                return new byte[0];
            } else {
                return new byte[byteLength];
            }
        }

        byte[] output = new byte[byteLength];

        if (isNegative) {
            //TODO  valueBn.negate()
            //const bn = _options.isNegative ? valueBn.toTwos(byteLength * 8) : valueBn;
        }

        if (isLe) {
            byte[] bytes = toByteArrayLittleEndianUnsigned(value);
            //arraycopy(Object src,  int  srcPos,
            //Object dest, int destPos,
            //int length);
            System.arraycopy(bytes, 0, output, 0, bytes.length);
        } else {
            //big-endian
            byte[] bytes = value.toByteArray();
            System.arraycopy(bytes, 0, output, output.length - bytes.length, bytes.length);
        }
        //if (output.length != bytes.length) {
        //    throw new RuntimeException();
        //}

        return output;

    }

    public static byte[] compactToU8a(BigInteger value) {
        BigInteger MAX_U8 = BigInteger.valueOf(2).pow(8 - 2).subtract(BigInteger.ONE);
        BigInteger MAX_U16 = BigInteger.valueOf(2).pow(16 - 2).subtract(BigInteger.ONE);
        BigInteger MAX_U32 = BigInteger.valueOf(2).pow(32 - 2).subtract(BigInteger.ONE);
        if (value.compareTo(MAX_U8) <= 0) {
            return new byte[]{UnsignedBytes.parseUnsignedByte((value.intValue() << 2) + "")};
        } else if (value.compareTo(MAX_U16) <= 0) {
            return bnToU8a(value.shiftLeft(2).add(BigInteger.valueOf(0b01)), true, false, 16);
        } else if (value.compareTo(MAX_U32) <= 0) {
            return bnToU8a(value.shiftLeft(2).add(BigInteger.valueOf(0b10)), true, false, 32);
        }

        byte[] u8a = bnToU8a(value, true, false, -1);
        int length = u8a.length;

        while (u8a[length - 1] == 0) {
            length--;
        }

        assert length >= 4 : "Previous tests match anyting less than 2^30; qed";

        return u8aConcat(Lists.newArrayList(
                // substract 4 as minimum (also catered for in decoding)
                new byte[]{UnsignedBytes.parseUnsignedByte((((length - 4) << 2) + 0b11) + "")},
                ArrayUtils.subarray(u8a, 0, length)
        ));
    }

    public static String encodeAddress(byte[] _key) {
        byte[] key = u8aToU8a(_key);
        System.out.println(Arrays.toString(key));
        boolean isPublicKey = key.length == 32 || key.length == 33;
        byte ss58Format = 42;
        int[] avaliable = new int[]{46, 67};
        assert ss58Format >= 0 && ss58Format <= 16383 && Arrays.asList(avaliable).indexOf(ss58Format) == -1 : "Out of range ss58Format specified";
        int[] allowedDecodedLengths = new int[]{1, 2, 4, 8, 32};
        assert Arrays.asList(allowedDecodedLengths).indexOf(key.length) > -1 : "Expected a valid key to convert, with length" + Arrays.toString(allowedDecodedLengths) + ":" + key.length;
        byte[] input = u8aConcat(Lists.newArrayList(new byte[]{ss58Format}, key));
        byte[] hash = sshash(input);
        byte[] bytes = u8aConcat(Lists.newArrayList(input, ArrayUtils.subarray(hash, 0, isPublicKey ? 2 : 1)));
        String result = Base58.encode(bytes);
        //System.out.println(result);
        return result;
    }

    public static String encodeAddress(String key) {
        // decode it, this means we can re-encode an address
        byte ss58Format = 42;
        byte[] u8a = decodeAddress(key);
        System.out.println(Arrays.toString(u8a));
        boolean isPublicKey = u8a.length == 32 || u8a.length == 33;

        int[] avaliable = new int[]{46, 67};
        assert ss58Format >= 0 && ss58Format <= 16383 && Arrays.asList(avaliable).indexOf(ss58Format) == -1 : "Out of range ss58Format specified";
        int[] allowedDecodedLengths = new int[]{1, 2, 4, 8, 32};
        assert Arrays.asList(allowedDecodedLengths).indexOf(u8a.length) > -1 : "Expected a valid key to convert, with length" + Arrays.toString(allowedDecodedLengths) + ":" + key.length();
        byte[] input = u8aConcat(Lists.newArrayList(new byte[]{ss58Format}, u8a));
        byte[] hash = sshash(input);
        byte[] bytes = u8aConcat(Lists.newArrayList(input, ArrayUtils.subarray(hash, 0, isPublicKey ? 2 : 1)));
        return Base58.encode(bytes);
    }

    //source of error? need to decode first
    public static byte[] decodeAddress(String encoded) {
        boolean ignoreChecksum = false;
        assert encoded != null && encoded.length() > 0 : "Invalid empty address passed";

        byte[] decoded = Base58.decode(encoded);

        String errorPre = "Decoding " + encoded + ":";
        int[] allowedEncodedLengths = new int[]{3, 4, 6, 10, 35, 36, 37, 38};
        assert Arrays.asList(allowedEncodedLengths).indexOf(decoded.length) > -1 : "Invalid decoded address length";
        boolean isPublicKey = decoded.length == 35;

        // non-publicKeys has 1 byte checksums, else default to 2
        int endPos = decoded.length - (isPublicKey ? 2 : 1);
        // calculate the hash and do the checksum byte checks
        byte[] hash = sshash(ArrayUtils.subarray(decoded, 0, endPos));
        boolean checks = isPublicKey
                ? decoded[decoded.length - 2] == hash[0] && decoded[decoded.length - 1] == hash[1]
                : decoded[decoded.length - 1] == hash[0];

        assert ignoreChecksum || checks : errorPre + "Invalid decoded address checksum";

        return ArrayUtils.subarray(decoded, 1, endPos);

    }


    public static byte[] stringToU8a(String value) {
        if (StringUtils.isEmpty(value)) {
            return new byte[0];
        }

        //TODO 2019-05-09 00:48 test
        return value.getBytes();
    }

    public static byte[] sshash(byte[] key) {
        byte[] SS58_PREFIX = stringToU8a("SS58PRE");
        return blake2AsU8a(u8aConcat(Lists.newArrayList(SS58_PREFIX, key)), 512, null);
    }

    public static byte[] blake2AsU8a(byte[] data, int bitLength, byte[] key) {

        int byteLength = (int) Math.ceil(bitLength / 8F);

        Blake2bDigest blake2bkeyed = new Blake2bDigest(key, byteLength, null, null);
        blake2bkeyed.reset();
        blake2bkeyed.update(data, 0, data.length);
        byte[] keyedHash = new byte[64];
        int digestLength = blake2bkeyed.doFinal(keyedHash, 0);

        return ArrayUtils.subarray(keyedHash, 0, digestLength);


    }

    public static byte[] u8aConcat(List<byte[]> _list) {
        List<byte[]> list = _list.stream().map(e -> u8aToU8a(e)).collect(Collectors.toList());

        int length = list.stream().mapToInt(e -> e.length).sum();
        byte[] result = new byte[length];
        int offset = 0;

        for (byte[] bytes : list) {
            System.arraycopy(bytes, 0, result, offset, bytes.length);
            offset += bytes.length;
        }
        return result;
    }


    public static byte[] u8aToU8a(Object value) {
        if (value == null) {
            return new byte[0];
        }

        return (byte[]) value;
    }
}
