# BitFuzz

BitFuzz is a library for defining schemas for binary protocols, serializing and deserializing data, as well as fuzzing
data based off of a schema with various type-specific generators and various mutators.

It's designed to be **easy to use** and as **agnostic as possible** to the libraries you want to use with it; this
means:

1. Support for little and big endian byte orders
2. Can be extended to work with any byte buffer library (NIO and Netty are supported out of the box)
3. Can be extended with any data types (primitives and more (see [Supported Types](#supported-types)) are supported out
   of the box)

## Submodules

This project is split into multiple submodules.

* **buffer** - Base interfaces for reading and writing bytes
    * **buffer-nio** - Implementation for Java NIO `ByteBuffer`
    * **buffer-netty** - Implementation for Netty `ByteBuf`
* [**schema-api**](#schema) - Main API for defining schemas and serializing/deserializing data
    * **schema-core** - Core implementation of the schema API
    * [**schema-std**](#with-the-standard-library) - Standard library for the schema API with additional types
* [**fuzz-api**](#fuzzing) - Interfaces for generators, mutators, shrinkers, and runners
    * **fuzz-core** - Standard generators, mutators and runners, and fuzzing entry point

At minimum, `schema-api` and `schema-core` are needed (or `schema-std` which includes both), plus a buffer module.

## Schema

The schema API allows defining schemas for binary protocols, serializing and deserializing data.

A schema is an ordered list of fields, each with a type and a name.

### With the standard library

`schema-std` has `Types`, which has everything you need for most protocols:

* Numbers that follow the schema's byte order
* Booleans
* Strings
* VarInts
* Collections
* Optionals
* Records

By default, the length is written as a VarInt (see [Options](#options)).

Schemas can be defined by creating a `SchemaBuilder` and adding fields to it.

```java
Schema player = SchemaBuilder.create()
        .field(Types.STRING, "name")
        .field(Types.INT, "level")
        .field(Types.BOOL, "online")
        .build();

player.

write(new NettyByteWriter(buffer),List.

of(
        "steve",
                12,
                true
));

List<Object> read = player.read(new NettyByteReader(buffer));
```

This writes the following bytes:

```
05 73 74 65 76 65 00 00 00 0c 01
╰╮ ╰──────┬─────╯ ╰────┬────╯ ╰╮
 │        │            │       ╰─ online: true
 │        │            ╰─ level: 12
 │        ╰─ name: "steve"
 ╰─ name.length: 5
```

#### Records

Fields can also be defined using record types.

```java
record Player(String name, @VarInt int level, boolean online, List<String> tags) {

}

Schema schema = SchemaBuilder.create()
        .field(Types.record(Player.class), "player")
        .build();

schema.

write(writer, List.of(new Player("steve", 300,true,List.of("op"))));
```

This writes the following bytes:

```
05 73 74 65 76 65 ac 02 01 01 02 6f 70
╰╮ ╰──────┬─────╯ ╰─┬─╯ ╰╮ ╰╮ ╰╮ ╰─┬─╯
 │        │         │    │  │  │   ╰─ player.tags[0]: "op"
 │        │         │    │  │  ╰─ player.tags[0].length: 2
 │        │         │    │  ╰─ player.tags.length: 1
 │        │         │    ╰─ player.online: true
 │        │         ╰─ player.level: 300
 │        ╰─ player.name: "steve"
 ╰─ player.name.length: 5
```

Each component's type is picked in this order:

1. An annotation marked with `@WireType` on the component, like the built-in `@VarInt` and `@VarLong`
2. The type set for its Java type in the schema's `RECORD_TYPES` option
3. The built-in types

Custom annotations are made in the same way the built-in ones are:

```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@WireType(Ascii.Spec.class)
public @interface Ascii {

    final class Spec implements Supplier<TypeSpec<?>> {

        @Override
        public TypeSpec<?> get() {
            return MyTypes.ASCII_STRING;
        }

    }

}

record Message(@Ascii String text) {

}
```

#### Options

Schema options are set with keys on the SchemaDefaults. Built-in keys are in `SchemaKeys` and standard library ones are
in `SchemaStandardKeys`.

| Key                               | Default value | Description                                                       |
|-----------------------------------|:--------------|:------------------------------------------------------------------|
| `SchemaKeys.ENDIANNESS`           | `BIG_ENDIAN`  | The default byte order of types (used by standard library)        |
| `SchemaStandardKeys.LENGTH_TYPE`  | `VAR_INT`     | The type of the length prefix for variable-length types           |
| `SchemaStandardKeys.RECORD_TYPES` | Empty         | The types records use for Java types instead of the built-in ones |

Here's how options are set:

```java
record Player(String name, int level) {

}

Schema schema = SchemaBuilder.create()
        .defaults(defaults -> defaults
                .set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN)
                .set(SchemaStandardKeys.LENGTH_TYPE, Types.INT)
                .update(SchemaStandardKeys.RECORD_TYPES, types ->
                        types.with(Integer.class, Types.VAR_INT)
                )
        )
        .field(Types.record(Player.class), "player")
        .field(Types.INT, "score")
        .build();

schema.

write(writer, List.of(new Player("steve", 300), 12));
```

Which would serialize as:

```
05 00 00 00 73 74 65 76 65 ac 02 0c 00 00 00
╰────┬────╯ ╰──────┬─────╯ ╰─┬─╯ ╰────┬────╯
     │             │         │        ╰─ score: 12
     │             │         ╰─ player.level: 300
     │             ╰─ player.name: "steve"
     ╰─ player.name.length: 5
```

The `int` in the record is written as a VarInt because of `RECORD_TYPES`, while the `score` field still uses
`Types.INT`, which is a little-endian int.

### Without the standard library

Using `schema-std` is not required. `schema-api` has `Primitives`, which are numbers with a fixed byte order,
so with only `schema-api` and `schema-core` you bring every other type yourself.

```java
Schema header = SchemaBuilder.create()
        .field(Primitives.INT, "id")
        .field(Primitives.SHORT_LE, "flags")
        .build();

header.

write(new NIOByteWriter(buffer),List.

of(7,(short) 1));
```

This writes the following bytes:

```
00 00 00 07 01 00
╰────┬────╯ ╰─┬─╯
     ╰─ id: 7 ╰─ flags: 1
```

### Extending the schema API

The simplest way to make custom types is a `SimpleType`, which takes the Java type, a write function, and a read
function:

```java
Type<String> asciiString = new SimpleType<>(String.class,
        (writer, value) -> {
            byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
            writer.writeByte((byte) bytes.length);
            writer.writeBytes(bytes);
        },
        reader -> {
            byte[] bytes = new byte[Byte.toUnsignedInt(reader.readByte())];
            reader.readBytes(bytes);
            return new String(bytes, StandardCharsets.US_ASCII);
        }
);
```

For anything more involved, `Type<T>` can be implemented.
For types made up of other types, implementing `CompositeType<T>` as well lets [`WireDump`](#wiredump) properly display
it.

A `TypeSpec<T>` is a type not picked until the schema is built, so it can read the schema's options.
`EndiannessTypeSpec` is one built-in example, it picks between a big and a little endian type:

```java
TypeSpec<Integer> integer = new EndiannessTypeSpec<>(Primitives.INT, Primitives.INT_LE);

Schema schema = SchemaBuilder.create()
        .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN))
        .field(integer, "level")
        .build();
```

Custom options are just another `SchemaKey`:

```java
public static final SchemaKey<Charset> CHARSET = new SchemaKey<>("charset", StandardCharsets.UTF_8);

TypeSpec<String> string = defaults -> new MyStringType(defaults.get(CHARSET));
```

## Fuzzing

`fuzz-core` tests schemas, and especially your own types, against random values and broken bytes.

### Round trip

`Fuzz.roundTrip` generates random valid values, writes them, and reads them back. The values read must equal the ones
written, and reading must use up exactly the bytes that were written.

```java
FuzzResult result = Fuzz.roundTrip(schema, 10_000, 42L);
System.out.

println(result.report());
```

Output:

```
Fuzzing finished after 10000 iterations, no failures found.
```

### Mutation

`Fuzz.mutate` generates random valid values, writes them, breaks the bytes with one to three random mutations, and reads
them back.
Reading has to either succeed or throw `SchemaException`, anything else is considered a failure.
Failing inputs are shrunk to the smallest bytes that still fail with the same exception.

For example, a string type that reads its length as a signed short:

```java
Type<String> buggyString = new SimpleType<>(String.class,
        (writer, value) -> {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            writer.writeShort((short) bytes.length);
            writer.writeBytes(bytes);
        },
        reader -> {
            byte[] bytes = new byte[reader.readShort()];
            reader.readBytes(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }
);

Schema schema = SchemaBuilder.create()
        .field(buggyString, "name")
        .field(Primitives.INT, "level")
        .build();

System.out.

println(Fuzz.mutate(schema, 10_000,42L).

report());
```

Output:

```
Fuzzing finished after 192 iterations, found 10 failures (showing first 5)
- seed=-6250311809693785164, iteration=40: java.lang.NegativeArraySizeException: -129
    input  (16 bytes): ff 7f ff ff 9c ac 31 f0 9f a0 ab 00 01 00 bb 72
    shrunk (2 bytes): ab 00
- seed=-4575290545323273701, iteration=44: java.lang.NegativeArraySizeException: -28905
    input  (8 bytes): 8f 17 a4 a5 9d 93 da 58
    shrunk (2 bytes): da 00
...
```

The built-in mutators are:

| Mutator                  | What it does                                                    |
|--------------------------|-----------------------------------------------------------------|
| `BitFlipMutator`         | Flips a single bit                                              |
| `InterestingByteMutator` | Replaces a byte with an edge value like `00`, `7f`, `80`, `ff`  |
| `InterestingIntMutator`  | Replaces bytes with an edge value integer, in either byte order |
| `TruncateMutator`        | Cuts off the end                                                |
| `DeleteRangeMutator`     | Removes a range of bytes                                        |
| `InsertRandomMutator`    | Inserts random bytes                                            |
| `DuplicateChunkMutator`  | Repeats a range of bytes                                        |

### Generators

Values are made by a `Generator` for each field, found through `Generators`.
The standard generators cover every type in `schema-std` and records.

One of the standard generators is one that's based off of the Java type, so the `buggyString` above is fuzzed with
random strings without any setup as Strings are a standard type.
For custom types, add a `GeneratorProvider` and use the runners directly:

```java
GeneratorProvider asciiProvider = new GeneratorProvider() {

    @Override
    public <T> Optional<Generator<T>> create(Type<T> type, GeneratorLookup lookup) {
        if (type != asciiString) {
            return Optional.empty();
        }

        Generator<String> generator = random -> random.ints(random.nextInt(16), 'a', 'z' + 1)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        //noinspection unchecked
        return Optional.of((Generator<T>) generator);
    }

};

Generators generators = Generators.standard().with(asciiProvider);

FuzzResult roundTrip = new RoundTripRunner(schema, generators).run(10_000, 42L);
FuzzResult mutation = new MutationRunner(schema, generators, Mutators.standard(), new ByteShrinker()).run(10_000, 42L);
```

`MutationRunner` takes any list of `Mutator`s and any `Shrinker`, so you can add protocol-specific mutations or swap the
shrinker.

### WireDump

`WireDump.explain` writes values and shows which value each byte belongs to, which is how every byte diagram in this
README was made.
It's handy for checking a type writes what you expect and for reading a shrunk failing input.

```java
System.out.println(WireDump.explain(schema, List.of("steve", 12,true)));
```

## Supported Types

1. **Primitives**
    * Byte, Short, Int, Long, Float, Double (big-endian, little-endian, or following the schema)
2. **Base Java types**
    * String (length-prefixed or fixed length, UTF-8), Boolean
3. **Variable-length**
    * VarInt, VarLong
4. **Collections**
    * List, Map
5. **Extras**
    * UUID, Optional, Records

## Requirements

Project is built with Java 25

## Usage

This project is published to Maven Central, one artifact per submodule.

Examples for

Gradle (Kotlin DSL):

```kotlin
dependencies {
    implementation("red.vortx.bitfuzz:schema-std:1.0.0") // standard brings in api and core
    implementation("red.vortx.bitfuzz:buffer-netty:1.0.0") // netty buffer

    testImplementation("red.vortx.bitfuzz:fuzz-core:1.0.0") // fuzzing as a test dependency
}
```

## License

Licensed under the Apache License, Version 2.0.
