package io.emeraldpay.polkaj.scaletypes;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Runtime Metadata, which defines all available actions and types for the blockchain.
 * Available through state_getMetadata RPC.
 * <p>
 * Reference: https://github.com/polkadot-js/api/blob/master/packages/types/src/interfaces/metadata/definitions.ts
 */
public class Metadata {

    private Integer magic;
    private Integer version;
    private List<Module> modules;

    public Integer getMagic() {
        return magic;
    }

    public void setMagic(Integer magic) {
        this.magic = magic;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public Optional<Module> findModule(String name) {
        if (modules == null) {
            return Optional.empty();
        }
        return getModules().stream()
                .filter(it -> it.getName().equals(name))
                .findAny();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metadata)) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(magic, metadata.magic) &&
                Objects.equals(version, metadata.version) &&
                Objects.equals(modules, metadata.modules);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(magic, version, modules);
    }

    public static class Module {
        private String name;
        private Storage storage;
        private TypeWrapper calls;
        private TypeWrapper events;
        private List<Constant> constants;
        private TypeWrapper errors;
        private Integer index;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Storage getStorage() {
            return storage;
        }

        public void setStorage(Storage storage) {
            this.storage = storage;
        }

        public TypeWrapper getCalls() {
            return calls;
        }

        public void setCalls(TypeWrapper calls) {
            this.calls = calls;
        }

        public TypeWrapper getEvents() {
            return events;
        }

        public void setEvents(TypeWrapper events) {
            this.events = events;
        }

        public List<Constant> getConstants() {
            return constants;
        }

        public void setConstants(List<Constant> constants) {
            this.constants = constants;
        }

        public TypeWrapper getErrors() {
            return errors;
        }

        public void setErrors(TypeWrapper errors) {
            this.errors = errors;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Module)) return false;
            Module module = (Module) o;
            return Objects.equals(name, module.name) &&
                    Objects.equals(storage, module.storage) &&
                    Objects.equals(calls, module.calls) &&
                    Objects.equals(events, module.events) &&
                    Objects.equals(constants, module.constants) &&
                    Objects.equals(errors, module.errors) &&
                    Objects.equals(index, module.index);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(name, storage, calls, events, constants, errors, index);
        }
    }

    public static class Storage {
        private String prefix;
        private List<Entry> entries;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public List<Entry> getEntries() {
            return entries;
        }

        public void setEntries(List<Entry> entries) {
            this.entries = entries;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Storage)) return false;
            Storage storage = (Storage) o;
            return Objects.equals(prefix, storage.prefix) &&
                    Objects.equals(entries, storage.entries);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(prefix, entries);
        }

        public static class Entry {
            private String name;
            private Modifier modifier;
            private Type<?> type;
            private byte[] defaults;
            private List<String> documentation;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Modifier getModifier() {
                return modifier;
            }

            public void setModifier(Modifier modifier) {
                this.modifier = modifier;
            }

            public Type<?> getType() {
                return type;
            }

            public void setType(Type<?> type) {
                this.type = type;
            }

            public byte[] getDefaults() {
                return defaults;
            }

            public void setDefaults(byte[] defaults) {
                this.defaults = defaults;
            }

            public List<String> getDocumentation() {
                return documentation;
            }

            public void setDocumentation(List<String> documentation) {
                this.documentation = documentation;
            }

            @Override
            public final boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Entry)) return false;
                Entry entry = (Entry) o;
                return Objects.equals(name, entry.name) &&
                        modifier == entry.modifier &&
                        Objects.equals(type, entry.type) &&
                        Arrays.equals(defaults, entry.defaults) &&
                        Objects.equals(documentation, entry.documentation);
            }

            @Override
            public final int hashCode() {
                int result = Objects.hash(name, modifier, type, documentation);
                result = 31 * result + Arrays.hashCode(defaults);
                return result;
            }
        }

        public static enum Modifier {
            OPTIONAL, DEFAULT, REQUIRED
        }

        public static enum Hasher {
            BLAKE2_128,
            BLAKE2_256,
            BLAKE2_256_CONCAT,
            TWOX_128,
            TWOX_256,
            TWOX_64_CONCAT,
            IDENTITY
        }

        public static enum TypeId {
            PLAIN(String.class),
            MAP(MapDefinition.class),
            DOUBLEMAP(DoubleMapDefinition.class);

            private final Class<?> clazz;

            TypeId(Class<?> clazz) {
                this.clazz = clazz;
            }

            public Class<?> getClazz() {
                return clazz;
            }
        }

        public abstract static class Type<T> {
            private final T value;

            public Type(T value) {
                this.value = value;
            }

            public abstract TypeId getId();

            public T get() {
                return value;
            }

            @SuppressWarnings("unchecked")
            public <X> Type<X> cast(Class<X> clazz) {
                if (clazz.isAssignableFrom(getId().getClazz())) {
                    return (Type<X>) this;
                }
                throw new ClassCastException("Cannot cast " + getId().getClazz() + " to " + clazz);
            }

            @Override
            public final boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Type)) return false;
                Type<?> type = (Type<?>) o;
                return Objects.equals(value, type.value);
            }

            @Override
            public final int hashCode() {
                return Objects.hash(value);
            }
        }

        public static class PlainType extends Type<Integer> {
            public PlainType(Integer value) {
                super(value);
            }

            @Override
            public TypeId getId() {
                return TypeId.PLAIN;
            }
        }

        public static class MapDefinition {
            private List<Hasher> hashers;
            private int key;
            private int value;

            public List<Hasher> getHashers() {
                return hashers;
            }

            public void setHashers(List<Hasher> hashers) {
                this.hashers = hashers;
            }

            public int getKey() {
                return key;
            }

            public void setKey(int key) {
                this.key = key;
            }

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }

            @Override
            public final boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof MapDefinition)) return false;
                MapDefinition that = (MapDefinition) o;
                return Objects.equals(hashers, that.hashers) &&
                        key == that.key &&
                        value == that.value;
            }

            @Override
            public final int hashCode() {
                return Objects.hash(hashers, key, value);
            }
        }

        public static class MapType extends Type<MapDefinition> {
            public MapType(MapDefinition value) {
                super(value);
            }

            @Override
            public TypeId getId() {
                return TypeId.MAP;
            }
        }

        public static class DoubleMapDefinition {
            private Hasher firstHasher;
            private String firstKey;
            private Hasher secondHasher;
            private String secondKey;
            private String type;

            public Hasher getFirstHasher() {
                return firstHasher;
            }

            public void setFirstHasher(Hasher firstHasher) {
                this.firstHasher = firstHasher;
            }

            public String getFirstKey() {
                return firstKey;
            }

            public void setFirstKey(String firstKey) {
                this.firstKey = firstKey;
            }

            public String getSecondKey() {
                return secondKey;
            }

            public void setSecondKey(String secondKey) {
                this.secondKey = secondKey;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Hasher getSecondHasher() {
                return secondHasher;
            }

            public void setSecondHasher(Hasher secondHasher) {
                this.secondHasher = secondHasher;
            }

            @Override
            public final boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof DoubleMapDefinition)) return false;
                DoubleMapDefinition that = (DoubleMapDefinition) o;
                return firstHasher == that.firstHasher &&
                        Objects.equals(firstKey, that.firstKey) &&
                        Objects.equals(secondKey, that.secondKey) &&
                        Objects.equals(type, that.type) &&
                        secondHasher == that.secondHasher;
            }

            @Override
            public final int hashCode() {
                return Objects.hash(firstHasher, firstKey, secondKey, type, secondHasher);
            }
        }

        public static class DoubleMapType extends Type<DoubleMapDefinition> {
            public DoubleMapType(DoubleMapDefinition value) {
                super(value);
            }

            @Override
            public TypeId getId() {
                return TypeId.DOUBLEMAP;
            }
        }
    }

    public static class TypeWrapper {
        private int type;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeWrapper that = (TypeWrapper) o;
            return type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }
    }

    public static class Constant {
        private String name;
        private int type;
        private byte[] value;
        private List<String> documentation;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public byte[] getValue() {
            return value;
        }

        public void setValue(byte[] value) {
            this.value = value;
        }

        public List<String> getDocumentation() {
            return documentation;
        }

        public void setDocumentation(List<String> documentation) {
            this.documentation = documentation;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Constant)) return false;
            Constant constant = (Constant) o;
            return Objects.equals(name, constant.name) &&
                    type == constant.type &&
                    Arrays.equals(value, constant.value) &&
                    Objects.equals(documentation, constant.documentation);
        }

        @Override
        public final int hashCode() {
            int result = Objects.hash(name, type, documentation);
            result = 31 * result + Arrays.hashCode(value);
            return result;
        }
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "magic=" + magic +
                ", version=" + version +
                '}';
    }
}