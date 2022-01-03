package io.emeraldpay.polkaj.scaletypes;

import io.emeraldpay.polkaj.scale.ScaleCodecReader;
import io.emeraldpay.polkaj.scale.ScaleReader;
import io.emeraldpay.polkaj.scale.reader.EnumReader;
import io.emeraldpay.polkaj.scale.reader.ListReader;
import io.emeraldpay.polkaj.scale.reader.UnionReader;

import java.util.List;

public class MetadataReader implements ScaleReader<Metadata> {

    public static final ListReader<Metadata.Module> MODULE_LIST_READER = new ListReader<>(new ModulesReader());
    public static final ListReader<String> STRING_LIST_READER = new ListReader<>(ScaleCodecReader.STRING);
    public static final ListReader<Metadata.Storage.Hasher> HASHER_ENUM_READER = new ListReader<Metadata.Storage.Hasher>(new EnumReader(Metadata.Storage.Hasher.values()));
    @Override
    public Metadata read(ScaleCodecReader rdr) {
        Metadata result = new Metadata();
        result.setMagic(ScaleCodecReader.INT32.read(rdr));
        result.setVersion(rdr.readUByte());
        //rmbr changed
        if (result.getVersion() != 14) {
            throw new IllegalStateException("Unsupported metadata version: " + result.getVersion());
        }
        result.setModules(MODULE_LIST_READER.read(rdr));
        List<Metadata.Module> modules = result.getModules();

        for (Metadata.Module m: modules) {
            Metadata.TypeWrapper calls = m.getCalls();

        }
        return result;
    }

    static class ModulesReader implements ScaleReader<Metadata.Module> {

        public static final StorageReader STORAGE_READER = new StorageReader();
        public static final TypeWrapperReader CALL_LIST_READER = new TypeWrapperReader();
        public static final TypeWrapperReader EVENT_LIST_READER = new TypeWrapperReader();
        public static final ListReader<Metadata.Constant> CONSTANT_LIST_READER = new ListReader<>(new ConstantReader());
        public static final TypeWrapperReader ERROR_LIST_READER = new TypeWrapperReader();

        @Override
        public Metadata.Module read(ScaleCodecReader rdr) {
            Metadata.Module result = new Metadata.Module();

            result.setName(rdr.readString());
            rdr.readOptional(STORAGE_READER).ifPresent(result::setStorage);
            rdr.readOptional(CALL_LIST_READER).ifPresent(result::setCalls);
            rdr.readOptional(EVENT_LIST_READER).ifPresent(result::setEvents);
            result.setConstants(CONSTANT_LIST_READER.read(rdr));
            result.setErrors(ERROR_LIST_READER.read(rdr));
            result.setIndex(rdr.readUByte());
            return result;
        }
    }

    static class StorageReader implements ScaleReader<Metadata.Storage> {

        public static final ListReader<Metadata.Storage.Entry> ENTRY_LIST_READER = new ListReader<>(new StorageEntryReader());

        @Override
        public Metadata.Storage read(ScaleCodecReader rdr) {
            Metadata.Storage result = new Metadata.Storage();
            result.setPrefix(rdr.readString());
            result.setEntries(ENTRY_LIST_READER.read(rdr));
            return result;
        }
    }

    static class StorageEntryReader implements ScaleReader<Metadata.Storage.Entry> {

        public static final EnumReader<Metadata.Storage.Modifier> MODIFIER_ENUM_READER = new EnumReader<>(Metadata.Storage.Modifier.values());
        public static final TypeReader TYPE_READER = new TypeReader();

        @Override
        public Metadata.Storage.Entry read(ScaleCodecReader rdr) {
            Metadata.Storage.Entry result = new Metadata.Storage.Entry();
            result.setName(rdr.readString());
            result.setModifier(MODIFIER_ENUM_READER.read(rdr));
            result.setType(rdr.read(TYPE_READER));
            result.setDefaults(rdr.readByteArray());
            result.setDocumentation(STRING_LIST_READER.read(rdr));
            return result;
        }
    }

    static class TypeReader implements ScaleReader<Metadata.Storage.Type<?>> {

        @SuppressWarnings("unchecked")
        private static final UnionReader<Metadata.Storage.Type<?>> TYPE_UNION_READER = new UnionReader<>(
                new TypePlainReader(),
                new TypeMapReader());

        @Override
        public Metadata.Storage.Type<?> read(ScaleCodecReader rdr) {
            return TYPE_UNION_READER.read(rdr).getValue();
        }
    }

    static class TypePlainReader implements ScaleReader<Metadata.Storage.PlainType> {
        @Override
        public Metadata.Storage.PlainType read(ScaleCodecReader rdr) {
            return new Metadata.Storage.PlainType(rdr.readCompactInt());
        }
    }

    static class TypeMapReader implements ScaleReader<Metadata.Storage.MapType> {

        @Override
        public Metadata.Storage.MapType read(ScaleCodecReader rdr) {
            Metadata.Storage.MapDefinition definition = new Metadata.Storage.MapDefinition();
            //check agian
            definition.setHashers(HASHER_ENUM_READER.read(rdr));
            definition.setKey(rdr.readCompactInt());
            definition.setValue(rdr.readCompactInt());
            return new Metadata.Storage.MapType(definition);
        }
    }



    static class TypeWrapperReader implements ScaleReader<Metadata.TypeWrapper> {

        @Override
        public Metadata.TypeWrapper read(ScaleCodecReader rdr) {
            Metadata.TypeWrapper result = new Metadata.TypeWrapper();
            //UNSURE
            //maybe read string cast to int
            result.setType(rdr.readCompactInt());
            return result;
        }
    }


    static class ConstantReader implements ScaleReader<Metadata.Constant> {

        @Override
        public Metadata.Constant read(ScaleCodecReader rdr) {
            Metadata.Constant result = new Metadata.Constant();
            result.setName(rdr.readString());
            result.setType(rdr.readCompactInt());
            result.setValue(rdr.readByteArray());
            result.setDocumentation(STRING_LIST_READER.read(rdr));
            return result;
        }
    }

}
